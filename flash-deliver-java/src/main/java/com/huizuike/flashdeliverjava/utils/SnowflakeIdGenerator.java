package com.huizuike.flashdeliverjava.utils;

import org.springframework.stereotype.Component;

import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Enumeration;

/**
 * 雪花算法 ID 生成器（分布式唯一ID）
 * 64位ID结构：1位符号位 + 41位时间戳 + 10位工作机器ID + 12位序列号
 */
@Component
public class SnowflakeIdGenerator {

    /**
     * 起始时间戳（2024-01-01 00:00:00）
     */
    private static final long EPOCH = 1704067200000L;

    /**
     * 机器ID位数
     */
    private static final long WORKER_ID_BITS = 10L;

    /**
     * 序列号位数
     */
    private static final long SEQUENCE_BITS = 12L;

    /**
     * 最大机器ID
     */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    /**
     * 序列号最大值
     */
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    /**
     * 机器ID左移位数
     */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /**
     * 时间戳左移位数
     */
    private static final long TIMESTAMP_SHIFT = WORKER_ID_BITS + SEQUENCE_BITS;

    private final long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator() {
        this.workerId = getWorkerId();
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(String.format("WorkerId不能大于%d或小于0", MAX_WORKER_ID));
        }
    }

    /**
     * 获取机器ID（根据MAC地址 + 随机数）
     */
    private long getWorkerId() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            StringBuilder sb = new StringBuilder();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null) {
                    for (byte b : mac) {
                        sb.append(String.format("%02X", b));
                    }
                }
            }
            // 取MAC地址的哈希值作为机器ID基础值
            return (sb.toString().hashCode() & 0xFFFF) % (MAX_WORKER_ID + 1);
        } catch (Exception e) {
            // 如果获取MAC失败，使用随机数
            return new SecureRandom().nextInt((int) MAX_WORKER_ID + 1);
        }
    }

    /**
     * 同步获取下一个ID
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        // 时钟回拨处理
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            if (offset <= 5) {
                try {
                    wait(offset << 1);
                    timestamp = timeGen();
                    if (timestamp < lastTimestamp) {
                        throw new RuntimeException("时钟回拨超过阈值，无法生成ID");
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new RuntimeException("时钟回拨超过阈值，无法生成ID");
            }
        }

        // 同一毫秒内，序列号递增
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        // 组合ID
        return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    /**
     * 等待下一毫秒
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 获取当前毫秒时间戳
     */
    private long timeGen() {
        return Instant.now().toEpochMilli();
    }

    /**
     * 生成用户默认昵称
     */
    public String generateDefaultNickname() {
        return "用户" + (nextId() % 100000000);
    }
}