package com.zwj.backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.mybatisflex.core.BaseMapper;
import com.zwj.backend.entity.Book;

@Mapper
public interface BookMapper extends BaseMapper<Book> {
    Book selectOneByTitle(String title);
}
