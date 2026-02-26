package com.bookshare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("discussion")
public class Discussion {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("book_id")
    private Long bookId;
    
    @TableField("user_id")
    private Long userId;
    
    private String title;
    private String content;
    private String topic;
    
    @TableField("view_count")
    private Integer viewCount;
    
    @TableField("reply_count")
    private Integer replyCount;
    
    @TableField("like_count")
    private Integer likeCount;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}

