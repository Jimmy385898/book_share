package com.bookshare.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bookshare.dto.DiscussionReplyDTO;
import com.bookshare.entity.DiscussionReply;
import com.bookshare.entity.User;
import com.bookshare.mapper.DiscussionReplyMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DiscussionReplyService extends ServiceImpl<DiscussionReplyMapper, DiscussionReply> {
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取讨论的回复列表（树形结构，类似贴吧）
     */
    public List<DiscussionReplyDTO> getReplyListWithUser(Long discussionId) {
        LambdaQueryWrapper<DiscussionReply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DiscussionReply::getDiscussionId, discussionId);
        wrapper.eq(DiscussionReply::getStatus, 1);
        wrapper.orderByAsc(DiscussionReply::getCreateTime);
        
        List<DiscussionReply> allReplies = this.list(wrapper);
        
        // 转换为DTO并填充用户信息
        List<DiscussionReplyDTO> dtoList = allReplies.stream().map(reply -> {
            DiscussionReplyDTO dto = new DiscussionReplyDTO();
            BeanUtils.copyProperties(reply, dto);
            
            // 获取回复者信息
            User user = userService.getById(reply.getUserId());
            if (user != null) {
                dto.setUsername(user.getUsername());
                dto.setNickname(user.getNickname());
                dto.setAvatar(user.getAvatar());
            }
            
            // 如果是回复别人的，获取被回复者信息
            if (reply.getParentId() != null && reply.getParentId() > 0) {
                DiscussionReply parentReply = this.getById(reply.getParentId());
                if (parentReply != null) {
                    User parentUser = userService.getById(parentReply.getUserId());
                    if (parentUser != null) {
                        dto.setParentUsername(parentUser.getUsername());
                        dto.setParentNickname(parentUser.getNickname());
                    }
                }
            }
            
            return dto;
        }).collect(Collectors.toList());
        
        // 构建树形结构
        return buildReplyTree(dtoList);
    }
    
    /**
     * 构建回复树形结构
     * 采用扁平化结构：顶级回复 + 所有子回复（包括多级嵌套）都显示在同一层级
     */
    private List<DiscussionReplyDTO> buildReplyTree(List<DiscussionReplyDTO> allReplies) {
        // 分组：顶级回复和子回复
        Map<Boolean, List<DiscussionReplyDTO>> grouped = allReplies.stream()
                .collect(Collectors.partitioningBy(reply -> reply.getParentId() == null || reply.getParentId() == 0));
        
        List<DiscussionReplyDTO> topLevelReplies = grouped.get(true);
        List<DiscussionReplyDTO> childReplies = grouped.get(false);
        
        // 为每个顶级回复找到其所有子孙回复（扁平化显示）
        for (DiscussionReplyDTO topReply : topLevelReplies) {
            List<DiscussionReplyDTO> allDescendants = findAllDescendants(topReply.getId(), allReplies);
            topReply.setChildren(allDescendants);
        }
        
        return topLevelReplies;
    }
    
    /**
     * 递归查找某个回复的所有子孙回复（扁平化）
     */
    private List<DiscussionReplyDTO> findAllDescendants(Long parentId, List<DiscussionReplyDTO> allReplies) {
        List<DiscussionReplyDTO> descendants = new ArrayList<>();
        
        // 找到直接子回复
        List<DiscussionReplyDTO> directChildren = allReplies.stream()
                .filter(reply -> parentId.equals(reply.getParentId()))
                .collect(Collectors.toList());
        
        for (DiscussionReplyDTO child : directChildren) {
            descendants.add(child);
            // 递归查找子回复的子回复
            descendants.addAll(findAllDescendants(child.getId(), allReplies));
        }
        
        return descendants;
    }
    
    public List<DiscussionReply> getReplyList(Long discussionId) {
        LambdaQueryWrapper<DiscussionReply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DiscussionReply::getDiscussionId, discussionId);
        wrapper.eq(DiscussionReply::getStatus, 1);
        wrapper.orderByAsc(DiscussionReply::getCreateTime);
        
        return this.list(wrapper);
    }
    
    /**
     * 管理员端获取回复列表（分页）
     */
    public IPage<DiscussionReplyDTO> getReplyListForAdmin(Integer page, Integer size, Long discussionId, Integer status) {
        Page<DiscussionReply> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<DiscussionReply> wrapper = new LambdaQueryWrapper<>();
        
        if (discussionId != null) {
            wrapper.eq(DiscussionReply::getDiscussionId, discussionId);
        }
        if (status != null) {
            wrapper.eq(DiscussionReply::getStatus, status);
        }
        wrapper.orderByDesc(DiscussionReply::getCreateTime);
        
        IPage<DiscussionReply> replyPage = this.page(pageParam, wrapper);
        
        // 转换为DTO
        Page<DiscussionReplyDTO> dtoPage = new Page<>(page, size);
        dtoPage.setTotal(replyPage.getTotal());
        
        List<DiscussionReplyDTO> dtoList = replyPage.getRecords().stream().map(reply -> {
            DiscussionReplyDTO dto = new DiscussionReplyDTO();
            BeanUtils.copyProperties(reply, dto);
            
            // 获取回复者信息
            User user = userService.getById(reply.getUserId());
            if (user != null) {
                dto.setUsername(user.getUsername());
                dto.setNickname(user.getNickname());
                dto.setAvatar(user.getAvatar());
            }
            
            // 如果是回复别人的，获取被回复者信息
            if (reply.getParentId() != null && reply.getParentId() > 0) {
                DiscussionReply parentReply = this.getById(reply.getParentId());
                if (parentReply != null) {
                    User parentUser = userService.getById(parentReply.getUserId());
                    if (parentUser != null) {
                        dto.setParentUsername(parentUser.getUsername());
                        dto.setParentNickname(parentUser.getNickname());
                    }
                }
            }
            
            return dto;
        }).collect(Collectors.toList());
        
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }
}

