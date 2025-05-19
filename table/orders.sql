create table orders
(
    id             bigint auto_increment
        primary key,
    user_id        bigint                                not null,
    total_amount   decimal(10, 2)                        not null,
    status         varchar(20) default 'PENDING'         not null,
    payment_time   datetime                              null,
    create_time    datetime    default CURRENT_TIMESTAMP not null,
    update_time    datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    remark         varchar(500)                          null comment '订单备注',
    payment_method varchar(50)                           null comment '支付方式',
    deleted        tinyint(1)  default 0                 null comment '是否被用户删除，1表示已删除'
);

create index idx_create_time
    on orders (create_time);

create index idx_status
    on orders (status);

create index idx_user_id
    on orders (user_id);

