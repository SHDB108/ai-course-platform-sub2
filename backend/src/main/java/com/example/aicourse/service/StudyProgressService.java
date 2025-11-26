package com.example.aicourse.service;

import com.example.aicourse.dto.study.*;
import com.example.aicourse.vo.study.*;
import java.util.List;

/**
 * 学习进度服务接口
 */
public interface StudyProgressService {
    
    /**
     * 获取学生的学习进度
     */
    StudyProgressVO getStudyProgress(Long studentId, Long courseId);
    
    /**
     * 获取学生的所有课程学习进度
     */
    List<StudyProgressVO> getAllStudyProgress(Long studentId);
    
    /**
     * 更新学习进度
     */
    void updateStudyProgress(Long studentId, Long courseId);
    
    /**
     * 获取学习分析报告
     */
    StudyAnalysisVO getStudyAnalysis(Long studentId, Long courseId);
    
    /**
     * 获取学生的整体学习分析
     */
    StudyAnalysisVO getOverallStudyAnalysis(Long studentId);
    
    /**
     * 开始学习会话
     */
    Long startStudySession(Long studentId, StudySessionCreateDTO createDTO);
    
    /**
     * 结束学习会话
     */
    void endStudySession(Long sessionId, StudySessionUpdateDTO updateDTO);
    
    /**
     * 获取学习会话历史
     */
    List<StudySessionVO> getStudySessionHistory(Long studentId, Long courseId, Integer limit);
    
    /**
     * 创建学习计划
     */
    Long createStudyPlan(Long studentId, StudyPlanCreateDTO createDTO);
    
    /**
     * 获取学习计划列表
     */
    List<StudyPlanVO> getStudyPlans(Long studentId, Long courseId);
    
    /**
     * 更新学习计划进度
     */
    void updateStudyPlanProgress(Long planId, Integer progress);
    
    /**
     * 生成AI学习计划
     */
    StudyPlanVO generateAiStudyPlan(Long studentId, Long courseId, String planType);
    
    /**
     * 更新知识点掌握进度
     */
    void updateKnowledgePointProgress(Long studentId, Long knowledgePointId, KnowledgePointProgressUpdateDTO updateDTO);
    
    /**
     * 获取知识点掌握情况
     */
    List<Object> getKnowledgePointProgress(Long studentId, Long courseId);
    
    /**
     * 获取需要复习的知识点
     */
    List<Object> getKnowledgePointsNeedReview(Long studentId);
}