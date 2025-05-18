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
    Result<Page<Book>> searchBooks(int pageNum, int pageSize, String keyword, String sortBy, String sortOrder, Double minPrice, Double maxPrice);
    Result<Page<Book>> adminSearchBooks(int pageNum, int pageSize, String keyword, String sortBy, String sortOrder, Double minPrice, Double maxPrice);
    Result<Page<Book>> searchBookByTagIds(int pageNum, int pageSize, List<Long> tids);
    Result<Void> createBook(Book book);
    Result<Void> updateBook(Long id, Book book);
    void updateStock(Long id, Integer quantity);
    Result<Void> toggleBookStatus(Long id);
}
