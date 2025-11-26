// src/main/java/com/example/aicourse/service/impl/DifyLlmServiceImpl.java
package com.example.aicourse.service.impl;

import com.example.aicourse.service.LlmService;
import com.example.aicourse.vo.task.IntelligentGradeResultVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class DifyLlmServiceImpl implements LlmService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${llm.dify.api-url}")
    private String difyApiUrl;

    @Value("${llm.dify.model}")
    private String difyModel;

    @Autowired
    public DifyLlmServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public String generateText(String prompt) {
        String jsonResponse = callDifyApi(prompt, "text");
        try {
            Map<String, String> difyWrapper = objectMapper.readValue(jsonResponse, new TypeReference<>() {});
            return difyWrapper.get("response");
        } catch (Exception e) {
            log.error("解析Dify的文本响应失败: {}", jsonResponse, e);
            throw new RuntimeException("解析LLM响应失败。", e);
        }
    }

    @Override
    public <T> T generateJson(String prompt, Class<T> responseType) {
        String actualJson = getInnerJsonResponse(prompt);
        try {
            return objectMapper.readValue(actualJson, responseType);
        } catch (Exception e) {
            log.error("解析Dify的JSON响应失败: {}", actualJson, e);
            throw new RuntimeException("解析LLM的JSON响应失败。", e);
        }
    }

    @Override
    public <T> T generateJson(String prompt, TypeReference<T> responseType) {
        String actualJson = getInnerJsonResponse(prompt);
        try {
            return objectMapper.readValue(actualJson, responseType);
        } catch (Exception e) {
            log.error("解析Dify的泛型JSON响应失败: {}", actualJson, e);
            throw new RuntimeException("解析LLM的泛型JSON响应失败。", e);
        }
    }

    private String getInnerJsonResponse(String prompt) {
        String jsonResponse = callDifyApi(prompt, "json");
        try {
            Map<String, String> difyWrapper = objectMapper.readValue(jsonResponse, new TypeReference<>() {});
            return difyWrapper.get("response");
        } catch (Exception e) {
            log.error("从Dify响应中提取内部JSON失败: {}", jsonResponse, e);
            throw new RuntimeException("从LLM响应中提取内部JSON失败。", e);
        }
    }


    @Override
    public IntelligentGradeResultVO gradeShortAnswer(String studentAnswer, String referenceAnswer) {
        if (studentAnswer == null || studentAnswer.isBlank()) {
            IntelligentGradeResultVO resultVO = new IntelligentGradeResultVO();
            resultVO.setScore(BigDecimal.ZERO);
            resultVO.setFeedback("学生未作答。");
            return resultVO;
        }

        String prompt = buildShortAnswerGradingPrompt(studentAnswer, referenceAnswer);
        return generateJson(prompt, IntelligentGradeResultVO.class);
    }

    private String callDifyApi(String prompt, String format) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("model", difyModel);
        body.put("prompt", prompt);
        body.put("stream", false);
        if ("json".equals(format)) {
            body.put("format", "json");
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            log.info("正在向Dify API发送请求: {}", difyApiUrl);
            ResponseEntity<String> response = restTemplate.postForEntity(difyApiUrl, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                log.error("Dify API调用失败，状态码: {}，响应体: {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("LLM API调用失败，状态码: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("调用Dify LLM服务时出错", e);
            throw new RuntimeException("调用LLM服务时出错: " + e.getMessage(), e);
        }
    }

    private String buildShortAnswerGradingPrompt(String studentAnswer, String referenceAnswer) {
        return String.format("""
            你是一位严谨的AI助教，请根据参考答案，对学生的回答进行评分。

            # 参考答案:
            ---
            %s
            ---

            # 学生的回答:
            ---
            %s
            ---

            # 评分要求:
            请判断学生的回答是否覆盖了参考答案中的要点，并评估其准确性。
            严格按照以下JSON格式返回你的批改结果，score字段为0到100之间的数字，代表回答的得分率。
            {
              "score": "你的评分 (0-100)",
              "feedback": "对学生回答的简短评语"
            }
            """, referenceAnswer, studentAnswer);
    }
}