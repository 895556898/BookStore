create table order_items
(
    id          bigint auto_increment
        primary key,
    order_id    bigint                             not null,
    book_id     bigint                             not null,
    quantity    int                                not null,
    price       decimal(10, 2)                     not null,
    create_time datetime default CURRENT_TIMESTAMP not null,
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP
);

create index idx_book_id
    on order_items (book_id);

create index idx_order_id
    on order_items (order_id);

