package com.bookshare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bookshare.entity.Book;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookMapper extends BaseMapper<Book> {
}

