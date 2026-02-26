package com.bookshare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bookshare.entity.Review;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
}

