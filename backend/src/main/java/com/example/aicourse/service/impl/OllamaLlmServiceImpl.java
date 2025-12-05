package com.example.aicourse.service.impl;

import com.example.aicourse.service.LlmService;
import com.example.aicourse.vo.task.IntelligentGradeResultVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Primary
@Service
public class OllamaLlmServiceImpl implements LlmService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${llm.ollama.api-url}")
    private String ollamaApiUrl;

    @Value("${llm.ollama.model}")
    private String ollamaModel;

    @Autowired
    public OllamaLlmServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public String generateText(String prompt) {
        String jsonResponse = callOllamaApi(prompt, false);
        try {
            Map<String, Object> ollamaResponse = objectMapper.readValue(jsonResponse, new TypeReference<>() {});

            // Ollama uses "response" field
            Object responseObj = ollamaResponse.get("response");
            return responseObj != null ? responseObj.toString() : "";
        } catch (Exception e) {
            log.error("解析Ollama的文本响应失败: {}", jsonResponse, e);
            return "抱歉，AI助教暂时无法生成建议，请稍后重试。"; // Fallback response
        }
    }

    @Override
    public <T> T generateJson(String prompt, Class<T> responseType) {
        String actualJson = getInnerJsonResponse(prompt);
        try {
            return objectMapper.readValue(actualJson, responseType);
        } catch (Exception e) {
            log.error("解析Ollama的JSON响应失败: {}", actualJson, e);
            throw new RuntimeException("解析LLM的JSON响应失败。", e);
        }
    }

    @Override
    public <T> T generateJson(String prompt, TypeReference<T> responseType) {
        String actualJson = getInnerJsonResponse(prompt);
        try {
            return objectMapper.readValue(actualJson, responseType);
        } catch (Exception e) {
            log.error("解析Ollama的泛型JSON响应失败: {}", actualJson, e);
            throw new RuntimeException("解析LLM的泛型JSON响应失败。", e);
        }
    }

    private String getInnerJsonResponse(String prompt) {
        String jsonResponse = callOllamaApi(prompt, true);
        try {
            Map<String, Object> ollamaResponse = objectMapper.readValue(jsonResponse, new TypeReference<>() {});

            // Ollama uses "response" field
            Object responseObj = ollamaResponse.get("response");
            return responseObj != null ? responseObj.toString() : "{}";
        } catch (Exception e) {
            log.error("从Ollama响应中提取内部JSON失败: {}", jsonResponse, e);
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

    /**
     * 调用Ollama API
     * 按照Ollama规范构造请求体和请求头
     * @param prompt 提示词
     * @param jsonFormat 是否要求返回JSON格式
     * @return Ollama API的响应字符串
     */
    private String callOllamaApi(String prompt, boolean jsonFormat) {
        // 1. 构造请求头：Content-Type: application/json
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 2. 构造Ollama规范的请求体
        Map<String, Object> body = new HashMap<>();
        body.put("model", ollamaModel);        // 模型名称
        body.put("prompt", prompt);            // 用户的prompt
        body.put("stream", false);             // 关键！阻塞模式，等待完整响应

        // 如果需要JSON格式输出，设置format参数
        if (jsonFormat) {
            body.put("format", "json");
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            log.info("正在向Ollama API发送请求: {}, 模型: {}", ollamaApiUrl, ollamaModel);
            log.debug("请求体: {}", body);

            ResponseEntity<String> response = restTemplate.postForEntity(ollamaApiUrl, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.debug("Ollama API响应: {}", response.getBody());
                return response.getBody();
            } else {
                log.error("Ollama API调用失败，状态码: {}，响应体: {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("LLM API调用失败，状态码: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("调用Ollama LLM服务时出错，API URL: {}, 模型: {}", ollamaApiUrl, ollamaModel, e);
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
