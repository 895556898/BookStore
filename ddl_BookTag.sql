# CREATE TABLE book_tag
# (
#     id  INT AUTO_INCREMENT NOT NULL,
#     bid BIGINT             NULL,
#     tid BIGINT             NULL,
#     CONSTRAINT pk_book_tags PRIMARY KEY (id)
# );

INSERT INTO tag (id, name) VALUES 
(1, '科幻'),
(2, '小说'),
(3, '历史'),
(4, '技术'),
(5, '艺术')
ON DUPLICATE KEY UPDATE name = VALUES(name);