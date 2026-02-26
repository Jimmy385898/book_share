package com.bookshare.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bookshare.dto.ReviewDTO;
import com.bookshare.entity.Review;
import com.bookshare.entity.User;
import com.bookshare.mapper.ReviewMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService extends ServiceImpl<ReviewMapper, Review> {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private UserService userService;
    
    public IPage<ReviewDTO> getReviewListWithUser(Integer page, Integer size, Long bookId) {
        Page<Review> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        
        if (bookId != null) {
            wrapper.eq(Review::getBookId, bookId);
        }
        
        wrapper.eq(Review::getStatus, 1);
        wrapper.orderByDesc(Review::getCreateTime);
        
        IPage<Review> reviewPage = this.page(pageParam, wrapper);
        
        // 转换为DTO并填充用户信息
        Page<ReviewDTO> dtoPage = new Page<>(page, size);
        dtoPage.setTotal(reviewPage.getTotal());
        
        List<ReviewDTO> dtoList = reviewPage.getRecords().stream().map(review -> {
            ReviewDTO dto = new ReviewDTO();
            BeanUtils.copyProperties(review, dto);
            
            // 获取用户信息
            User user = userService.getById(review.getUserId());
            if (user != null) {
                dto.setUsername(user.getUsername());
                dto.setNickname(user.getNickname());
                dto.setAvatar(user.getAvatar());
            }
            
            return dto;
        }).collect(Collectors.toList());
        
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }
    
    public IPage<Review> getReviewList(Integer page, Integer size, Long bookId) {
        Page<Review> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        
        if (bookId != null) {
            wrapper.eq(Review::getBookId, bookId);
        }
        
        wrapper.eq(Review::getStatus, 1);
        wrapper.orderByDesc(Review::getCreateTime);
        
        return this.page(pageParam, wrapper);
    }
    
    public void publishReview(Review review) {
        review.setStatus(0);
        review.setLikeCount(0);
        this.save(review);
    }
    
    public void likeReview(Long reviewId, Long userId) {
        String key = "review:like:" + reviewId + ":" + userId;
        Boolean hasLiked = redisTemplate.hasKey(key);
        
        if (Boolean.TRUE.equals(hasLiked)) {
            redisTemplate.delete(key);
            Review review = this.getById(reviewId);
            review.setLikeCount(review.getLikeCount() - 1);
            this.updateById(review);
        } else {
            redisTemplate.opsForValue().set(key, "1");
            Review review = this.getById(reviewId);
            review.setLikeCount(review.getLikeCount() + 1);
            this.updateById(review);
        }
    }
}

