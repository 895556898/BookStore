package com.zwj.backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.mybatisflex.core.BaseMapper;
import com.zwj.backend.entity.Book;

@Mapper
public interface BookMapper extends BaseMapper<Book> {
    Book selectOneByTitle(String title);

    @Select("SELECT * FROM book WHERE id = #{id} FOR UPDATE")
    Book selectForUpdate(Long id);
}
