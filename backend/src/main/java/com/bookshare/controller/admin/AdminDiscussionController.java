package com.bookshare.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookshare.common.Result;
import com.bookshare.dto.DiscussionReplyDTO;
import com.bookshare.entity.Discussion;
import com.bookshare.entity.DiscussionReply;
import com.bookshare.service.DiscussionReplyService;
import com.bookshare.service.DiscussionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/discussion")
public class AdminDiscussionController {
    
    @Autowired
    private DiscussionService discussionService;
    
    @Autowired
    private DiscussionReplyService replyService;
    
    @GetMapping("/list")
    public Result<IPage<Discussion>> getDiscussionList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        try {
            Page<Discussion> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<Discussion> wrapper = new LambdaQueryWrapper<>();
            if (status != null) {
                wrapper.eq(Discussion::getStatus, status);
            }
            wrapper.orderByDesc(Discussion::getCreateTime);
            
            IPage<Discussion> result = discussionService.page(pageParam, wrapper);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PutMapping("/audit/{id}")
    public Result<String> auditDiscussion(@PathVariable Long id, @RequestParam Integer status) {
        try {
            Discussion discussion = discussionService.getById(id);
            discussion.setStatus(status);
            discussionService.updateById(discussion);
            return Result.success("操作成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public Result<String> deleteDiscussion(@PathVariable Long id) {
        try {
            discussionService.removeById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/reply/list")
    public Result<IPage<DiscussionReplyDTO>> getReplyList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long discussionId,
            @RequestParam(required = false) Integer status) {
        try {
            IPage<DiscussionReplyDTO> result = replyService.getReplyListForAdmin(page, size, discussionId, status);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/reply/{id}")
    public Result<String> deleteReply(@PathVariable Long id) {
        try {
            replyService.removeById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PutMapping("/reply/status/{id}")
    public Result<String> updateReplyStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            DiscussionReply reply = replyService.getById(id);
            reply.setStatus(status);
            replyService.updateById(reply);
            return Result.success("操作成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
