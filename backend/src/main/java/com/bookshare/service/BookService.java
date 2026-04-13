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

import java.util.Arrays;

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
        } else {
            wrapper.in(Book::getStatus, Arrays.asList(1, 2));
        }
        
        wrapper.orderByDesc(Book::getCreateTime);
        
        return this.page(pageParam, wrapper);
    }

    public IPage<Book> getMyBookList(Integer page, Integer size, Long userId, String keyword, Integer status) {
        Page<Book> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getUserId, userId);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Book::getTitle, keyword)
                    .or().like(Book::getAuthor, keyword)
                    .or().like(Book::getIsbn, keyword));
        }

        if (status != null) {
            wrapper.eq(Book::getStatus, status);
        }

        wrapper.orderByDesc(Book::getUpdateTime);
        return this.page(pageParam, wrapper);
    }

    public Book getPublicBookDetail(Long id) {
        Book book = this.getById(id);
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }
        if (book.getStatus() != 1 && book.getStatus() != 2) {
            throw new RuntimeException("图书未通过审核或已下架");
        }
        return book;
    }

    public Book getMyBookDetail(Long id, Long userId) {
        Book book = this.getById(id);
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }
        if (!book.getUserId().equals(userId)) {
            throw new RuntimeException("无权查看");
        }
        return book;
    }
    
    public void publishBook(Book book) {
        book.setStatus(0);
        book.setFollowCount(0);
        book.setBorrowCount(0);
        this.save(book);
    }

    public void updateBookByOwner(Long id, Long userId, Book bookForm) {
        Book existBook = this.getById(id);
        if (existBook == null) {
            throw new RuntimeException("图书不存在");
        }
        if (!existBook.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }
        if (existBook.getStatus() == 2) {
            throw new RuntimeException("图书借阅中，暂不支持修改");
        }

        existBook.setCover(bookForm.getCover());
        existBook.setTitle(bookForm.getTitle());
        existBook.setAuthor(bookForm.getAuthor());
        existBook.setPublisher(bookForm.getPublisher());
        existBook.setIsbn(bookForm.getIsbn());
        existBook.setCategory(bookForm.getCategory());
        existBook.setDescription(bookForm.getDescription());
        existBook.setBorrowDays(bookForm.getBorrowDays());
        existBook.setAllowRenew(bookForm.getAllowRenew());
        existBook.setAllowTransfer(bookForm.getAllowTransfer());
        existBook.setPickupAddress(bookForm.getPickupAddress());
        existBook.setDeliveryRange(bookForm.getDeliveryRange());
        existBook.setReturnPoint(bookForm.getReturnPoint());
        existBook.setStatus(0);
        this.updateById(existBook);
    }

    public void deleteBookByOwner(Long id, Long userId) {
        Book book = this.getById(id);
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }
        if (!book.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }
        if (book.getStatus() == 2) {
            throw new RuntimeException("图书借阅中，暂不支持删除");
        }
        this.removeById(id);
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

