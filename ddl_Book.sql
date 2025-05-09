CREATE TABLE book
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    title      VARCHAR(255)          NULL,
    writer     VARCHAR(255)          NULL,
    isbn       VARCHAR(255)          NULL,
    cover      VARCHAR(255)          NULL,
    price      DECIMAL               NULL,
    cost       DECIMAL               NULL,
    stock      INT                   NULL,
    created_by VARCHAR(255)          NULL,
    sales      INT                   NOT NULL,
    CONSTRAINT pk_book PRIMARY KEY (id)
);