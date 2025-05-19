create table if not exists BookStore.book
(
    id         bigint auto_increment
        primary key,
    title      varchar(255)         null,
    writer     varchar(255)         null,
    isbn       varchar(255)         null,
    cover      varchar(255)         null,
    price      decimal              null,
    cost       decimal              null,
    stock      int                  null,
    created_by varchar(255)         null,
    sales      int                  not null,
    status     tinyint(1) default 1 not null
);

