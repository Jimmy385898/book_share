-- 创建数据库
CREATE DATABASE IF NOT EXISTS book_share DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE book_share;

-- 用户表
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `email` VARCHAR(100) COMMENT '邮箱',
  `avatar` VARCHAR(255) COMMENT '头像',
  `nickname` VARCHAR(50) COMMENT '昵称',
  `real_name` VARCHAR(50) COMMENT '真实姓名',
  `address` VARCHAR(255) COMMENT '地址',
  `credit_score` INT DEFAULT 100 COMMENT '信用分',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `role` VARCHAR(20) DEFAULT 'user' COMMENT '角色：user-普通用户，admin-管理员',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 图书表
CREATE TABLE `book` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '图书ID',
  `user_id` BIGINT NOT NULL COMMENT '发布用户ID',
  `title` VARCHAR(200) NOT NULL COMMENT '书名',
  `author` VARCHAR(100) COMMENT '作者',
  `publisher` VARCHAR(100) COMMENT '出版社',
  `isbn` VARCHAR(50) COMMENT 'ISBN',
  `category` VARCHAR(50) COMMENT '分类',
  `cover` VARCHAR(255) COMMENT '封面图片',
  `description` TEXT COMMENT '简介',
  `borrow_days` INT DEFAULT 30 COMMENT '借阅期限（天）',
  `allow_renew` TINYINT DEFAULT 1 COMMENT '是否允许续借：0-否，1-是',
  `allow_transfer` TINYINT DEFAULT 0 COMMENT '是否允许转借：0-否，1-是',
  `pickup_address` VARCHAR(255) COMMENT '取书地址',
  `delivery_range` VARCHAR(255) COMMENT '配送范围',
  `return_point` VARCHAR(255) COMMENT '归还点',
  `follow_count` INT DEFAULT 0 COMMENT '关注人数',
  `borrow_count` INT DEFAULT 0 COMMENT '借阅次数',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-待审核，1-可借阅，2-已借出，3-下架',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书表';

-- 借阅记录表
CREATE TABLE `borrow_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `book_id` BIGINT NOT NULL COMMENT '图书ID',
  `borrower_id` BIGINT NOT NULL COMMENT '借阅者ID',
  `lender_id` BIGINT NOT NULL COMMENT '出借者ID',
  `borrow_days` INT NOT NULL COMMENT '借阅天数',
  `delivery_method` VARCHAR(50) COMMENT '交接方式：face-面对面，pickup-代收点，express-快递',
  `shipping_fee` DECIMAL(10,2) DEFAULT 0 COMMENT '运费',
  `apply_time` DATETIME COMMENT '申请时间',
  `confirm_time` DATETIME COMMENT '确认时间',
  `delivery_time` DATETIME COMMENT '发货时间',
  `return_time` DATETIME COMMENT '应还时间',
  `actual_return_time` DATETIME COMMENT '实际归还时间',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-待确认，1-已确认，2-借阅中，3-已归还，4-已取消，5-已拒绝，6-待确认归还',
  `is_overdue` TINYINT DEFAULT 0 COMMENT '是否逾期：0-否，1-是',
  `renew_count` INT DEFAULT 0 COMMENT '续借次数',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_book_id` (`book_id`),
  KEY `idx_borrower_id` (`borrower_id`),
  KEY `idx_lender_id` (`lender_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借阅记录表';

-- 书评表
CREATE TABLE `review` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '书评ID',
  `book_id` BIGINT NOT NULL COMMENT '图书ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `content` TEXT NOT NULL COMMENT '书评内容',
  `rating` TINYINT COMMENT '评分（1-5）',
  `like_count` INT DEFAULT 0 COMMENT '点赞数',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-待审核，1-已通过，2-已拒绝',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_book_id` (`book_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='书评表';

-- 讨论表
CREATE TABLE `discussion` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '讨论ID',
  `book_id` BIGINT COMMENT '图书ID（可为空，表示通用讨论）',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `content` TEXT NOT NULL COMMENT '内容',
  `topic` VARCHAR(50) COMMENT '主题标签',
  `view_count` INT DEFAULT 0 COMMENT '浏览数',
  `reply_count` INT DEFAULT 0 COMMENT '回复数',
  `like_count` INT DEFAULT 0 COMMENT '点赞数',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-待审核，1-已通过，2-已拒绝',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_book_id` (`book_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_topic` (`topic`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='讨论表';

-- 讨论回复表
CREATE TABLE `discussion_reply` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '回复ID',
  `discussion_id` BIGINT NOT NULL COMMENT '讨论ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `parent_id` BIGINT COMMENT '父回复ID',
  `content` TEXT NOT NULL COMMENT '回复内容',
  `like_count` INT DEFAULT 0 COMMENT '点赞数',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-待审核，1-已通过，2-已拒绝',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_discussion_id` (`discussion_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='讨论回复表';

-- 插入管理员账号（密码：admin123，MD5加密）
INSERT INTO `user` (`username`, `password`, `phone`, `email`, `role`, `nickname`) 
VALUES ('admin', '0192023a7bbd73250516f069df18b500', '13800138000', 'admin@bookshare.com', 'admin', '管理员');

