package com.example.aicourse.repository;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.aicourse.entity.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StudentMapper extends BaseMapper<Student>{
    /**
     * 根据课程ID，分页查询已选课的学生列表
     * @param page 分页对象
     * @param courseId 课程ID
     * @return 学生分页结果
     */
    @Select("SELECT s.* FROM t_student s " +
            "JOIN t_course_enrollment ce ON s.id = ce.student_id " +
            "WHERE ce.course_id = #{courseId}")
    Page<Student> selectStudentsByCourse(Page<Student> page, @Param("courseId") Long courseId);
}