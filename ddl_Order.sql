create table orders
(
    id           bigint auto_increment
        primary key,
    user_id      bigint                                not null,
    total_amount decimal(10, 2)                        not null,
    status       varchar(20) default 'PENDING'         not null,
    payment_time datetime                              null,
    create_time  datetime    default CURRENT_TIMESTAMP not null,
    update_time  datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP
);

create index idx_create_time
    on orders (create_time);

create index idx_status
    on orders (status);

create index idx_user_id
    on orders (user_id);



CREATE TABLE IF NOT EXISTS `order_items` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `order_id` INT NOT NULL,
  `book_id` INT NOT NULL,
  `quantity` INT NOT NULL,
  `price` DECIMAL(10, 2) NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_order_id` (`order_id`),
  KEY `idx_book_id` (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `order_transactions` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `order_id` INT NOT NULL,
  `amount` DECIMAL(10, 2) NOT NULL,
  `transaction_time` DATETIME NOT NULL,
  `status` VARCHAR(20) NOT NULL,
  `payment_method` VARCHAR(50) NOT NULL,
  `remark` VARCHAR(255) NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_order_id` (`order_id`),
  KEY `idx_transaction_time` (`transaction_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;