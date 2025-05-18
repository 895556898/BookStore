package com.zwj.backend.controller;

import com.zwj.backend.entity.Book;
import com.zwj.backend.entity.Result;
import com.zwj.backend.service.BookService;
import com.mybatisflex.core.paginate.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/book")
public class BookController {

    @Autowired
    private BookService bookService;

    //图书详情
    @GetMapping("/get/{id}")
    public Result<Book> getBook(@PathVariable Long id) {
        return bookService.getBookById(id);
    }

    @Value("${file.upload-dir}")
    private String uploadFilePath;

    //上传封面
    @RequestMapping("/cover/upload")
    public Result<String> httpUpload(@RequestParam("files") MultipartFile files){
        String fileName = UUID.randomUUID() + "_" + files.getOriginalFilename();  // 文件名
        File dest = new File(uploadFilePath +'/'+ fileName);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        try {
            files.transferTo(dest);
        } catch (Exception e) {
            return Result.error(400,"上传错误");
        }
        String returnUrl = "http://localhost:3000/covers/" + fileName;
        return Result.success(returnUrl);
    }

    //查找图书
    @GetMapping("/search")
    public Result<Page<Book>> searchBooks(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        return bookService.searchBooks(pageNum, pageSize, keyword, sortBy, sortOrder, minPrice, maxPrice);
    }

    //管理员查找图书
    @GetMapping("/admin/search")
    public Result<Page<Book>> adminSearchBooks(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        return bookService.adminSearchBooks(pageNum, pageSize, keyword, sortBy, sortOrder, minPrice, maxPrice);
    }

    //按标签查找图书
    @GetMapping("/searchByTags")
    public Result<Page<Book>> searchBookByTagIds(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam List<Long> tids) {
        return bookService.searchBookByTagIds(pageNum, pageSize, tids);
    }

    //添加图书
    @PostMapping("/admin/add")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> createBook(@RequestBody Book book) {
        return bookService.createBook(book);
    }

    //更新图书信息
    @PutMapping("/admin/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateBook(@PathVariable Long id, @RequestBody Book book) {
        return bookService.updateBook(id, book);
    }

    //删除图书
    @DeleteMapping("/admin/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteBook(@PathVariable Long id) {
        return bookService.deleteBook(id);
    }
} 