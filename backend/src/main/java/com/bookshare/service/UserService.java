package com.bookshare.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bookshare.entity.User;
import com.bookshare.mapper.UserMapper;
import com.bookshare.util.JwtUtil;
import cn.hutool.crypto.digest.DigestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public Map<String, Object> login(String account, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(User::getPhone, account).or().eq(User::getEmail, account));
        wrapper.eq(User::getPassword, DigestUtil.md5Hex(password));
        
        User user = this.getOne(wrapper);
        if (user == null) {
            throw new RuntimeException("账号或密码错误");
        }
        
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        redisTemplate.opsForValue().set("user:token:" + user.getId(), token, 7, TimeUnit.DAYS);
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", user);
        
        return result;
    }
    
    public void register(User user) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, user.getPhone());
        if (this.count(wrapper) > 0) {
            throw new RuntimeException("手机号已被注册");
        }
        
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getEmail, user.getEmail());
            if (this.count(wrapper) > 0) {
                throw new RuntimeException("邮箱已被注册");
            }
        }
        
        user.setPassword(DigestUtil.md5Hex(user.getPassword()));
        user.setRole("user");
        user.setStatus(1);
        user.setCreditScore(100);
        
        this.save(user);
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!DigestUtil.md5Hex(oldPassword).equals(user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        user.setPassword(DigestUtil.md5Hex(newPassword));
        this.updateById(user);
        redisTemplate.delete("user:token:" + userId);
    }
    
    public void logout(Long userId) {
        redisTemplate.delete("user:token:" + userId);
    }
}

