package com.example.aicourse.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aicourse.entity.StudySession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface StudySessionMapper extends BaseMapper<StudySession> {
    
    /**
     * 查询学生的学习会话
     */
    @Select("SELECT * FROM t_study_session WHERE student_id = #{studentId} ORDER BY start_time DESC LIMIT #{limit}")
    List<StudySession> findByStudentId(@Param("studentId") Long studentId, @Param("limit") Integer limit);
    
    /**
     * 查询学生在特定课程的学习会话
     */
    @Select("SELECT * FROM t_study_session WHERE student_id = #{studentId} AND course_id = #{courseId} ORDER BY start_time DESC")
    List<StudySession> findByStudentAndCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
    
    /**
     * 查询指定时间段的学习会话
     */
    @Select("SELECT * FROM t_study_session WHERE student_id = #{studentId} AND start_time >= #{startTime} AND start_time <= #{endTime}")
    List<StudySession> findByTimeRange(@Param("studentId") Long studentId, 
                                      @Param("startTime") LocalDateTime startTime, 
                                      @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计学习时长
     */
    @Select("SELECT COALESCE(SUM(duration), 0) FROM t_study_session WHERE student_id = #{studentId} AND start_time >= #{startTime}")
    Integer sumStudyTimeByStudentSince(@Param("studentId") Long studentId, @Param("startTime") LocalDateTime startTime);
}