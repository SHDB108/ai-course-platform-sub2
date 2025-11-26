package com.example.aicourse.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aicourse.entity.StudyProgress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface StudyProgressMapper extends BaseMapper<StudyProgress> {
    
    /**
     * 根据学生ID和课程ID查询学习进度
     */
    @Select("SELECT * FROM t_study_progress WHERE student_id = #{studentId} AND course_id = #{courseId}")
    StudyProgress findByStudentAndCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
    
    /**
     * 查询学生的所有学习进度
     */
    @Select("SELECT * FROM t_study_progress WHERE student_id = #{studentId} ORDER BY gmt_modified DESC")
    List<StudyProgress> findByStudentId(@Param("studentId") Long studentId);
    
    /**
     * 查询课程的学习进度统计
     */
    @Select("SELECT * FROM t_study_progress WHERE course_id = #{courseId} ORDER BY total_progress DESC")
    List<StudyProgress> findByCourseId(@Param("courseId") Long courseId);
    
    /**
     * 查询活跃学习者
     */
    @Select("SELECT * FROM t_study_progress WHERE status = 'ACTIVE' AND last_study_time >= #{sinceTime}")
    List<StudyProgress> findActiveLearners(@Param("sinceTime") LocalDateTime sinceTime);
}