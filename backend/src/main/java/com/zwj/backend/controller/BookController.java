package com.zwj.backend.controller;

import com.zwj.backend.common.StatusCode;
import com.zwj.backend.entity.Book;
import com.zwj.backend.entity.Result;
import com.zwj.backend.service.BookService;
import com.mybatisflex.core.paginate.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/book")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping("/get/{id}")
    public Result<Book> getBook(@PathVariable Long id) {
        return bookService.getBookById(id);
    }

//    @GetMapping
//    public ResponseEntity<Page<Book>> getBooks(
//
//            @RequestParam(defaultValue = "1") int pageNum,
//            @RequestParam(defaultValue = "10") int pageSize) {
//        return bookService.getBooksByPage(pageNum, pageSize);
//    }

    @GetMapping("/search")
    public Result<Page<Book>> searchBooks(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "") String keyword) {
        return bookService.searchBooks(pageNum, pageSize, keyword);
    }

    @GetMapping("/searchByTags")
    public Result<Page<Book>> searchBookByTagIds(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam List<Long> tids) {
        return bookService.searchBookByTagIds(pageNum, pageSize, tids);
    }

    @PostMapping("/admin/add")
    @PreAuthorize("hasRole('ADMIN')")
    public StatusCode createBook(@RequestBody Book book) {
        return bookService.createBook(book);
    }

    @PutMapping("/admin/update/{title}")
    @PreAuthorize("hasRole('ADMIN')")
    public StatusCode updateBook(@PathVariable String title, @RequestBody Book book) {
        return bookService.updateBook(title, book);
    }

    @DeleteMapping("/admin/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public StatusCode deleteBook(@PathVariable Long id) {
        return bookService.deleteBook(id);
    }
} 