package com.bookshare.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DiscussionDTO {
    private Long id;
    private Long bookId;
    private Long userId;
    private String title;
    private String content;
    private String topic;
    private Integer viewCount;
    private Integer replyCount;
    private Integer likeCount;
    private Integer status;
    private LocalDateTime createTime;
    
    // 用户信息
    private String username;
    private String nickname;
    private String avatar;
}

