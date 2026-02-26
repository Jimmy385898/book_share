package com.bookshare.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bookshare.entity.Book;
import com.bookshare.mapper.BookMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class BookService extends ServiceImpl<BookMapper, Book> {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public IPage<Book> getBookList(Integer page, Integer size, String keyword, String category, Integer status) {
        Page<Book> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Book::getTitle, keyword)
                    .or().like(Book::getAuthor, keyword)
                    .or().like(Book::getIsbn, keyword));
        }
        
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Book::getCategory, category);
        }
        
        if (status != null) {
            wrapper.eq(Book::getStatus, status);
        }
        
        wrapper.orderByDesc(Book::getCreateTime);
        
        return this.page(pageParam, wrapper);
    }
    
    public void publishBook(Book book) {
        book.setStatus(0);
        book.setFollowCount(0);
        book.setBorrowCount(0);
        this.save(book);
    }
    
    public void followBook(Long bookId, Long userId) {
        String key = "book:follow:" + bookId + ":" + userId;
        Boolean hasFollowed = redisTemplate.hasKey(key);
        
        if (Boolean.TRUE.equals(hasFollowed)) {
            redisTemplate.delete(key);
            Book book = this.getById(bookId);
            book.setFollowCount(book.getFollowCount() - 1);
            this.updateById(book);
        } else {
            redisTemplate.opsForValue().set(key, "1");
            Book book = this.getById(bookId);
            book.setFollowCount(book.getFollowCount() + 1);
            this.updateById(book);
        }
    }
    
    public boolean hasFollowed(Long bookId, Long userId) {
        String key = "book:follow:" + bookId + ":" + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}

