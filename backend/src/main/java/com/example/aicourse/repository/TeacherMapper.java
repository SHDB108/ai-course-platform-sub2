package com.example.aicourse.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aicourse.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TeacherMapper extends BaseMapper<Teacher> {
}
