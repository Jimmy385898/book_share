package com.bookshare.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bookshare.common.Result;
import com.bookshare.entity.Book;
import com.bookshare.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/book")
public class BookController {
    
    @Autowired
    private BookService bookService;
    
    @GetMapping("/list")
    public Result<IPage<Book>> getBookList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status) {
        try {
            IPage<Book> result = bookService.getBookList(page, size, keyword, category, status);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public Result<Book> getBookDetail(@PathVariable Long id) {
        try {
            Book book = bookService.getById(id);
            return Result.success(book);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/publish")
    public Result<String> publishBook(@RequestBody Book book, @RequestAttribute Long userId) {
        try {
            book.setUserId(userId);
            bookService.publishBook(book);
            return Result.success("发布成功，等待审核");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public Result<String> updateBook(@PathVariable Long id, @RequestBody Book book, @RequestAttribute Long userId) {
        try {
            Book existBook = bookService.getById(id);
            if (!existBook.getUserId().equals(userId)) {
                return Result.error("无权操作");
            }
            book.setId(id);
            bookService.updateById(book);
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public Result<String> deleteBook(@PathVariable Long id, @RequestAttribute Long userId) {
        try {
            Book book = bookService.getById(id);
            if (!book.getUserId().equals(userId)) {
                return Result.error("无权操作");
            }
            bookService.removeById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/follow/{id}")
    public Result<String> followBook(@PathVariable Long id, @RequestAttribute Long userId) {
        try {
            bookService.followBook(id, userId);
            return Result.success("操作成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/follow/status/{id}")
    public Result<Boolean> getFollowStatus(@PathVariable Long id, @RequestAttribute Long userId) {
        try {
            boolean hasFollowed = bookService.hasFollowed(id, userId);
            return Result.success(hasFollowed);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

