package com.bookshare.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bookshare.entity.Book;
import com.bookshare.entity.BorrowRecord;
import com.bookshare.entity.User;
import com.bookshare.mapper.BorrowRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class BorrowRecordService extends ServiceImpl<BorrowRecordMapper, BorrowRecord> {
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private UserService userService;
    
    @Transactional
    public void applyBorrow(BorrowRecord record) {
        Book book = bookService.getById(record.getBookId());
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }
        
        if (book.getStatus() != 1) {
            throw new RuntimeException("图书当前不可借阅");
        }
        
        record.setLenderId(book.getUserId());
        record.setStatus(0);
        record.setApplyTime(LocalDateTime.now());
        record.setIsOverdue(false);
        record.setRenewCount(0);
        
        this.save(record);
    }
    
    @Transactional
    public void confirmBorrow(Long recordId, Long userId) {
        BorrowRecord record = this.getById(recordId);
        if (record == null) {
            throw new RuntimeException("借阅记录不存在");
        }
        
        if (!record.getLenderId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }
        
        if (record.getStatus() != 0) {
            throw new RuntimeException("当前状态不允许确认");
        }
        
        // 更新借阅记录状态为已确认
        record.setStatus(1);
        record.setConfirmTime(LocalDateTime.now());
        record.setReturnTime(LocalDateTime.now().plusDays(record.getBorrowDays()));
        this.updateById(record);
        
        // 更新图书状态为已借出
        Book book = bookService.getById(record.getBookId());
        book.setStatus(2);
        book.setBorrowCount(book.getBorrowCount() + 1);
        bookService.updateById(book);
    }
    
    @Transactional
    public void rejectBorrow(Long recordId, Long userId) {
        BorrowRecord record = this.getById(recordId);
        if (record == null) {
            throw new RuntimeException("借阅记录不存在");
        }
        
        if (!record.getLenderId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }
        
        record.setStatus(5);
        this.updateById(record);
    }
    
    @Transactional
    public void deliverBook(Long recordId, Long userId) {
        BorrowRecord record = this.getById(recordId);
        if (record == null) {
            throw new RuntimeException("借阅记录不存在");
        }
        
        if (!record.getLenderId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }
        
        if (record.getStatus() != 1) {
            throw new RuntimeException("当前状态不允许发货");
        }
        
        // 更新为借阅中状态
        record.setStatus(2);
        record.setDeliveryTime(LocalDateTime.now());
        this.updateById(record);
    }
    
    /**
     * 借阅者申请归还（第一步）
     */
    @Transactional
    public void applyReturn(Long recordId, Long userId) {
        BorrowRecord record = this.getById(recordId);
        if (record == null) {
            throw new RuntimeException("借阅记录不存在");
        }
        
        if (!record.getBorrowerId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }
        
        if (record.getStatus() != 2) {
            throw new RuntimeException("当前状态不允许归还");
        }
        
        // 更新为待确认归还状态
        record.setStatus(6); // 6-待确认归还
        record.setActualReturnTime(LocalDateTime.now());
        
        // 检查是否逾期
        if (record.getActualReturnTime().isAfter(record.getReturnTime())) {
            record.setIsOverdue(true);
        }
        
        this.updateById(record);
    }
    
    /**
     * 出借方确认归还（第二步）
     */
    @Transactional
    public void confirmReturn(Long recordId, Long userId) {
        BorrowRecord record = this.getById(recordId);
        if (record == null) {
            throw new RuntimeException("借阅记录不存在");
        }
        
        if (!record.getLenderId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }
        
        if (record.getStatus() != 6) {
            throw new RuntimeException("借阅者尚未申请归还");
        }
        
        // 更新为已归还状态
        record.setStatus(3);
        this.updateById(record);
        
        // 恢复图书为可借阅状态
        Book book = bookService.getById(record.getBookId());
        book.setStatus(1);
        bookService.updateById(book);
        
        // 如果逾期，扣除借阅者信用分
        if (record.getIsOverdue()) {
            User borrower = userService.getById(record.getBorrowerId());
            long overdueDays = java.time.Duration.between(record.getReturnTime(), record.getActualReturnTime()).toDays();
            int deductScore = (int) Math.min(overdueDays, 30); // 最多扣30分
            borrower.setCreditScore(Math.max(0, borrower.getCreditScore() - deductScore));
            userService.updateById(borrower);
        }
    }
    
    @Transactional
    public void renewBorrow(Long recordId, Long userId, Integer days) {
        BorrowRecord record = this.getById(recordId);
        if (record == null) {
            throw new RuntimeException("借阅记录不存在");
        }
        
        if (!record.getBorrowerId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }
        
        if (record.getStatus() != 2) {
            throw new RuntimeException("只有借阅中的图书才能续借");
        }
        
        Book book = bookService.getById(record.getBookId());
        if (!book.getAllowRenew()) {
            throw new RuntimeException("该图书不允许续借");
        }
        
        if (record.getRenewCount() >= 2) {
            throw new RuntimeException("续借次数已达上限");
        }
        
        // 续借：延长归还时间
        record.setReturnTime(record.getReturnTime().plusDays(days));
        record.setRenewCount(record.getRenewCount() + 1);
        this.updateById(record);
    }
    
    public IPage<BorrowRecord> getMyBorrowList(Integer page, Integer size, Long userId, Integer type) {
        Page<BorrowRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        
        if (type == 1) {
            wrapper.eq(BorrowRecord::getBorrowerId, userId);
        } else {
            wrapper.eq(BorrowRecord::getLenderId, userId);
        }
        
        wrapper.orderByDesc(BorrowRecord::getCreateTime);
        
        return this.page(pageParam, wrapper);
    }
    
    /**
     * 生成电子借阅协议
     */
    public String generateAgreement(Long recordId) {
        BorrowRecord record = this.getById(recordId);
        if (record == null) {
            throw new RuntimeException("借阅记录不存在");
        }
        
        Book book = bookService.getById(record.getBookId());
        User borrower = userService.getById(record.getBorrowerId());
        User lender = userService.getById(record.getLenderId());
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm");
        
        StringBuilder agreement = new StringBuilder();
        agreement.append("==================== 图书借阅电子协议 ====================\n\n");
        agreement.append("协议编号：BR-").append(record.getId()).append("\n");
        agreement.append("生成时间：").append(LocalDateTime.now().format(formatter)).append("\n\n");
        
        agreement.append("【出借方信息】\n");
        agreement.append("姓名：").append(lender.getRealName() != null ? lender.getRealName() : lender.getUsername()).append("\n");
        agreement.append("联系方式：").append(lender.getPhone()).append("\n\n");
        
        agreement.append("【借阅方信息】\n");
        agreement.append("姓名：").append(borrower.getRealName() != null ? borrower.getRealName() : borrower.getUsername()).append("\n");
        agreement.append("联系方式：").append(borrower.getPhone()).append("\n\n");
        
        agreement.append("【图书信息】\n");
        agreement.append("书名：《").append(book.getTitle()).append("》\n");
        agreement.append("作者：").append(book.getAuthor()).append("\n");
        agreement.append("ISBN：").append(book.getIsbn()).append("\n\n");
        
        agreement.append("【借阅条款】\n");
        agreement.append("1. 借阅期限：").append(record.getBorrowDays()).append("天\n");
        agreement.append("2. 借出时间：").append(record.getConfirmTime().format(formatter)).append("\n");
        agreement.append("3. 应还时间：").append(record.getReturnTime().format(formatter)).append("\n");
        agreement.append("4. 交接方式：").append(getDeliveryMethodText(record.getDeliveryMethod())).append("\n");
        agreement.append("5. 取书地址：").append(book.getPickupAddress()).append("\n");
        agreement.append("6. 是否允许续借：").append(book.getAllowRenew() ? "是" : "否").append("\n");
        agreement.append("7. 是否允许转借：").append(book.getAllowTransfer() ? "否（禁止转借）" : "否").append("\n\n");
        
        agreement.append("【双方责任】\n");
        agreement.append("出借方承诺：\n");
        agreement.append("1. 保证图书来源合法，内容完整无损\n");
        agreement.append("2. 按约定时间和方式交付图书\n");
        agreement.append("3. 不得在借阅期间要求提前归还\n\n");
        
        agreement.append("借阅方承诺：\n");
        agreement.append("1. 妥善保管图书，不得损坏、涂改或遗失\n");
        agreement.append("2. 按时归还图书，如需续借需提前申请\n");
        agreement.append("3. 不得将图书转借他人\n");
        agreement.append("4. 如逾期归还，信用分将受到影响\n\n");
        
        agreement.append("【违约责任】\n");
        agreement.append("1. 图书损坏：按图书原价赔偿\n");
        agreement.append("2. 图书遗失：按图书原价2倍赔偿\n");
        agreement.append("3. 逾期归还：每天扣除信用分1分\n");
        agreement.append("4. 严重违约：平台有权冻结账号\n\n");
        
        agreement.append("【其他说明】\n");
        agreement.append("1. 本协议自双方确认之日起生效\n");
        agreement.append("2. 本协议受平台规则约束\n");
        agreement.append("3. 如有争议，可联系平台客服协调\n\n");
        
        agreement.append("出借方（电子签名）：").append(lender.getUsername()).append("\n");
        agreement.append("借阅方（电子签名）：").append(borrower.getUsername()).append("\n");
        agreement.append("签署时间：").append(record.getConfirmTime().format(formatter)).append("\n\n");
        
        agreement.append("==================== 协议结束 ====================\n");
        
        return agreement.toString();
    }
    
    /**
     * 获取借阅状态文本
     */
    public String getStatusText(Integer status) {
        switch (status) {
            case 0: return "待确认";
            case 1: return "已确认";
            case 2: return "借阅中";
            case 3: return "已归还";
            case 4: return "已取消";
            case 5: return "已拒绝";
            case 6: return "待确认归还";
            default: return "未知状态";
        }
    }
    
    /**
     * 获取交接方式文本
     */
    private String getDeliveryMethodText(String method) {
        switch (method) {
            case "face": return "面对面交接";
            case "pickup": return "社区代收点";
            case "express": return "快递配送";
            default: return method;
        }
    }
    
    /**
     * 检查并更新逾期状态
     */
    @Transactional
    public void checkAndUpdateOverdue() {
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BorrowRecord::getStatus, 2); // 借阅中
        wrapper.eq(BorrowRecord::getIsOverdue, false);
        wrapper.lt(BorrowRecord::getReturnTime, LocalDateTime.now());
        
        List<BorrowRecord> overdueRecords = this.list(wrapper);
        for (BorrowRecord record : overdueRecords) {
            record.setIsOverdue(true);
            this.updateById(record);
            
            // 扣除借阅者信用分
            User borrower = userService.getById(record.getBorrowerId());
            long overdueDays = java.time.Duration.between(record.getReturnTime(), LocalDateTime.now()).toDays();
            int deductScore = (int) Math.min(overdueDays, 30); // 最多扣30分
            borrower.setCreditScore(Math.max(0, borrower.getCreditScore() - deductScore));
            userService.updateById(borrower);
        }
    }
}

