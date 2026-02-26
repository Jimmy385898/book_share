package com.bookshare.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookshare.common.Result;
import com.bookshare.entity.BorrowRecord;
import com.bookshare.service.BorrowRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/borrow")
public class AdminBorrowController {
    
    @Autowired
    private BorrowRecordService borrowRecordService;
    
    @GetMapping("/list")
    public Result<IPage<BorrowRecord>> getBorrowList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        try {
            Page<BorrowRecord> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
            
            if (status != null) {
                wrapper.eq(BorrowRecord::getStatus, status);
            }
            
            wrapper.orderByDesc(BorrowRecord::getCreateTime);
            
            IPage<BorrowRecord> result = borrowRecordService.page(pageParam, wrapper);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public Result<BorrowRecord> getBorrowDetail(@PathVariable Long id) {
        try {
            BorrowRecord record = borrowRecordService.getById(id);
            return Result.success(record);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

