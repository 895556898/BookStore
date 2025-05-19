create table user
(
    id       bigint unsigned auto_increment
        primary key,
    username varchar(255) not null,
    password varchar(255) not null,
    phone    varchar(255) not null,
    role     varchar(255) null
);

