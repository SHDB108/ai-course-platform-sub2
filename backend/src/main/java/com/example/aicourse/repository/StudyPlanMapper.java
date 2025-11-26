package com.example.aicourse.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aicourse.entity.StudyPlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StudyPlanMapper extends BaseMapper<StudyPlan> {
    
    /**
     * 查询学生的学习计划
     */
    @Select("SELECT * FROM t_study_plan WHERE student_id = #{studentId} ORDER BY gmt_create DESC")
    List<StudyPlan> findByStudentId(@Param("studentId") Long studentId);
    
    /**
     * 查询学生在特定课程的学习计划
     */
    @Select("SELECT * FROM t_study_plan WHERE student_id = #{studentId} AND course_id = #{courseId}")
    List<StudyPlan> findByStudentAndCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
    
    /**
     * 查询活跃的学习计划
     */
    @Select("SELECT * FROM t_study_plan WHERE student_id = #{studentId} AND status = 'ACTIVE' ORDER BY priority DESC, target_date ASC")
    List<StudyPlan> findActiveByStudentId(@Param("studentId") Long studentId);
    
    /**
     * 查询AI生成的学习计划
     */
    @Select("SELECT * FROM t_study_plan WHERE student_id = #{studentId} AND is_ai_generated = true ORDER BY gmt_create DESC")
    List<StudyPlan> findAiGeneratedByStudentId(@Param("studentId") Long studentId);
}