package com.huizuike.flashdeliverjava;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient   // 添加这一行
public class FlashDeliverJavaApplication {

    public static void main(String[] args) {SpringApplication.run(FlashDeliverJavaApplication.class, args);
    }

}
