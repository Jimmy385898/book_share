package com.bookshare;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.bookshare.mapper")
public class BookShareApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookShareApplication.class, args);
    }
}

