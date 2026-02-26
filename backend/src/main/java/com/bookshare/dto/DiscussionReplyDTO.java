package com.bookshare.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DiscussionReplyDTO {
    private Long id;
    private Long discussionId;
    private Long userId;
    private Long parentId;
    private String content;
    private Integer likeCount;
    private Integer status;
    private LocalDateTime createTime;
    
    // 用户信息
    private String username;
    private String nickname;
    private String avatar;
    
    // 父回复信息（如果是回复别人的）
    private String parentUsername;
    private String parentNickname;
    
    // 子回复列表
    private List<DiscussionReplyDTO> children;
}

