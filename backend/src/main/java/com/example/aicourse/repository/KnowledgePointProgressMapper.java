package com.example.aicourse.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aicourse.entity.KnowledgePointProgress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface KnowledgePointProgressMapper extends BaseMapper<KnowledgePointProgress> {
    
    /**
     * 查询学生的知识点掌握进度
     */
    @Select("SELECT * FROM t_knowledge_point_progress WHERE student_id = #{studentId} AND course_id = #{courseId}")
    List<KnowledgePointProgress> findByStudentAndCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
    
    /**
     * 查询特定知识点的掌握情况
     */
    @Select("SELECT * FROM t_knowledge_point_progress WHERE student_id = #{studentId} AND knowledge_point_id = #{knowledgePointId}")
    KnowledgePointProgress findByStudentAndKnowledgePoint(@Param("studentId") Long studentId, @Param("knowledgePointId") Long knowledgePointId);
    
    /**
     * 查询已掌握的知识点
     */
    @Select("SELECT * FROM t_knowledge_point_progress WHERE student_id = #{studentId} AND mastery_level IN ('MASTERED', 'EXPERT')")
    List<KnowledgePointProgress> findMasteredByStudent(@Param("studentId") Long studentId);
    
    /**
     * 查询需要复习的知识点
     */
    @Select("SELECT * FROM t_knowledge_point_progress WHERE student_id = #{studentId} AND review_status = 'SCHEDULED' AND next_review_time <= NOW()")
    List<KnowledgePointProgress> findNeedReviewByStudent(@Param("studentId") Long studentId);
    
    /**
     * 统计课程知识点掌握情况
     */
    @Select("SELECT mastery_level, COUNT(*) as count FROM t_knowledge_point_progress WHERE student_id = #{studentId} AND course_id = #{courseId} GROUP BY mastery_level")
    List<Object> getMasteryStatsByStudentAndCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
}