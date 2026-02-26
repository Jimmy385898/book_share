package com.bookshare.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewDTO {
    private Long id;
    private Long bookId;
    private Long userId;
    private String content;
    private Integer rating;
    private Integer likeCount;
    private Integer status;
    private LocalDateTime createTime;
    
    // 用户信息
    private String username;
    private String nickname;
    private String avatar;
}

