package com.bookshare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("borrow_record")
public class BorrowRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("book_id")
    private Long bookId;
    
    @TableField("borrower_id")
    private Long borrowerId;
    
    @TableField("lender_id")
    private Long lenderId;
    
    @TableField("borrow_days")
    private Integer borrowDays;
    
    @TableField("delivery_method")
    private String deliveryMethod;
    
    @TableField("shipping_fee")
    private Double shippingFee;
    
    @TableField("apply_time")
    private LocalDateTime applyTime;
    
    @TableField("confirm_time")
    private LocalDateTime confirmTime;
    
    @TableField("delivery_time")
    private LocalDateTime deliveryTime;
    
    @TableField("return_time")
    private LocalDateTime returnTime;
    
    @TableField("actual_return_time")
    private LocalDateTime actualReturnTime;
    
    private Integer status;
    
    @TableField("is_overdue")
    private Boolean isOverdue;
    
    @TableField("renew_count")
    private Integer renewCount;
    
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}

