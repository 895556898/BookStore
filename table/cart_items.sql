create table cart_items
(
    id          bigint auto_increment
        primary key,
    user_id     bigint                             not null,
    book_id     bigint                             not null,
    quantity    int      default 1                 not null,
    create_time datetime default CURRENT_TIMESTAMP not null,
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP
);

create index idx_book_id
    on cart_items (book_id);

create index idx_user_id
    on cart_items (user_id);

