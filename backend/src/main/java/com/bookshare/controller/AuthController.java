package com.bookshare.controller;

import com.bookshare.common.Result;
import com.bookshare.entity.User;
import com.bookshare.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        try {
            String account = params.get("account");
            String password = params.get("password");
            Map<String, Object> result = userService.login(account, password);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        try {
            userService.register(user);
            return Result.success("注册成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/logout")
    public Result<String> logout(@RequestAttribute Long userId) {
        try {
            userService.logout(userId);
            return Result.success("退出成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

