//package com.example.aicourse.service.impl;
//
//import com.example.aicourse.vo.task.IntelligentGradeResultVO;
//import com.example.aicourse.service.LlmService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.math.BigDecimal;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class IntelligentGradingServiceImplTest {
//
//    @Mock
//    private LlmService llmService;
//
//    @InjectMocks
//    private IntelligentGradingServiceImpl intelligentGradingService;
//
//    @BeforeEach
//    void setUp() {
//        // 不需要设置字段，因为现在使用依赖注入的LlmService
//    }
//
//    @Test
//    void gradeShortAnswer_shouldReturnCorrectScoreAndFeedback_whenLlmApiSucceeds() throws Exception {
//        // 1. 准备 (Arrange)
//        String studentAnswer = "Spring Boot是一个用于快速开发Java应用的框架。";
//        String referenceAnswer = "Spring Boot是一个开源的、基于Spring的框架，用于创建独立的、生产级的应用程序。";
//
//        // 模拟LLM返回的结果
//        IntelligentGradeResultVO mockResult = new IntelligentGradeResultVO();
//        mockResult.setScore(new BigDecimal("85.5"));
//        mockResult.setFeedback("回答基本正确，但可以更详细。");
//
//        // 当LlmService的gradeShortAnswer方法被调用时，返回我们模拟的结果
//        when(llmService.gradeShortAnswer(anyString(), anyString()))
//                .thenReturn(mockResult);
//
//        // 2. 执行 (Act)
//        IntelligentGradeResultVO result = intelligentGradingService.gradeShortAnswer(studentAnswer, referenceAnswer);
//
//        // 3. 断言 (Assert)
//        assertNotNull(result);
//        assertEquals(0, new BigDecimal("85.5").compareTo(result.getScore()));
//        assertEquals("回答基本正确，但可以更详细。", result.getFeedback());
//    }
//
//    @Test
//    void gradeShortAnswer_shouldReturnZeroScore_whenStudentAnswerIsBlank() {
//        // 1. 准备
//        String studentAnswer = "";
//        String referenceAnswer = "任何参考答案";
//
//        // 模拟LLM返回的结果
//        IntelligentGradeResultVO mockResult = new IntelligentGradeResultVO();
//        mockResult.setScore(BigDecimal.ZERO);
//        mockResult.setFeedback("学生未作答。");
//
//        when(llmService.gradeShortAnswer(anyString(), anyString()))
//                .thenReturn(mockResult);
//
//        // 2. 执行
//        IntelligentGradeResultVO result = intelligentGradingService.gradeShortAnswer(studentAnswer, referenceAnswer);
//
//        // 3. 断言
//        assertNotNull(result);
//        assertEquals(BigDecimal.ZERO, result.getScore());
//        assertEquals("学生未作答。", result.getFeedback());
//    }
//}