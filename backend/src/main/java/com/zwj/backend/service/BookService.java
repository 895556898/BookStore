package com.zwj.backend.service;

import com.zwj.backend.common.StatusCode;
import com.zwj.backend.entity.Book;
import com.zwj.backend.entity.Result;
import com.zwj.backend.entity.Tag;

import com.mybatisflex.core.paginate.Page;

import java.util.List;

public interface BookService {
    Result<Book> getBookById(Long id);
    List<Tag> getTagsByBookId(Long bookId);
//    Page<Book> getBooksByPage(int pageNum, int pageSize);
    Result<Page<Book>> searchBooks(int pageNum, int pageSize, String keyword);
    Result<Page<Book>> searchBookByTagIds(int pageNum, int pageSize, List<Long> tids);
    Result<Void> createBook(Book book);
    Result<Void> updateBook(Long id, Book book);

    Result<Void> deleteBook(Long id);
    void updateStock(Long id, Integer quantity);
}
