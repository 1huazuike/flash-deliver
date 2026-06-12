-- 补充顾客数据到 3000 条
SET @row = (SELECT MAX(id) FROM `user` WHERE role = 'CUSTOMER');
INSERT INTO `user` (`id`, `phone`, `password`, `nickname`, `avatar`, `gender`, `role`, `status`, `last_login_time`, `last_login_ip`, `created_at`, `updated_at`, `is_deleted`)
SELECT
    @row := @row + 1,
    CONCAT('188', LPAD(@row, 7, '0')),
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
    CONCAT('顾客', @row),
    CONCAT('https://randomuser.me/api/portraits/lego/', @row % 100, '.jpg'),
    FLOOR(RAND() * 3),
    'CUSTOMER',
    1,
    NOW() - INTERVAL FLOOR(RAND() * 30) DAY,
    CONCAT('192.168.', FLOOR(RAND() * 255), '.', FLOOR(RAND() * 255)),
    NOW() - INTERVAL FLOOR(RAND() * 365) DAY,
    NOW(),
    0
FROM (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) t1,
    (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) t2,
    (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) t3
    LIMIT 1400;



-- =====================================================
-- 补充地址表数据（不使用变量）
-- =====================================================

-- 1. 为没有地址的顾客生成地址
INSERT INTO `address` (`user_id`, `contact_name`, `contact_phone`, `province`, `city`, `district`, `detail`, `lng`, `lat`, `is_default`, `created_at`, `updated_at`, `is_deleted`)
SELECT
    u.id,
    u.nickname,
    u.phone,
    '北京市',
    '北京市',
    CASE FLOOR(RAND() * 6)
        WHEN 0 THEN '东城区' WHEN 1 THEN '西城区' WHEN 2 THEN '朝阳区'
        WHEN 3 THEN '海淀区' WHEN 4 THEN '丰台区' ELSE '通州区'
        END,
    CONCAT('XX路', FLOOR(RAND() * 200) + 1, '号', '小区', FLOOR(RAND() * 50) + 1, '号楼', FLOOR(RAND() * 20) + 1, '单元', FLOOR(RAND() * 10) + 1, '室'),
    116.397428 + (RAND() - 0.5) * 0.1,
    39.909187 + (RAND() - 0.5) * 0.1,
    0,
    NOW() - INTERVAL FLOOR(RAND() * 730) DAY,
    NOW(),
    0
FROM `user` u
WHERE u.role = 'CUSTOMER'
  AND u.id NOT IN (SELECT DISTINCT user_id FROM address)
  AND RAND() < 0.6
    LIMIT 3000;

-- 2. 确保每个顾客至少有一个默认地址
UPDATE `address` a
    JOIN (
    SELECT user_id, MIN(id) AS first_id
    FROM `address`
    WHERE user_id IN (SELECT id FROM `user` WHERE role = 'CUSTOMER')
    GROUP BY user_id
    ) t ON a.id = t.first_id
    SET a.is_default = 1;
