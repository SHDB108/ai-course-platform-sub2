package com.example.aicourse.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.aicourse.client.AssessmentClient;
import com.example.aicourse.client.KnowledgeGraphClient;
import com.example.aicourse.dto.study.*;
import com.example.aicourse.entity.*;
import com.example.aicourse.repository.*;
import com.example.aicourse.service.LlmService;
import com.example.aicourse.service.StudyProgressService;
import com.example.aicourse.service.VideoProgressService;
import com.example.aicourse.vo.PageVO;
import com.example.aicourse.vo.exam.ExamStatisticsVO;
import com.example.aicourse.vo.study.*;
import com.example.aicourse.vo.task.StudentTaskVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StudyProgressServiceImpl implements StudyProgressService {
    private static final String KEY_TOTAL_VIDEOS = "totalVideos";
    private static final String KEY_COMPLETED_VIDEOS = "completedVideos";
    private static final String KEY_AVERAGE_PROGRESS = "averageProgress";
    private static final long TASK_PAGE_SIZE = 200L;
    
    private final StudyProgressMapper studyProgressMapper;
    private final StudySessionMapper studySessionMapper;
    private final StudyPlanMapper studyPlanMapper;
    private final KnowledgePointProgressMapper knowledgePointProgressMapper;
    private final ObjectMapper objectMapper;
    private final KnowledgeGraphClient knowledgeGraphClient;
    private final AssessmentClient assessmentClient;
    private final VideoProgressService videoProgressService;
    private final LlmService llmService;
    
    @Autowired
    public StudyProgressServiceImpl(StudyProgressMapper studyProgressMapper,
                                   StudySessionMapper studySessionMapper,
                                   StudyPlanMapper studyPlanMapper,
                                   KnowledgePointProgressMapper knowledgePointProgressMapper,
                                   ObjectMapper objectMapper,
                                   KnowledgeGraphClient knowledgeGraphClient,
                                   AssessmentClient assessmentClient,
                                   VideoProgressService videoProgressService,
                                   LlmService llmService) {
        this.studyProgressMapper = studyProgressMapper;
        this.studySessionMapper = studySessionMapper;
        this.studyPlanMapper = studyPlanMapper;
        this.knowledgePointProgressMapper = knowledgePointProgressMapper;
        this.objectMapper = objectMapper;
        this.knowledgeGraphClient = knowledgeGraphClient;
        this.assessmentClient = assessmentClient;
        this.videoProgressService = videoProgressService;
        this.llmService = llmService;
    }
    
    @Override
    public StudyProgressVO getStudyProgress(Long studentId, Long courseId) {
        StudyProgress progress = studyProgressMapper.findByStudentAndCourse(studentId, courseId);
        if (progress == null) {
            // 如果没有进度记录，创建一个新的
            progress = createInitialProgress(studentId, courseId);
        }
        
        return convertToStudyProgressVO(progress);
    }
    
    @Override
    public List<StudyProgressVO> getAllStudyProgress(Long studentId) {
        List<StudyProgress> progressList = studyProgressMapper.findByStudentId(studentId);
        return progressList.stream()
                .map(this::convertToStudyProgressVO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void updateStudyProgress(Long studentId, Long courseId) {
        StudyProgress progress = studyProgressMapper.findByStudentAndCourse(studentId, courseId);
        if (progress == null) {
            progress = createInitialProgress(studentId, courseId);
        }
        
        // 计算各模块进度
        calculateVideoProgress(progress, studentId, courseId);
        calculateTaskProgress(progress, studentId, courseId);
        calculateExamProgress(progress, studentId, courseId);
        calculateKnowledgeProgress(progress, studentId, courseId);
        
        // 计算学习时间
        calculateStudyTime(progress, studentId, courseId);
        
        // 计算总体进度
        calculateTotalProgress(progress);
        
        progress.setLastStudyTime(LocalDateTime.now());
        studyProgressMapper.updateById(progress);
    }
    
    @Override
    public StudyAnalysisVO getStudyAnalysis(Long studentId, Long courseId) {
        StudyAnalysisVO analysis = new StudyAnalysisVO();
        analysis.setStudentId(studentId);
        analysis.setCourseId(courseId);
        
        // 获取课程信息
        knowledgeGraphClient.getCourse(courseId)
                .map(Course::getCourseName)
                .ifPresent(analysis::setCourseName);
        
        // 构建各种分析数据
        analysis.setTimeAnalysis(buildTimeAnalysis(studentId, courseId));
        analysis.setProgressAnalysis(buildProgressAnalysis(studentId, courseId));
        analysis.setKnowledgeAnalysis(buildKnowledgeAnalysis(studentId, courseId));
        analysis.setBehaviorAnalysis(buildBehaviorAnalysis(studentId, courseId));
        
        // 生成预测和建议
        analysis.setPredictions(generatePredictions(studentId, courseId));
        analysis.setRecommendations(generateRecommendations(studentId, courseId));
        
        return analysis;
    }
    
    @Override
    public StudyAnalysisVO getOverallStudyAnalysis(Long studentId) {
        StudyAnalysisVO analysis = new StudyAnalysisVO();
        analysis.setStudentId(studentId);
        
        // 获取学生的所有课程进度
        List<StudyProgress> allProgress = studyProgressMapper.findByStudentId(studentId);
        
        // 构建整体统计
        StudyAnalysisVO.OverallStats overallStats = new StudyAnalysisVO.OverallStats();
        overallStats.setTotalCourses(allProgress.size());
        overallStats.setActiveCourses((int) allProgress.stream().filter(p -> "ACTIVE".equals(p.getStatus())).count());
        overallStats.setCompletedCourses((int) allProgress.stream().filter(p -> "COMPLETED".equals(p.getStatus())).count());
        overallStats.setTotalStudyHours(allProgress.stream().mapToInt(p -> p.getTotalStudyTime() != null ? p.getTotalStudyTime() : 0).sum() / 60);
        
        if (!allProgress.isEmpty()) {
            overallStats.setAverageProgress(allProgress.stream().mapToInt(p -> p.getTotalProgress() != null ? p.getTotalProgress() : 0).average().orElse(0.0));
        }
        
        analysis.setOverallStats(overallStats);
        
        return analysis;
    }
    
    @Override
    @Transactional
    public Long startStudySession(Long studentId, StudySessionCreateDTO createDTO) {
        StudySession session = new StudySession();
        session.setStudentId(studentId);
        session.setCourseId(createDTO.getCourseId());
        session.setSessionType(createDTO.getSessionType());
        session.setResourceId(createDTO.getResourceId());
        session.setResourceTitle(createDTO.getResourceTitle());
        session.setStartTime(LocalDateTime.now());
        session.setDeviceType(createDTO.getDeviceType());
        session.setBrowserInfo(createDTO.getBrowserInfo());
        session.setIpAddress(createDTO.getIpAddress());
        session.setNotes(createDTO.getNotes());
        session.setResult("IN_PROGRESS");
        
        studySessionMapper.insert(session);
        return session.getId();
    }
    
    @Override
    @Transactional
    public void endStudySession(Long sessionId, StudySessionUpdateDTO updateDTO) {
        StudySession session = studySessionMapper.selectById(sessionId);
        if (session == null) {
            throw new RuntimeException("Study session not found: " + sessionId);
        }
        
        session.setEndTime(LocalDateTime.now());
        session.setDuration(updateDTO.getDuration());
        session.setEffectiveTime(updateDTO.getEffectiveTime());
        session.setProgressData(updateDTO.getProgressData());
        session.setCompletionRate(updateDTO.getCompletionRate());
        session.setInteractionData(updateDTO.getInteractionData());
        session.setResult(updateDTO.getResult());
        session.setScore(updateDTO.getScore());
        session.setNotes(updateDTO.getNotes());
        session.setFeedback(updateDTO.getFeedback());
        
        studySessionMapper.updateById(session);
        
        // 更新学习进度
        updateStudyProgress(session.getStudentId(), session.getCourseId());
    }
    
    @Override
    public List<StudySessionVO> getStudySessionHistory(Long studentId, Long courseId, Integer limit) {
        List<StudySession> sessions;
        if (courseId != null) {
            sessions = studySessionMapper.findByStudentAndCourse(studentId, courseId);
        } else {
            sessions = studySessionMapper.findByStudentId(studentId, limit != null ? limit : 50);
        }
        
        return sessions.stream()
                .map(this::convertToStudySessionVO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public Long createStudyPlan(Long studentId, StudyPlanCreateDTO createDTO) {
        StudyPlan plan = new StudyPlan();
        plan.setStudentId(studentId);
        plan.setCourseId(createDTO.getCourseId());
        plan.setPlanName(createDTO.getPlanName());
        plan.setDescription(createDTO.getDescription());
        plan.setPlanType(createDTO.getPlanType());
        plan.setStartDate(createDTO.getStartDate());
        plan.setEndDate(createDTO.getEndDate());
        plan.setTargetDate(createDTO.getTargetDate());
        plan.setEstimatedHours(createDTO.getEstimatedHours());
        plan.setPriority(createDTO.getPriority());
        plan.setStatus("ACTIVE");
        plan.setProgress(0);
        plan.setReminderEnabled(createDTO.getReminderEnabled());
        plan.setReminderFrequency(createDTO.getReminderFrequency());
        plan.setReminderTime(createDTO.getReminderTime());
        plan.setIsAiGenerated(false);
        
        // 转换目标和里程碑为JSON
        try {
            if (createDTO.getGoals() != null) {
                plan.setGoals(objectMapper.writeValueAsString(createDTO.getGoals()));
            }
            if (createDTO.getMilestones() != null) {
                plan.setMilestones(objectMapper.writeValueAsString(createDTO.getMilestones()));
            }
        } catch (Exception e) {
            log.error("Error converting goals/milestones to JSON", e);
        }
        
        studyPlanMapper.insert(plan);
        return plan.getId();
    }
    
    @Override
    public List<StudyPlanVO> getStudyPlans(Long studentId, Long courseId) {
        List<StudyPlan> plans;
        if (courseId != null) {
            plans = studyPlanMapper.findByStudentAndCourse(studentId, courseId);
        } else {
            plans = studyPlanMapper.findByStudentId(studentId);
        }
        
        return plans.stream()
                .map(this::convertToStudyPlanVO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void updateStudyPlanProgress(Long planId, Integer progress) {
        StudyPlan plan = studyPlanMapper.selectById(planId);
        if (plan != null) {
            plan.setProgress(progress);
            if (progress >= 100) {
                plan.setStatus("COMPLETED");
            }
            studyPlanMapper.updateById(plan);
        }
    }
    
    @Override
    public StudyPlanVO generateAiStudyPlan(Long studentId, Long courseId, String planType) {
        String normalizedPlanType = (planType == null || planType.isBlank())
                ? "WEEKLY"
                : planType.toUpperCase(Locale.ROOT);
        String courseName = courseId == null
                ? "当前课程"
                : knowledgeGraphClient.getCourse(courseId)
                        .map(Course::getCourseName)
                        .orElse("当前课程");

        String prompt = String.format("""
你是一名智能学习规划助手，需要为学生制定 %s 课程的 %s 学习计划。
请综合考虑课程目标、任务和常见难点，返回严格的JSON对象，字段包含：
{
  "goals": ["目标1","目标2","目标3"],
  "milestones": [
    {"title":"阶段名称","description":"关键学习内容","week":1,"deliverable":"需完成的成果"}
  ],
  "estimatedHours": 20,
  "durationWeeks": 4,
  "rationale": "计划总体说明"
}
仅输出JSON。
""", courseName, normalizedPlanType);

        StudyPlanContent content = null;
        try {
            content = llmService.generateJson(prompt, StudyPlanContent.class);
        } catch (Exception e) {
            log.warn("Failed to generate AI study plan via LLM for student {} course {}", studentId, courseId, e);
        }
        if (content == null) {
            content = fallbackStudyPlanContent(courseName, normalizedPlanType);
        }

        StudyPlan plan = new StudyPlan();
        plan.setStudentId(studentId);
        plan.setCourseId(courseId);
        plan.setPlanType(normalizedPlanType);
        plan.setPlanName(String.format("%s %s学习计划", courseName, normalizedPlanType));
        plan.setDescription(Optional.ofNullable(content.getRationale()).orElse("AI生成的个性化学习计划"));
        plan.setStartDate(LocalDateTime.now());
        int durationWeeks = Optional.ofNullable(content.getDurationWeeks())
                .filter(value -> value > 0)
                .orElse(getDefaultDurationWeeks(normalizedPlanType));
        plan.setTargetDate(plan.getStartDate().plusWeeks(durationWeeks));
        plan.setEndDate(plan.getTargetDate());
        plan.setEstimatedHours(Optional.ofNullable(content.getEstimatedHours()).orElse(durationWeeks * 5));
        plan.setPriority("MEDIUM");
        plan.setStatus("ACTIVE");
        plan.setProgress(0);
        plan.setCompletedTasks(0);
        int milestoneCount = content.getMilestones() == null ? 0 : content.getMilestones().size();
        plan.setTotalTasks(milestoneCount);
        plan.setGoals(toJsonString(content.getGoals()));
        plan.setMilestones(toJsonString(content.getMilestones()));
        plan.setIsAiGenerated(true);
        plan.setAiRecommendReason("LLM根据最新学习进度生成的计划");
        plan.setLastAiUpdate(LocalDateTime.now());
        plan.setReminderEnabled(false);
        plan.setReminderFrequency(null);
        plan.setReminderTime(null);
        
        studyPlanMapper.insert(plan);
        return convertToStudyPlanVO(plan);
    }
    
    @Override
    @Transactional
    public void updateKnowledgePointProgress(Long studentId, Long knowledgePointId, KnowledgePointProgressUpdateDTO updateDTO) {
        KnowledgePointProgress progress = knowledgePointProgressMapper.findByStudentAndKnowledgePoint(studentId, knowledgePointId);
        
        if (progress == null) {
            // 创建新的知识点进度记录
            progress = new KnowledgePointProgress();
            progress.setStudentId(studentId);
            progress.setKnowledgePointId(knowledgePointId);
            
            // 获取知识点的课程ID
            Long kpCourseId = knowledgeGraphClient.getKnowledgePoint(knowledgePointId)
                    .map(KnowledgePoint::getCourseId)
                    .orElse(null);
            if (kpCourseId != null) {
                progress.setCourseId(kpCourseId);
            }
            
            progress.setMasteryLevel("LEARNING");
            progress.setStudyCount(0);
            progress.setTestCount(0);
            progress.setCorrectCount(0);
            progress.setWrongCount(0);
            progress.setTotalStudyTime(0);
            progress.setFirstStudyTime(LocalDateTime.now());
            progress.setReviewInterval(1);
            progress.setReviewCount(0);
            progress.setReviewStatus("SCHEDULED");
        }
        
        // 更新掌握信息
        if (updateDTO.getMasteryLevel() != null) {
            progress.setMasteryLevel(updateDTO.getMasteryLevel());
        }
        if (updateDTO.getMasteryScore() != null) {
            progress.setMasteryScore(updateDTO.getMasteryScore());
        }
        if (updateDTO.getConfidence() != null) {
            progress.setConfidence(updateDTO.getConfidence());
        }
        
        // 更新统计信息
        progress.setStudyCount(progress.getStudyCount() + 1);
        if (updateDTO.getIsCorrect() != null) {
            progress.setTestCount(progress.getTestCount() + 1);
            if (updateDTO.getIsCorrect()) {
                progress.setCorrectCount(progress.getCorrectCount() + 1);
            } else {
                progress.setWrongCount(progress.getWrongCount() + 1);
            }
            
            // 计算正确率
            if (progress.getTestCount() > 0) {
                progress.setAccuracy((double) progress.getCorrectCount() / progress.getTestCount());
            }
        }
        
        if (updateDTO.getStudyTime() != null) {
            progress.setTotalStudyTime(progress.getTotalStudyTime() + updateDTO.getStudyTime());
        }
        
        progress.setLastStudyTime(LocalDateTime.now());
        
        // 检查是否达到掌握标准
        if (progress.getAccuracy() != null && progress.getAccuracy() >= 0.8 && progress.getTestCount() >= 3) {
            if (!"MASTERED".equals(progress.getMasteryLevel()) && !"EXPERT".equals(progress.getMasteryLevel())) {
                progress.setMasteryLevel("MASTERED");
                progress.setMasteredTime(LocalDateTime.now());
            }
        }
        
        // 设置下次复习时间
        if ("MASTERED".equals(progress.getMasteryLevel())) {
            progress.setNextReviewTime(LocalDateTime.now().plusDays(progress.getReviewInterval()));
            progress.setReviewInterval(Math.min(progress.getReviewInterval() * 2, 30)); // 最长30天
        }
        
        if (progress.getId() == null) {
            knowledgePointProgressMapper.insert(progress);
        } else {
            knowledgePointProgressMapper.updateById(progress);
        }
    }
    
    @Override
    public List<Object> getKnowledgePointProgress(Long studentId, Long courseId) {
        return knowledgePointProgressMapper.findByStudentAndCourse(studentId, courseId)
                .stream()
                .map(this::convertToKnowledgePointProgressVO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Object> getKnowledgePointsNeedReview(Long studentId) {
        return knowledgePointProgressMapper.findNeedReviewByStudent(studentId)
                .stream()
                .map(this::convertToKnowledgePointProgressVO)
                .collect(Collectors.toList());
    }
    
    // 私有辅助方法
    
    private StudyProgress createInitialProgress(Long studentId, Long courseId) {
        StudyProgress progress = new StudyProgress();
        progress.setStudentId(studentId);
        progress.setCourseId(courseId);
        progress.setTotalProgress(0);
        progress.setVideoProgress(0);
        progress.setTaskProgress(0);
        progress.setExamProgress(0);
        progress.setKnowledgeProgress(0);
        progress.setTotalStudyTime(0);
        progress.setTodayStudyTime(0);
        progress.setWeekStudyTime(0);
        progress.setMonthStudyTime(0);
        progress.setCompletedVideos(0);
        progress.setCompletedTasks(0);
        progress.setCompletedExams(0);
        progress.setMasteredKnowledge(0);
        progress.setStatus("ACTIVE");
        progress.setStartDate(LocalDateTime.now());
        progress.setLastStudyTime(LocalDateTime.now());
        
        studyProgressMapper.insert(progress);
        return progress;
    }
    
    private void calculateVideoProgress(StudyProgress progress, Long studentId, Long courseId) {
        Map<String, Integer> summary = videoProgressService.getStudentCourseProgressSummary(studentId, courseId);
        int totalVideos = summary.getOrDefault(KEY_TOTAL_VIDEOS, 0);
        int completedVideos = summary.getOrDefault(KEY_COMPLETED_VIDEOS, 0);
        int averageProgress = summary.getOrDefault(KEY_AVERAGE_PROGRESS, 0);

        progress.setTotalVideos(totalVideos);
        progress.setCompletedVideos(completedVideos);
        progress.setVideoProgress(averageProgress);
    }
    
    private void calculateTaskProgress(StudyProgress progress, Long studentId, Long courseId) {
        List<StudentTaskVO> courseTasks = fetchStudentTasksForCourse(studentId, courseId);
        int totalTasks = courseTasks.size();
        int completedTasks = (int) courseTasks.stream()
                .map(StudentTaskVO::getSubmissionStatus)
                .filter(Objects::nonNull)
                .filter(status -> "SUBMITTED".equalsIgnoreCase(status) || "GRADED".equalsIgnoreCase(status))
                .count();

        progress.setTotalTasks(totalTasks);
        progress.setCompletedTasks(completedTasks);
        progress.setTaskProgress(totalTasks > 0 ? (completedTasks * 100 / totalTasks) : 0);
    }

    private List<StudentTaskVO> fetchStudentTasksForCourse(Long studentId, Long courseId) {
        List<StudentTaskVO> tasks = new ArrayList<>();
        long currentPage = 1L;

        while (true) {
            PageVO<StudentTaskVO> page = assessmentClient.findStudentTasks(studentId, currentPage, TASK_PAGE_SIZE, null);
            if (page == null || page.getRecords() == null || page.getRecords().isEmpty()) {
                break;
            }
            tasks.addAll(page.getRecords().stream()
                    .filter(task -> Objects.equals(task.getCourseId(), courseId))
                    .collect(Collectors.toList()));

            long totalPages = page.getPages();
            if ((totalPages > 0 && currentPage >= totalPages) || page.getRecords().size() < TASK_PAGE_SIZE) {
                break;
            }
            currentPage++;
        }

        return tasks;
    }
    
    private void calculateExamProgress(StudyProgress progress, Long studentId, Long courseId) {
        ExamStatisticsVO stats = assessmentClient.getStudentExamStatistics(studentId, courseId)
                .orElse(null);
        if (stats == null) {
            progress.setExamProgress(0);
            progress.setCompletedExams(0);
            progress.setTotalExams(0);
            return;
        }

        int total = Optional.ofNullable(stats.getTotalExams()).orElse(0);
        int completed = Optional.ofNullable(stats.getCompletedExams()).orElse(0);
        progress.setTotalExams(total);
        progress.setCompletedExams(completed);

        if (total > 0) {
            progress.setExamProgress(completed * 100 / total);
        } else {
            progress.setExamProgress(0);
        }
    }
    
    private void calculateKnowledgeProgress(StudyProgress progress, Long studentId, Long courseId) {
        List<KnowledgePointProgress> kpProgress = knowledgePointProgressMapper.findByStudentAndCourse(studentId, courseId);
        long masteredCount = kpProgress.stream()
                .filter(kp -> "MASTERED".equals(kp.getMasteryLevel()) || "EXPERT".equals(kp.getMasteryLevel()))
                .count();
        
        progress.setMasteredKnowledge((int) masteredCount);
        progress.setTotalKnowledge(kpProgress.size());
        
        if (kpProgress.size() > 0) {
            progress.setKnowledgeProgress((int) (masteredCount * 100 / kpProgress.size()));
        } else {
            progress.setKnowledgeProgress(0);
        }
    }
    
    private void calculateStudyTime(StudyProgress progress, Long studentId, Long courseId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime weekStart = now.minusDays(now.getDayOfWeek().getValue() - 1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        
        progress.setTodayStudyTime(studySessionMapper.sumStudyTimeByStudentSince(studentId, todayStart));
        progress.setWeekStudyTime(studySessionMapper.sumStudyTimeByStudentSince(studentId, weekStart));
        progress.setMonthStudyTime(studySessionMapper.sumStudyTimeByStudentSince(studentId, monthStart));
        
        // 计算总学习时间
        Integer totalTime = studySessionMapper.sumStudyTimeByStudentSince(studentId, LocalDateTime.of(1970, 1, 1, 0, 0));
        progress.setTotalStudyTime(totalTime != null ? totalTime : 0);
    }
    
    private void calculateTotalProgress(StudyProgress progress) {
        int totalProgress = 0;
        int moduleCount = 0;
        
        if (progress.getVideoProgress() != null) {
            totalProgress += progress.getVideoProgress();
            moduleCount++;
        }
        if (progress.getTaskProgress() != null) {
            totalProgress += progress.getTaskProgress();
            moduleCount++;
        }
        if (progress.getExamProgress() != null) {
            totalProgress += progress.getExamProgress();
            moduleCount++;
        }
        if (progress.getKnowledgeProgress() != null) {
            totalProgress += progress.getKnowledgeProgress();
            moduleCount++;
        }
        
        if (moduleCount > 0) {
            progress.setTotalProgress(totalProgress / moduleCount);
        } else {
            progress.setTotalProgress(0);
        }
    }
    
    private StudyProgressVO convertToStudyProgressVO(StudyProgress progress) {
        StudyProgressVO vo = new StudyProgressVO();
        vo.setId(progress.getId());
        vo.setStudentId(progress.getStudentId());
        vo.setCourseId(progress.getCourseId());
        vo.setTotalProgress(progress.getTotalProgress());
        vo.setStatus(progress.getStatus());
        vo.setLastStudyTime(progress.getLastStudyTime());
        vo.setStartDate(progress.getStartDate());
        vo.setExpectedEndDate(progress.getExpectedEndDate());
        
        // 获取课程名称
        knowledgeGraphClient.getCourse(progress.getCourseId())
                .map(Course::getCourseName)
                .ifPresent(vo::setCourseName);
        
        // 设置模块进度
        StudyProgressVO.ModuleProgress videoProgress = new StudyProgressVO.ModuleProgress();
        videoProgress.setProgress(progress.getVideoProgress());
        videoProgress.setCompleted(progress.getCompletedVideos());
        videoProgress.setTotal(progress.getTotalVideos());
        vo.setVideoProgress(videoProgress);
        
        StudyProgressVO.ModuleProgress taskProgress = new StudyProgressVO.ModuleProgress();
        taskProgress.setProgress(progress.getTaskProgress());
        taskProgress.setCompleted(progress.getCompletedTasks());
        taskProgress.setTotal(progress.getTotalTasks());
        vo.setTaskProgress(taskProgress);
        
        StudyProgressVO.ModuleProgress examProgress = new StudyProgressVO.ModuleProgress();
        examProgress.setProgress(progress.getExamProgress());
        examProgress.setCompleted(progress.getCompletedExams());
        examProgress.setTotal(progress.getTotalExams());
        vo.setExamProgress(examProgress);
        
        StudyProgressVO.ModuleProgress knowledgeProgress = new StudyProgressVO.ModuleProgress();
        knowledgeProgress.setProgress(progress.getKnowledgeProgress());
        knowledgeProgress.setCompleted(progress.getMasteredKnowledge());
        knowledgeProgress.setTotal(progress.getTotalKnowledge());
        vo.setKnowledgeProgress(knowledgeProgress);
        
        // 设置学习时间统计
        StudyProgressVO.StudyTimeStats timeStats = new StudyProgressVO.StudyTimeStats();
        timeStats.setTotalStudyTime(progress.getTotalStudyTime());
        timeStats.setTodayStudyTime(progress.getTodayStudyTime());
        timeStats.setWeekStudyTime(progress.getWeekStudyTime());
        timeStats.setMonthStudyTime(progress.getMonthStudyTime());
        
        // 计算日均学习时长
        if (progress.getStartDate() != null) {
            long daysSinceStart = ChronoUnit.DAYS.between(progress.getStartDate(), LocalDateTime.now()) + 1;
            if (daysSinceStart > 0) {
                timeStats.setAverageDaily((double) progress.getTotalStudyTime() / daysSinceStart);
            }
        }
        vo.setStudyTimeStats(timeStats);
        
        // 设置完成统计
        StudyProgressVO.CompletionStats completionStats = new StudyProgressVO.CompletionStats();
        completionStats.setCompletedVideos(progress.getCompletedVideos());
        completionStats.setTotalVideos(progress.getTotalVideos());
        completionStats.setCompletedTasks(progress.getCompletedTasks());
        completionStats.setTotalTasks(progress.getTotalTasks());
        completionStats.setCompletedExams(progress.getCompletedExams());
        completionStats.setTotalExams(progress.getTotalExams());
        completionStats.setMasteredKnowledge(progress.getMasteredKnowledge());
        completionStats.setTotalKnowledge(progress.getTotalKnowledge());
        vo.setCompletionStats(completionStats);
        
        return vo;
    }
    
    private StudySessionVO convertToStudySessionVO(StudySession session) {
        StudySessionVO vo = new StudySessionVO();
        vo.setId(session.getId());
        vo.setStudentId(session.getStudentId());
        vo.setCourseId(session.getCourseId());
        vo.setSessionType(session.getSessionType());
        vo.setResourceId(session.getResourceId());
        vo.setResourceTitle(session.getResourceTitle());
        vo.setStartTime(session.getStartTime());
        vo.setEndTime(session.getEndTime());
        vo.setDuration(session.getDuration());
        vo.setEffectiveTime(session.getEffectiveTime());
        vo.setCompletionRate(session.getCompletionRate());
        vo.setResult(session.getResult());
        vo.setScore(session.getScore());
        vo.setNotes(session.getNotes());
        vo.setDeviceType(session.getDeviceType());
        vo.setBrowserInfo(session.getBrowserInfo());
        
        // 获取课程名称
        knowledgeGraphClient.getCourse(session.getCourseId())
                .map(Course::getCourseName)
                .ifPresent(vo::setCourseName);
        
        return vo;
    }
    
    private StudyPlanVO convertToStudyPlanVO(StudyPlan plan) {
        StudyPlanVO vo = new StudyPlanVO();
        vo.setId(plan.getId());
        vo.setStudentId(plan.getStudentId());
        vo.setCourseId(plan.getCourseId());
        vo.setPlanName(plan.getPlanName());
        vo.setDescription(plan.getDescription());
        vo.setPlanType(plan.getPlanType());
        vo.setStartDate(plan.getStartDate());
        vo.setEndDate(plan.getEndDate());
        vo.setTargetDate(plan.getTargetDate());
        vo.setEstimatedHours(plan.getEstimatedHours());
        vo.setStatus(plan.getStatus());
        vo.setProgress(plan.getProgress());
        vo.setPriority(plan.getPriority());
        vo.setCompletedTasks(plan.getCompletedTasks());
        vo.setTotalTasks(plan.getTotalTasks());
        vo.setReminderEnabled(plan.getReminderEnabled());
        vo.setReminderFrequency(plan.getReminderFrequency());
        vo.setReminderTime(plan.getReminderTime());
        vo.setIsAiGenerated(plan.getIsAiGenerated());
        vo.setAiRecommendReason(plan.getAiRecommendReason());
        
        // 获取课程名称
        if (plan.getCourseId() != null) {
            knowledgeGraphClient.getCourse(plan.getCourseId())
                    .map(Course::getCourseName)
                    .ifPresent(vo::setCourseName);
        }
        
        // 计算剩余天数
        if (plan.getTargetDate() != null) {
            long daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), plan.getTargetDate());
            vo.setDaysRemaining((int) Math.max(0, daysRemaining));
        }
        
        // 解析JSON字段
        try {
            if (plan.getGoals() != null) {
                vo.setGoals(objectMapper.readValue(plan.getGoals(), new TypeReference<List<String>>() {}));
            }
            if (plan.getMilestones() != null) {
                vo.setMilestones(objectMapper.readValue(plan.getMilestones(), new TypeReference<List<StudyPlanVO.Milestone>>() {}));
            }
        } catch (Exception e) {
            log.error("Error parsing JSON fields for study plan", e);
        }
        
        return vo;
    }
    
    private Object convertToKnowledgePointProgressVO(KnowledgePointProgress progress) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", progress.getId());
        vo.put("studentId", progress.getStudentId());
        vo.put("knowledgePointId", progress.getKnowledgePointId());
        vo.put("courseId", progress.getCourseId());
        vo.put("masteryLevel", progress.getMasteryLevel());
        vo.put("masteryScore", progress.getMasteryScore());
        vo.put("confidence", progress.getConfidence());
        vo.put("accuracy", progress.getAccuracy());
        vo.put("studyCount", progress.getStudyCount());
        vo.put("totalStudyTime", progress.getTotalStudyTime());
        vo.put("lastStudyTime", progress.getLastStudyTime());
        vo.put("nextReviewTime", progress.getNextReviewTime());
        vo.put("reviewStatus", progress.getReviewStatus());
        
        // 获取知识点名称
        knowledgeGraphClient.getKnowledgePoint(progress.getKnowledgePointId()).ifPresent(kp -> {
            vo.put("knowledgePointName", kp.getName());
            vo.put("knowledgePointDescription", kp.getDescription());
        });
        
        return vo;
    }
    
    // 分析方法的简化实现
    private StudyAnalysisVO.TimeAnalysis buildTimeAnalysis(Long studentId, Long courseId) {
        StudyAnalysisVO.TimeAnalysis analysis = new StudyAnalysisVO.TimeAnalysis();
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime weekStart = now.minusDays(now.getDayOfWeek().getValue() - 1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        
        analysis.setTodayMinutes(studySessionMapper.sumStudyTimeByStudentSince(studentId, todayStart));
        analysis.setWeekMinutes(studySessionMapper.sumStudyTimeByStudentSince(studentId, weekStart));
        analysis.setMonthMinutes(studySessionMapper.sumStudyTimeByStudentSince(studentId, monthStart));
        
        if (analysis.getWeekMinutes() != null) {
            analysis.setDailyAverage(analysis.getWeekMinutes() / 7.0);
        }
        
        return analysis;
    }
    
    private StudyAnalysisVO.ProgressAnalysis buildProgressAnalysis(Long studentId, Long courseId) {
        StudyAnalysisVO.ProgressAnalysis analysis = new StudyAnalysisVO.ProgressAnalysis();
        
        StudyProgress progress = studyProgressMapper.findByStudentAndCourse(studentId, courseId);
        if (progress != null) {
            analysis.setCompletionRate(progress.getTotalProgress() != null ? progress.getTotalProgress().doubleValue() : 0.0);
        }
        
        analysis.setProgressTrend("STEADY");
        return analysis;
    }
    
    private StudyAnalysisVO.KnowledgeAnalysis buildKnowledgeAnalysis(Long studentId, Long courseId) {
        StudyAnalysisVO.KnowledgeAnalysis analysis = new StudyAnalysisVO.KnowledgeAnalysis();
        
        List<KnowledgePointProgress> kpProgress = knowledgePointProgressMapper.findByStudentAndCourse(studentId, courseId);
        analysis.setTotalKnowledgePoints(kpProgress.size());
        
        long masteredCount = kpProgress.stream()
                .filter(kp -> "MASTERED".equals(kp.getMasteryLevel()) || "EXPERT".equals(kp.getMasteryLevel()))
                .count();
        analysis.setMasteredPoints((int) masteredCount);
        
        long learningCount = kpProgress.stream()
                .filter(kp -> "LEARNING".equals(kp.getMasteryLevel()) || "PARTIAL".equals(kp.getMasteryLevel()))
                .count();
        analysis.setLearningPoints((int) learningCount);
        
        if (kpProgress.size() > 0) {
            analysis.setMasteryRate(masteredCount * 100.0 / kpProgress.size());
        }
        
        return analysis;
    }
    
    private StudyAnalysisVO.BehaviorAnalysis buildBehaviorAnalysis(Long studentId, Long courseId) {
        StudyAnalysisVO.BehaviorAnalysis analysis = new StudyAnalysisVO.BehaviorAnalysis();
        
        List<StudySession> sessions = studySessionMapper.findByStudentAndCourse(studentId, courseId);
        if (!sessions.isEmpty()) {
            double avgDuration = sessions.stream().mapToInt(s -> s.getDuration() != null ? s.getDuration() : 0).average().orElse(0.0);
            analysis.setAverageSessionDuration((int) avgDuration);
        }
        
        analysis.setStudyPattern("规律学习");
        analysis.setPreferredStudyTime("晚上");
        analysis.setDevicePreference("PC");
        analysis.setEngagementScore(85.0);
        
        return analysis;
    }
    
    private StudyPlanContent fallbackStudyPlanContent(String courseName, String planType) {
        StudyPlanContent content = new StudyPlanContent();
        content.setGoals(Arrays.asList(
                "巩固" + courseName + "核心概念",
                "完成关键练习任务",
                "总结阶段性成果并复盘"
        ));
        List<StudyPlanContent.Milestone> milestones = new ArrayList<>();

        StudyPlanContent.Milestone milestone1 = new StudyPlanContent.Milestone();
        milestone1.setTitle("理解基础理论");
        milestone1.setDescription("复习课程导学与核心章节，整理知识框架");
        milestone1.setWeek(1);
        milestone1.setDeliverable("输出知识结构思维导图");
        milestones.add(milestone1);

        StudyPlanContent.Milestone milestone2 = new StudyPlanContent.Milestone();
        milestone2.setTitle("完成实践任务");
        milestone2.setDescription("针对重点知识完成对应的实践或作业任务");
        milestone2.setWeek(2);
        milestone2.setDeliverable("提交课程要求的任务并记录难点");
        milestones.add(milestone2);

        StudyPlanContent.Milestone milestone3 = new StudyPlanContent.Milestone();
        milestone3.setTitle("复盘与巩固");
        milestone3.setDescription("整理错题，总结学习心得，并准备测验");
        milestone3.setWeek(3);
        milestone3.setDeliverable("完成一次自测并更新复习清单");
        milestones.add(milestone3);

        content.setMilestones(milestones);
        content.setEstimatedHours(20);
        content.setDurationWeeks(getDefaultDurationWeeks(planType));
        content.setRationale("基于课程结构生成的通用学习规划，可按个人节奏调整。");
        return content;
    }

    private int getDefaultDurationWeeks(String planType) {
        if ("WEEKLY".equalsIgnoreCase(planType)) {
            return 1;
        }
        if ("MONTHLY".equalsIgnoreCase(planType)) {
            return 4;
        }
        return 4;
    }

    private String toJsonString(Object value) {
        try {
            if (value == null) {
                return objectMapper.writeValueAsString(Collections.emptyList());
            }
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize study plan content", e);
            return "[]";
        }
    }

    private List<String> generatePredictions(Long studentId, Long courseId) {
        return Arrays.asList(
                "按当前进度，预计在2周内完成本课程",
                "建议加强知识点练习以提高掌握度",
                "保持当前学习节奏可以达到优秀水平"
        );
    }
    
    private List<String> generateRecommendations(Long studentId, Long courseId) {
        return Arrays.asList(
                "建议每天学习30-45分钟以保持学习连续性",
                "重点关注薄弱知识点的练习",
                "可以尝试制定详细的学习计划"
        );
    }

    @Data
    public static class StudyPlanContent {
        private List<String> goals;
        private List<Milestone> milestones;
        private Integer estimatedHours;
        private Integer durationWeeks;
        private String rationale;

        @Data
        public static class Milestone {
            private String title;
            private String description;
            private Integer week;
            private String deliverable;
        }
    }
}
