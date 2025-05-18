-- 检查字段是否已存在，如果不存在则添加备注字段
ALTER TABLE `orders` 
ADD COLUMN IF NOT EXISTS `remark` varchar(500) DEFAULT NULL COMMENT '订单备注' AFTER `status`;

-- 添加支付方式字段
ALTER TABLE `orders` 
ADD COLUMN IF NOT EXISTS `payment_method` varchar(50) DEFAULT NULL COMMENT '支付方式' AFTER `remark`;

-- 如果你的数据库不支持IF NOT EXISTS语法，也可以使用下面的方式
-- 先检查字段是否存在，如果不存在再添加
/*
SELECT COUNT(*) INTO @exists FROM information_schema.columns 
WHERE table_schema = DATABASE() AND table_name = 'orders' AND column_name = 'remark';

SET @query = IF(@exists = 0, 
    'ALTER TABLE `orders` ADD COLUMN `remark` varchar(500) DEFAULT NULL COMMENT "订单备注" AFTER `status`', 
    'SELECT "Column already exists"');

PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查payment_method字段是否存在
SELECT COUNT(*) INTO @exists FROM information_schema.columns 
WHERE table_schema = DATABASE() AND table_name = 'orders' AND column_name = 'payment_method';

SET @query = IF(@exists = 0, 
    'ALTER TABLE `orders` ADD COLUMN `payment_method` varchar(50) DEFAULT NULL COMMENT "支付方式" AFTER `remark`', 
    'SELECT "Column already exists"');

PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
*/ 