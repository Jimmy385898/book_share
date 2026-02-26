package com.bookshare.controller;

import com.bookshare.common.Result;
import com.bookshare.entity.User;
import com.bookshare.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestAttribute Long userId) {
        try {
            User user = userService.getById(userId);
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PutMapping("/info")
    public Result<String> updateUserInfo(@RequestBody User user, @RequestAttribute Long userId) {
        try {
            user.setId(userId);
            user.setPassword(null);
            userService.updateById(user);
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

