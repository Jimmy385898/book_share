package com.bookshare.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bookshare.common.Result;
import com.bookshare.entity.BorrowRecord;
import com.bookshare.service.BorrowRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/borrow")
public class BorrowController {
    
    @Autowired
    private BorrowRecordService borrowRecordService;
    
    @PostMapping("/apply")
    public Result<String> applyBorrow(@RequestBody BorrowRecord record, @RequestAttribute Long userId) {
        try {
            record.setBorrowerId(userId);
            borrowRecordService.applyBorrow(record);
            return Result.success("申请成功，等待出借方确认");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/confirm/{id}")
    public Result<String> confirmBorrow(@PathVariable Long id, @RequestAttribute Long userId) {
        try {
            borrowRecordService.confirmBorrow(id, userId);
            return Result.success("确认成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/reject/{id}")
    public Result<String> rejectBorrow(@PathVariable Long id, @RequestAttribute Long userId) {
        try {
            borrowRecordService.rejectBorrow(id, userId);
            return Result.success("已拒绝");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/deliver/{id}")
    public Result<String> deliverBook(@PathVariable Long id, @RequestAttribute Long userId) {
        try {
            borrowRecordService.deliverBook(id, userId);
            return Result.success("已发货");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/return/apply/{id}")
    public Result<String> applyReturn(@PathVariable Long id, @RequestAttribute Long userId) {
        try {
            borrowRecordService.applyReturn(id, userId);
            return Result.success("归还申请已提交，等待出借方确认");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/return/confirm/{id}")
    public Result<String> confirmReturn(@PathVariable Long id, @RequestAttribute Long userId) {
        try {
            borrowRecordService.confirmReturn(id, userId);
            return Result.success("归还确认成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/renew/{id}")
    public Result<String> renewBorrow(@PathVariable Long id, @RequestParam Integer days, @RequestAttribute Long userId) {
        try {
            borrowRecordService.renewBorrow(id, userId, days);
            return Result.success("续借成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/my")
    public Result<IPage<BorrowRecord>> getMyBorrowList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam Integer type,
            @RequestAttribute Long userId) {
        try {
            IPage<BorrowRecord> result = borrowRecordService.getMyBorrowList(page, size, userId, type);
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
    
    @GetMapping("/agreement/{id}")
    public Result<String> getAgreement(@PathVariable Long id, @RequestAttribute Long userId) {
        try {
            BorrowRecord record = borrowRecordService.getById(id);
            if (record == null) {
                return Result.error("借阅记录不存在");
            }
            
            // 只有借阅双方可以查看协议
            if (!record.getBorrowerId().equals(userId) && !record.getLenderId().equals(userId)) {
                return Result.error("无权查看该协议");
            }
            
            // 只有已确认的借阅才能生成协议
            if (record.getStatus() < 1) {
                return Result.error("借阅尚未确认，无法生成协议");
            }
            
            String agreement = borrowRecordService.generateAgreement(id);
            return Result.success(agreement);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/cancel/{id}")
    public Result<String> cancelBorrow(@PathVariable Long id, @RequestAttribute Long userId) {
        try {
            BorrowRecord record = borrowRecordService.getById(id);
            if (record == null) {
                return Result.error("借阅记录不存在");
            }
            
            // 只有借阅者可以取消
            if (!record.getBorrowerId().equals(userId)) {
                return Result.error("无权操作");
            }
            
            // 只有待确认状态可以取消
            if (record.getStatus() != 0) {
                return Result.error("当前状态不允许取消");
            }
            
            record.setStatus(4);
            borrowRecordService.updateById(record);
            return Result.success("已取消借阅申请");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

