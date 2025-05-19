create table order_transactions
(
    id               bigint auto_increment
        primary key,
    order_id         bigint                             not null,
    amount           decimal(10, 2)                     not null,
    transaction_time datetime                           not null,
    status           varchar(20)                        not null,
    payment_method   varchar(50)                        not null,
    remark           varchar(255)                       null,
    create_time      datetime default CURRENT_TIMESTAMP not null,
    update_time      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP
);

create index idx_order_id
    on order_transactions (order_id);

create index idx_status
    on order_transactions (status);

create index idx_transaction_time
    on order_transactions (transaction_time);

