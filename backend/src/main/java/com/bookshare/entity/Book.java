package com.bookshare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("book")
public class Book {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("user_id")
    private Long userId;
    
    private String title;
    private String author;
    private String publisher;
    private String isbn;
    private String category;
    private String cover;
    private String description;
    
    @TableField("borrow_days")
    private Integer borrowDays;
    
    @TableField("allow_renew")
    private Boolean allowRenew;
    
    @TableField("allow_transfer")
    private Boolean allowTransfer;
    
    @TableField("pickup_address")
    private String pickupAddress;
    
    @TableField("delivery_range")
    private String deliveryRange;
    
    @TableField("return_point")
    private String returnPoint;
    
    @TableField("follow_count")
    private Integer followCount;
    
    @TableField("borrow_count")
    private Integer borrowCount;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}

