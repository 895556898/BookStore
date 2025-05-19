create table order_addresses
(
    id             bigint auto_increment
        primary key,
    order_id       bigint                               not null comment '订单ID',
    receiver       varchar(50)                          not null comment '收货人',
    phone          varchar(20)                          not null comment '联系电话',
    province       varchar(50)                          null comment '省份',
    city           varchar(50)                          null comment '城市',
    district       varchar(50)                          null comment '区/县',
    detail_address varchar(200)                         not null comment '详细地址',
    zip_code       varchar(10)                          null comment '邮政编码',
    is_default     tinyint(1) default 0                 null comment '是否默认地址',
    create_time    datetime   default CURRENT_TIMESTAMP null,
    update_time    datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
)
    comment '订单地址表';

create index idx_order_id
    on order_addresses (order_id);

