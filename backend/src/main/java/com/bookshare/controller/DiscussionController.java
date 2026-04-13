package com.bookshare.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bookshare.common.Result;
import com.bookshare.dto.DiscussionDTO;
import com.bookshare.dto.DiscussionReplyDTO;
import com.bookshare.entity.Discussion;
import com.bookshare.entity.DiscussionReply;
import com.bookshare.entity.User;
import com.bookshare.service.DiscussionReplyService;
import com.bookshare.service.DiscussionService;
import com.bookshare.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/discussion")
public class DiscussionController {
    
    @Autowired
    private DiscussionService discussionService;
    
    @Autowired
    private DiscussionReplyService replyService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/list")
    public Result<IPage<DiscussionDTO>> getDiscussionList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) String topic) {
        try {
            IPage<DiscussionDTO> result = discussionService.getDiscussionListWithUser(page, size, bookId, topic);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getDiscussionDetail(@PathVariable Long id) {
        try {
            discussionService.viewDiscussion(id);
            Discussion discussion = discussionService.getById(id);
            
            // 获取发布者信息
            User user = userService.getById(discussion.getUserId());
            
            Map<String, Object> result = new HashMap<>();
            result.put("discussion", discussion);
            result.put("user", user);
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/publish")
    public Result<String> publishDiscussion(@RequestBody Discussion discussion, @RequestAttribute Long userId) {
        try {
            discussion.setUserId(userId);
            discussionService.publishDiscussion(discussion);
            return Result.success("发布成功，等待审核");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/like/{id}")
    public Result<String> likeDiscussion(@PathVariable Long id, @RequestAttribute Long userId) {
        try {
            discussionService.likeDiscussion(id, userId);
            return Result.success("操作成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/reply")
    public Result<String> replyDiscussion(@RequestBody DiscussionReply reply, @RequestAttribute Long userId) {
        try {
            reply.setUserId(userId);
            discussionService.replyDiscussion(reply);
            return Result.success("回复成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/reply/{discussionId}")
    public Result<List<DiscussionReplyDTO>> getReplyList(@PathVariable Long discussionId) {
        try {
            List<DiscussionReplyDTO> result = replyService.getReplyListWithUser(discussionId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

