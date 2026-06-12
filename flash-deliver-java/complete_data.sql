-- =====================================================
-- 完整数据生成脚本 - 即时配送平台（修复版）
-- =====================================================

-- 1. 清空所有表（按依赖顺序）
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `order_log`;
TRUNCATE TABLE `order_delivery`;
TRUNCATE TABLE `order_item`;
TRUNCATE TABLE `order`;
TRUNCATE TABLE `cart`;
TRUNCATE TABLE `product_sku`;
TRUNCATE TABLE `product`;
TRUNCATE TABLE `category`;
TRUNCATE TABLE `shop`;
TRUNCATE TABLE `address`;
TRUNCATE TABLE `rider`;
TRUNCATE TABLE `merchant`;
TRUNCATE TABLE `user`;
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 2. 用户表 - 3601 条
-- =====================================================

-- 2.1 重置变量
SET @row = 0;

-- 2.2 顾客 3000 条 (id: 1-3000)
INSERT INTO `user` (`id`, `phone`, `password`, `nickname`, `avatar`, `gender`, `role`, `status`, `last_login_time`, `last_login_ip`, `created_at`, `updated_at`, `is_deleted`)
SELECT
    @row := @row + 1 AS id,
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
LIMIT 3000;

-- 2.3 商家 300 条 (id: 3001-3300)
SET @mrow = 0;
INSERT INTO `user` (`id`, `phone`, `password`, `nickname`, `avatar`, `gender`, `role`, `status`, `created_at`, `updated_at`, `is_deleted`)
SELECT
    3000 + (@mrow := @mrow + 1) AS id,
    CONCAT('189', LPAD(@mrow, 7, '0')),
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
    CONCAT('商家', @mrow),
    CONCAT('https://randomuser.me/api/portraits/lego/', (3000 + @mrow) % 100, '.jpg'),
    FLOOR(RAND() * 2) + 1,
    'MERCHANT',
    1,
    NOW() - INTERVAL FLOOR(RAND() * 180) DAY,
    NOW(),
    0
FROM (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) t1,
     (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) t2,
     (SELECT 1 UNION SELECT 2 UNION SELECT 3) t3
LIMIT 300;

-- 2.4 骑手 300 条 (id: 3301-3600)
SET @rrow = 0;
INSERT INTO `user` (`id`, `phone`, `password`, `nickname`, `avatar`, `gender`, `role`, `status`, `created_at`, `updated_at`, `is_deleted`)
SELECT
    3300 + (@rrow := @rrow + 1) AS id,
    CONCAT('187', LPAD(@rrow, 7, '0')),
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
    CONCAT('骑手', @rrow),
    CONCAT('https://randomuser.me/api/portraits/lego/', (3300 + @rrow) % 100, '.jpg'),
    FLOOR(RAND() * 2) + 1,
    'RIDER',
    1,
    NOW() - INTERVAL FLOOR(RAND() * 90) DAY,
    NOW(),
    0
FROM (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) t1,
     (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) t2,
     (SELECT 1 UNION SELECT 2 UNION SELECT 3) t3
LIMIT 300;

