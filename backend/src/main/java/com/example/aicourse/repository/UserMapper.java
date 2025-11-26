package com.example.aicourse.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.aicourse.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper 
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 检查用户名是否存在
     */
    default boolean existsByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return selectCount(queryWrapper) > 0;
    }
}
