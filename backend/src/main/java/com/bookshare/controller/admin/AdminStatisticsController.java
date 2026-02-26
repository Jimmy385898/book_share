package com.bookshare.controller.admin;

import com.bookshare.common.Result;
import com.bookshare.entity.Book;
import com.bookshare.entity.BorrowRecord;
import com.bookshare.entity.User;
import com.bookshare.service.BookService;
import com.bookshare.service.BorrowRecordService;
import com.bookshare.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/statistics")
public class AdminStatisticsController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private BorrowRecordService borrowRecordService;
    
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        try {
            Map<String, Object> data = new HashMap<>();
            
            long userCount = userService.count();
            long bookCount = bookService.count();
            long borrowCount = borrowRecordService.count();
            
            data.put("userCount", userCount);
            data.put("bookCount", bookCount);
            data.put("borrowCount", borrowCount);
            
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

