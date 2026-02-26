package com.bookshare.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bookshare.dto.DiscussionDTO;
import com.bookshare.entity.Discussion;
import com.bookshare.entity.DiscussionReply;
import com.bookshare.entity.User;
import com.bookshare.mapper.DiscussionMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiscussionService extends ServiceImpl<DiscussionMapper, Discussion> {
    
    @Autowired
    private DiscussionReplyService replyService;
    
    @Autowired
    private UserService userService;
    
    public IPage<DiscussionDTO> getDiscussionListWithUser(Integer page, Integer size, Long bookId, String topic) {
        Page<Discussion> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Discussion> wrapper = new LambdaQueryWrapper<>();
        
        if (bookId != null) {
            wrapper.eq(Discussion::getBookId, bookId);
        }
        
        if (topic != null && !topic.isEmpty()) {
            wrapper.eq(Discussion::getTopic, topic);
        }
        
        wrapper.eq(Discussion::getStatus, 1);
        wrapper.orderByDesc(Discussion::getCreateTime);
        
        IPage<Discussion> discussionPage = this.page(pageParam, wrapper);
        
        // 转换为DTO并填充用户信息
        Page<DiscussionDTO> dtoPage = new Page<>(page, size);
        dtoPage.setTotal(discussionPage.getTotal());
        
        List<DiscussionDTO> dtoList = discussionPage.getRecords().stream().map(discussion -> {
            DiscussionDTO dto = new DiscussionDTO();
            BeanUtils.copyProperties(discussion, dto);
            
            // 获取用户信息
            User user = userService.getById(discussion.getUserId());
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
    
    public IPage<Discussion> getDiscussionList(Integer page, Integer size, Long bookId, String topic) {
        Page<Discussion> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Discussion> wrapper = new LambdaQueryWrapper<>();
        
        if (bookId != null) {
            wrapper.eq(Discussion::getBookId, bookId);
        }
        
        if (topic != null && !topic.isEmpty()) {
            wrapper.eq(Discussion::getTopic, topic);
        }
        
        wrapper.eq(Discussion::getStatus, 1);
        wrapper.orderByDesc(Discussion::getCreateTime);
        
        return this.page(pageParam, wrapper);
    }
    
    @Transactional
    public void publishDiscussion(Discussion discussion) {
        discussion.setStatus(0);
        discussion.setViewCount(0);
        discussion.setReplyCount(0);
        discussion.setLikeCount(0);
        this.save(discussion);
    }
    
    @Transactional
    public void replyDiscussion(DiscussionReply reply) {
        reply.setStatus(1);
        reply.setLikeCount(0);
        replyService.save(reply);
        
        Discussion discussion = this.getById(reply.getDiscussionId());
        discussion.setReplyCount(discussion.getReplyCount() + 1);
        this.updateById(discussion);
    }
    
    public void viewDiscussion(Long discussionId) {
        Discussion discussion = this.getById(discussionId);
        discussion.setViewCount(discussion.getViewCount() + 1);
        this.updateById(discussion);
    }
}

