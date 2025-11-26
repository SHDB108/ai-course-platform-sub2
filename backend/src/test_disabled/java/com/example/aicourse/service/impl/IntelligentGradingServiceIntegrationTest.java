//package com.example.aicourse.service.impl;
//
//import com.example.aicourse.service.IntelligentGradingService;
//import com.example.aicourse.vo.task.IntelligentGradeResultVO;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.client.MockRestServiceServer;
//import org.springframework.web.client.RestTemplate;
//
//import java.math.BigDecimal;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
//import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
//
//// 使用@RestClientTest专注于测试HTTP客户端部分
//// @Import导入我们需要测试的Service实现类
//@SpringBootTest
//@Import(IntelligentGradingServiceImpl.class)
//class IntelligentGradingServiceIntegrationTest {
//
//    @Autowired
//    private IntelligentGradingService intelligentGradingService;
//
//    @Autowired
//    private MockRestServiceServer server;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Value("${llm.dify.api-url}")
//    private String difyApiUrl;
//
//    @Test
//    void gradeShortAnswer_integrationTest() throws Exception {
//        // 1. 准备
//        String studentAnswer = "这是一个集成测试。";
//        String referenceAnswer = "集成测试用于测试多个组件协同工作的情况。";
//
//        // 准备模拟的LLM响应
//        String mockLlmJsonResponse = "{\"score\": \"95\", \"feedback\": \"优秀，完全理解了概念！\"}";
//        String mockDifyResponse = "{\"response\": " + objectMapper.writeValueAsString(mockLlmJsonResponse) + "}";
//
//        // 设置Mock服务器的预期行为
//        this.server.expect(requestTo(difyApiUrl))
//                .andRespond(withSuccess(mockDifyResponse, MediaType.APPLICATION_JSON));
//
//        // 2. 执行
//        IntelligentGradeResultVO result = intelligentGradingService.gradeShortAnswer(studentAnswer, referenceAnswer);
//
//        // 3. 断言
//        assertNotNull(result);
//        assertEquals(0, new BigDecimal("95").compareTo(result.getScore()));
//        assertEquals("优秀，完全理解了概念！", result.getFeedback());
//
//        // 验证所有预期的HTTP请求都已发生
//        this.server.verify();
//    }
//    @TestConfiguration // 可以是 @TestConfiguration，但 @Configuration 也可在测试中生效
//    static class TestConfig {
//        @Bean
//        public MockRestServiceServer mockRestServiceServer(RestTemplate restTemplate) { // Autowire application's RestTemplate
//            return MockRestServiceServer.bindTo(restTemplate).build();
//        }
//    }
//}