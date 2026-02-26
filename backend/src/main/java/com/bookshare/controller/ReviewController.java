package com.bookshare.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bookshare.common.Result;
import com.bookshare.dto.ReviewDTO;
import com.bookshare.entity.Review;
import com.bookshare.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/review")
public class ReviewController {
    
    @Autowired
    private ReviewService reviewService;
    
    @GetMapping("/list")
    public Result<IPage<ReviewDTO>> getReviewList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long bookId) {
        try {
            IPage<ReviewDTO> result = reviewService.getReviewListWithUser(page, size, bookId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/publish")
    public Result<String> publishReview(@RequestBody Review review, @RequestAttribute Long userId) {
        try {
            review.setUserId(userId);
            reviewService.publishReview(review);
            return Result.success("发布成功，等待审核");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/like/{id}")
    public Result<String> likeReview(@PathVariable Long id, @RequestAttribute Long userId) {
        try {
            reviewService.likeReview(id, userId);
            return Result.success("操作成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

