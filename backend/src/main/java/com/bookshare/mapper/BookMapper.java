package com.bookshare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bookshare.entity.Book;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookMapper extends BaseMapper<Book> {
    // 此处无需编写基础SQL方法，BaseMapper已提供
    // 若有复杂查询需求，可在此处自定义方法
}

