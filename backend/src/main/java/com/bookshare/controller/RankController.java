package com.bookshare.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bookshare.common.Result;
import com.bookshare.entity.Book;
import com.bookshare.entity.Discussion;
import com.bookshare.entity.Review;
import com.bookshare.entity.User;
import com.bookshare.service.BookService;
import com.bookshare.service.DiscussionService;
import com.bookshare.service.ReviewService;
import com.bookshare.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rank")
public class RankController {
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private DiscussionService discussionService;
    
    @GetMapping("/hot-books")
    public Result<List<Book>> getHotBooks() {
        try {
            LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(Book::getStatus, Arrays.asList(1, 2));
            wrapper.orderByDesc(Book::getBorrowCount);
            wrapper.last("limit 10");
            
            List<Book> books = bookService.list(wrapper);
            return Result.success(books);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/popular-books")
    public Result<List<Book>> getPopularBooks() {
        try {
            LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(Book::getStatus, Arrays.asList(1, 2));
            wrapper.orderByDesc(Book::getFollowCount);
            wrapper.last("limit 10");
            
            List<Book> books = bookService.list(wrapper);
            return Result.success(books);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/active-users")
    public Result<List<Map<String, Object>>> getActiveUsers() {
        try {
            // 获取所有正常状态的用户
            LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.eq(User::getStatus, 1);
            List<User> users = userService.list(userWrapper);
            
            // 获取所有已通过的书评
            LambdaQueryWrapper<Review> reviewWrapper = new LambdaQueryWrapper<>();
            reviewWrapper.eq(Review::getStatus, 1);
            List<Review> reviews = reviewService.list(reviewWrapper);
            
            // 获取所有已通过的讨论
            LambdaQueryWrapper<Discussion> discussionWrapper = new LambdaQueryWrapper<>();
            discussionWrapper.eq(Discussion::getStatus, 1);
            List<Discussion> discussions = discussionService.list(discussionWrapper);
            
            // 统计每个用户的书评和讨论数量
            Map<Long, Integer> reviewCountMap = reviews.stream()
                    .collect(Collectors.groupingBy(Review::getUserId, Collectors.summingInt(r -> 1)));
            
            Map<Long, Integer> discussionCountMap = discussions.stream()
                    .collect(Collectors.groupingBy(Discussion::getUserId, Collectors.summingInt(d -> 1)));
            
            // 计算活跃度并构建结果
            List<Map<String, Object>> result = users.stream().map(user -> {
                int reviewCount = reviewCountMap.getOrDefault(user.getId(), 0);
                int discussionCount = discussionCountMap.getOrDefault(user.getId(), 0);
                int activityScore = reviewCount * 2 + discussionCount * 3; // 讨论权重更高
                
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("username", user.getUsername());
                userMap.put("nickname", user.getNickname());
                userMap.put("avatar", user.getAvatar());
                userMap.put("creditScore", user.getCreditScore());
                userMap.put("reviewCount", reviewCount);
                userMap.put("discussionCount", discussionCount);
                userMap.put("activityScore", activityScore);
                
                return userMap;
            })
            .filter(map -> (int) map.get("activityScore") > 0) // 只显示有活跃度的用户
            .sorted((a, b) -> Integer.compare((int) b.get("activityScore"), (int) a.get("activityScore")))
            .limit(10)
            .collect(Collectors.toList());
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

