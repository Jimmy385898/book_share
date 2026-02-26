package com.bookshare.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookshare.common.Result;
import com.bookshare.entity.Book;
import com.bookshare.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/book")
public class AdminBookController {
    
    @Autowired
    private BookService bookService;
    
    @GetMapping("/list")
    public Result<IPage<Book>> getBookList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        try {
            Page<Book> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
            
            if (status != null) {
                wrapper.eq(Book::getStatus, status);
            }
            
            wrapper.orderByDesc(Book::getCreateTime);
            
            IPage<Book> result = bookService.page(pageParam, wrapper);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PutMapping("/audit/{id}")
    public Result<String> auditBook(@PathVariable Long id, @RequestParam Integer status) {
        try {
            Book book = bookService.getById(id);
            book.setStatus(status);
            bookService.updateById(book);
            return Result.success("审核成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public Result<String> deleteBook(@PathVariable Long id) {
        try {
            bookService.removeById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

