CREATE TABLE book_tags
(
    id  INT AUTO_INCREMENT NOT NULL,
    bid BIGINT             NULL,
    tid BIGINT             NULL,
    CONSTRAINT pk_book_tags PRIMARY KEY (id)
);