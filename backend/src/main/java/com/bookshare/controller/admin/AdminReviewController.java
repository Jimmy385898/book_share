package com.bookshare.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookshare.common.Result;
import com.bookshare.entity.Review;
import com.bookshare.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/review")
public class AdminReviewController {
    
    @Autowired
    private ReviewService reviewService;
    
    @GetMapping("/list")
    public Result<IPage<Review>> getReviewList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        try {
            Page<Review> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
            
            if (status != null) {
                wrapper.eq(Review::getStatus, status);
            }
            
            wrapper.orderByDesc(Review::getCreateTime);
            
            IPage<Review> result = reviewService.page(pageParam, wrapper);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PutMapping("/audit/{id}")
    public Result<String> auditReview(@PathVariable Long id, @RequestParam Integer status) {
        try {
            Review review = reviewService.getById(id);
            review.setStatus(status);
            reviewService.updateById(review);
            return Result.success("审核成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public Result<String> deleteReview(@PathVariable Long id) {
        try {
            reviewService.removeById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

