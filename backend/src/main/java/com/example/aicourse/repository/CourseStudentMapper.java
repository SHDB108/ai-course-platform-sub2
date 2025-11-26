package com.example.aicourse.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aicourse.entity.CourseStudent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourseStudentMapper extends BaseMapper<CourseStudent> {
}