-- 2.5 管理员 1 条 (id: 3601)
INSERT INTO `user` (`id`, `phone`, `password`, `nickname`, `avatar`, `gender`, `role`, `status`, `created_at`, `updated_at`, `is_deleted`) VALUES
    (3601, '18600000001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'https://randomuser.me/api/portraits/men/99.jpg', 1, 'ADMIN', 1, NOW(), NOW(), 0);

-- 验证用户表
SELECT 'user表统计' AS info;
SELECT role, COUNT(*) FROM `user` GROUP BY role;

-- =====================================================
-- 3. 商家表 - 300 条
-- =====================================================
INSERT INTO `merchant` (`user_id`, `shop_name`, `shop_logo`, `contact_name`, `contact_phone`, `business_license`, `status`, `settlement_rate`, `balance`, `created_at`, `updated_at`, `is_deleted`)
SELECT
    u.id,
    CONCAT('美食店铺', u.id),
    CONCAT('https://picsum.photos/200/200?random=', u.id),
    CONCAT('老板', u.id),
    CONCAT('139', LPAD(FLOOR(RAND() * 100000000), 8, '0')),
    CONCAT('91110100', LPAD(u.id, 6, '0'), 'MA', LPAD(u.id, 4, '0')),
    1,
    15.00 + (RAND() * 5),
    ROUND(RAND() * 50000, 2),
    NOW() - INTERVAL FLOOR(RAND() * 180) DAY,
    NOW(),
    0
FROM `user` u
WHERE u.role = 'MERCHANT';

-- =====================================================
-- 4. 骑手表 - 300 条
-- =====================================================
INSERT INTO `rider` (`user_id`, `real_name`, `id_card`, `driver_license`, `vehicle_type`, `vehicle_plate`, `status`, `online_status`, `current_lng`, `current_lat`, `rating`, `total_orders`, `balance`, `created_at`, `updated_at`, `is_deleted`)
SELECT
    u.id,
    CONCAT('骑手', u.id),
    CONCAT('4101011990', LPAD(FLOOR(RAND() * 100000000), 8, '0')),
    CONCAT('DL', u.id),
    CASE FLOOR(RAND() * 3)
        WHEN 0 THEN 'BIKE'
        WHEN 1 THEN 'MOTOR'
        ELSE 'CAR'
        END,
    CASE WHEN FLOOR(RAND() * 3) = 2 THEN CONCAT('京', CHAR(65 + FLOOR(RAND() * 26)), LPAD(FLOOR(RAND() * 100000), 5, '0')) ELSE NULL END,
    CASE WHEN RAND() < 0.85 THEN 1 ELSE 0 END,
    CASE WHEN RAND() < 0.6 THEN 1 ELSE 0 END,
    116.39 + (RAND() * 0.03),
    39.90 + (RAND() * 0.03),
    4.0 + (RAND() * 1.0),
    FLOOR(RAND() * 1500),
    ROUND(RAND() * 8000, 2),
    NOW() - INTERVAL FLOOR(RAND() * 180) DAY,
    NOW(),
    0
FROM `user` u
WHERE u.role = 'RIDER';

SELECT '商家/骑手表统计' AS info;
SELECT (SELECT COUNT(*) FROM merchant) AS merchant_count, (SELECT COUNT(*) FROM rider) AS rider_count;

-- =====================================================
-- 5. 地址表 - 每用户约 2-3 条
-- =====================================================
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
LIMIT 6000;

-- 设置默认地址（每个顾客第一个地址）
UPDATE `address` a
    JOIN (
        SELECT user_id, MIN(id) AS first_id
        FROM `address`
        WHERE user_id IN (SELECT id FROM `user` WHERE role = 'CUSTOMER')
        GROUP BY user_id
    ) t ON a.id = t.first_id
SET a.is_default = 1;

SELECT '地址表统计' AS info;
SELECT COUNT(*) AS address_count FROM address;

-- =====================================================
-- 6. 店铺表 - 300 条
-- =====================================================
INSERT INTO `shop` (`merchant_id`, `shop_name`, `shop_logo`, `banner`, `business_hours`, `phone`, `province`, `city`, `district`, `address`, `lng`, `lat`, `min_order_amount`, `delivery_fee`, `status`, `rating`, `created_at`, `updated_at`, `is_deleted`)
SELECT
    m.id,
    m.shop_name,
    m.shop_logo,
    CONCAT('https://picsum.photos/800/200?random=', m.id),
    '09:00-21:00',
    m.contact_phone,
    '北京市',
    '北京市',
    CASE FLOOR(RAND() * 6)
        WHEN 0 THEN '东城区' WHEN 1 THEN '西城区' WHEN 2 THEN '朝阳区'
        WHEN 3 THEN '海淀区' WHEN 4 THEN '丰台区' ELSE '通州区'
        END,
    CONCAT('美食街', FLOOR(RAND() * 100) + 1, '号'),
    116.39 + (RAND() * 0.05),
    39.90 + (RAND() * 0.05),
    ROUND(15 + RAND() * 35, 0),
    ROUND(3 + RAND() * 7, 1),
    1,
    4.0 + (RAND() * 1.0),
    NOW() - INTERVAL FLOOR(RAND() * 180) DAY,
    NOW(),
    0
FROM `merchant` m
LIMIT 300;

SELECT '店铺表统计' AS info;
SELECT COUNT(*) AS shop_count FROM shop;

-- =====================================================
-- 7. 商品分类表 - 每店铺 5-6 个
-- =====================================================
INSERT INTO `category` (`shop_id`, `name`, `sort_order`, `created_at`, `updated_at`, `is_deleted`)
SELECT
    s.id,
    ELT(1 + FLOOR(RAND() * 8), '招牌推荐', '热销菜品', '主食类', '汤羹类', '小吃类', '饮品类', '甜品区', '特色菜'),
    FLOOR(RAND() * 100),
    NOW() - INTERVAL FLOOR(RAND() * 180) DAY,
    NOW(),
    0
FROM `shop` s
         CROSS JOIN (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) t
LIMIT 1500;

SELECT '分类表统计' AS info;
SELECT COUNT(*) AS category_count FROM category;

-- =====================================================
-- 8. 商品表 - 每店铺约 20 个商品
-- =====================================================
INSERT INTO `product` (`shop_id`, `category_id`, `name`, `description`, `images`, `price`, `original_price`, `sales`, `status`, `is_recommend`, `created_at`, `updated_at`, `is_deleted`)
SELECT
    s.id,
    (SELECT c.id FROM category c WHERE c.shop_id = s.id ORDER BY RAND() LIMIT 1),
    CONCAT(ELT(1 + FLOOR(RAND() * 30), '红烧肉', '宫保鸡丁', '鱼香肉丝', '麻婆豆腐', '糖醋排骨', '水煮鱼', '北京烤鸭', '酸菜鱼', '小炒肉', '干煸豆角', '番茄炒蛋', '地三鲜', '回锅肉', '青椒肉丝', '蚂蚁上树', '辣子鸡', '毛血旺', '酸辣土豆丝', '手撕包菜', '蒜蓉西兰花', '干锅花菜', '剁椒鱼头', '清蒸鲈鱼', '口水鸡', '夫妻肺片', '担担面', '酸辣粉', '牛肉面', '扬州炒饭', '蛋炒饭'), '（', FLOOR(RAND() * 30) + 1, '号）'),
    CONCAT('美味', ELT(1 + FLOOR(RAND() * 30), '红烧肉', '宫保鸡丁', '鱼香肉丝', '麻婆豆腐', '糖醋排骨', '水煮鱼', '北京烤鸭', '酸菜鱼', '小炒肉', '干煸豆角', '番茄炒蛋', '地三鲜', '回锅肉', '青椒肉丝', '蚂蚁上树', '辣子鸡', '毛血旺', '酸辣土豆丝', '手撕包菜', '蒜蓉西兰花', '干锅花菜', '剁椒鱼头', '清蒸鲈鱼', '口水鸡', '夫妻肺片', '担担面', '酸辣粉', '牛肉面', '扬州炒饭', '蛋炒饭'), '，地道风味，现点现做'),
    CONCAT('["https://picsum.photos/400/300?random=', FLOOR(RAND() * 10000), '"]'),
    ROUND(15 + RAND() * 85, 2),
    ROUND((15 + RAND() * 85) * 1.2, 2),
    FLOOR(RAND() * 1000),
    CASE WHEN RAND() < 0.9 THEN 1 ELSE 0 END,
    CASE WHEN RAND() < 0.3 THEN 1 ELSE 0 END,
    NOW() - INTERVAL FLOOR(RAND() * 180) DAY,
    NOW(),
    0
FROM `shop` s
         CROSS JOIN (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) t1
         CROSS JOIN (SELECT 1 UNION SELECT 2) t2
LIMIT 6000;

SELECT '商品表统计' AS info;
SELECT COUNT(*) AS product_count FROM product;

-- =====================================================
-- 9. 商品规格表 - 约 60% 商品有规格
-- =====================================================
INSERT INTO `product_sku` (`product_id`, `spec_name`, `spec_value`, `price`, `stock`, `created_at`, `updated_at`)
SELECT
    p.id,
    ELT(1 + FLOOR(RAND() * 3), '口味', '辣度', '分量'),
    ELT(1 + FLOOR(RAND() * 5), '原味', '微辣', '中辣', '特辣', '不辣'),
    p.price * (0.8 + RAND() * 0.5),
    FLOOR(RAND() * 300),
    NOW(),
    NOW()
FROM `product` p
WHERE RAND() < 0.6
LIMIT 3600;

SELECT '规格表统计' AS info;
SELECT COUNT(*) AS sku_count FROM product_sku;

-- =====================================================
-- 10. 购物车表
-- =====================================================
INSERT INTO `cart` (`user_id`, `product_id`, `sku_id`, `quantity`, `checked`, `created_at`, `updated_at`)
SELECT
    u.id,
    p.id,
    NULL,
    FLOOR(1 + RAND() * 3),
    CASE WHEN RAND() < 0.7 THEN 1 ELSE 0 END,
    NOW() - INTERVAL FLOOR(RAND() * 7) DAY,
    NOW()
FROM `user` u
         CROSS JOIN (SELECT id FROM `product` WHERE status = 1 ORDER BY RAND() LIMIT 3) p
WHERE u.role = 'CUSTOMER' AND RAND() < 0.3
LIMIT 3000;

SELECT '购物车表统计' AS info;
SELECT COUNT(*) AS cart_count FROM cart;

-- =====================================================
-- 11. 订单表
-- =====================================================
SET @rownum = 0;
INSERT INTO `order` (`order_no`, `user_id`, `shop_id`, `rider_id`, `total_amount`, `discount_amount`, `delivery_fee`, `actual_amount`, `status`, `pay_status`, `pay_type`, `pay_time`, `remark`, `cancel_reason`, `created_at`, `updated_at`)
SELECT
    CONCAT('ORD', DATE_FORMAT(NOW() - INTERVAL FLOOR(RAND() * 90) DAY, '%Y%m%d'), LPAD(@rownum := @rownum + 1, 8, '0')),
    u.id,
    s.id,
    (SELECT id FROM rider WHERE status = 1 ORDER BY RAND() LIMIT 1),
    ROUND(30 + RAND() * 200, 2),
    CASE WHEN RAND() < 0.3 THEN ROUND(5 + RAND() * 20, 2) ELSE 0 END,
    ROUND(3 + RAND() * 7, 1),
    0,
    ELT(1 + FLOOR(RAND() * 7), 'WAIT_PAY', 'WAIT_ACCEPT', 'WAIT_PICKUP', 'DELIVERING', 'ARRIVED', 'COMPLETED', 'CANCELLED'),
    CASE WHEN RAND() < 0.8 THEN 1 ELSE 0 END,
    ELT(1 + FLOOR(RAND() * 3), 'WECHAT', 'ALIPAY', 'BALANCE'),
    CASE WHEN RAND() < 0.7 THEN NOW() - INTERVAL FLOOR(RAND() * 30) DAY ELSE NULL END,
    CONCAT('备注', FLOOR(RAND() * 100)),
    CASE WHEN RAND() < 0.1 THEN '用户取消' WHEN RAND() < 0.15 THEN '商家取消' ELSE NULL END,
    NOW() - INTERVAL FLOOR(RAND() * 90) DAY,
    NOW()
FROM (SELECT id FROM `user` WHERE role = 'CUSTOMER' LIMIT 2000) u,
     (SELECT id FROM `shop` WHERE status = 1 LIMIT 200) s
LIMIT 5000;

UPDATE `order` SET actual_amount = total_amount - discount_amount + delivery_fee;

SELECT '订单表统计' AS info;
SELECT COUNT(*) AS order_count, status, COUNT(*) FROM `order` GROUP BY status;

-- =====================================================
-- 12. 订单明细表
-- =====================================================
INSERT INTO `order_item` (`order_id`, `product_id`, `sku_id`, `product_name`, `sku_spec`, `price`, `quantity`, `total_amount`, `created_at`)
SELECT
    o.id,
    p.id,
    NULL,
    p.name,
    NULL,
    p.price,
    FLOOR(1 + RAND() * 3),
    p.price * FLOOR(1 + RAND() * 3),
    o.created_at
FROM `order` o
         CROSS JOIN (SELECT id, name, price FROM `product` WHERE status = 1 ORDER BY RAND() LIMIT 2) p
LIMIT 8000;

-- =====================================================
-- 13. 订单配送信息
-- =====================================================
INSERT INTO `order_delivery` (`order_id`, `user_address_id`, `user_contact`, `user_phone`, `delivery_address`, `delivery_lng`, `delivery_lat`, `rider_pick_time`, `rider_arrive_time`, `delivery_distance`, `created_at`, `updated_at`)
SELECT
    o.id,
    a.id,
    a.contact_name,
    a.contact_phone,
    a.detail,
    a.lng,
    a.lat,
    o.created_at + INTERVAL (15 + FLOOR(RAND() * 20)) MINUTE,
    o.created_at + INTERVAL (30 + FLOOR(RAND() * 50)) MINUTE,
    ROUND(1 + RAND() * 8, 1),
    o.created_at,
    NOW()
FROM `order` o
         JOIN `address` a ON a.user_id = o.user_id AND a.is_default = 1
WHERE o.status IN ('DELIVERING', 'ARRIVED', 'COMPLETED')
LIMIT 3500;

-- =====================================================
-- 14. 订单日志
-- =====================================================
INSERT INTO `order_log` (`order_id`, `from_status`, `to_status`, `operator`, `remark`, `created_at`)
SELECT id, NULL, status, 'system', '订单创建', created_at FROM `order`;