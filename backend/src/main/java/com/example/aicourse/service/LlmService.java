// src/main/java/com/example/aicourse/service/LlmService.java
package com.example.aicourse.service;

import com.example.aicourse.vo.task.IntelligentGradeResultVO;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 抽象的大语言模型服务接口
 * 用于封装对底层LLM（如Dify）的调用逻辑，提供统一的服务出口。
 */
public interface LlmService {

    /**
     * 调用LLM生成纯文本内容。
     * @param prompt 提示词
     * @return LLM生成的文本
     */
    String generateText(String prompt);

    /**
     * 调用LLM生成一个特定格式的JSON，并自动映射为指定的Java对象。
     * @param prompt 旨在引导LLM输出JSON的提示词
     * @param responseType 目标Java类的Class对象
     * @param <T> 目标类型
     * @return 解析后的Java对象
     */
    <T> T generateJson(String prompt, Class<T> responseType);

    /**
     * 调用LLM生成一个复杂的、包含泛型的JSON，并自动映射为指定的Java对象。
     * @param prompt 旨在引导LLM输出JSON的提示词
     * @param responseType 目标Java类的TypeReference，用于处理泛型
     * @param <T> 目标类型
     * @return 解析后的Java对象
     */
    <T> T generateJson(String prompt, TypeReference<T> responseType);

    /**
     * 针对简答题进行智能评分的专用方法。
     * @param studentAnswer 学生答案
     * @param referenceAnswer 参考答案
     * @return 包含分数和评语的评分结果
     */
    IntelligentGradeResultVO gradeShortAnswer(String studentAnswer, String referenceAnswer);
}