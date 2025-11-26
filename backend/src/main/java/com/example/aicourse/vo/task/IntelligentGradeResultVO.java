package com.example.aicourse.vo.task;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 智能评分结果视图，承载大语言模型返回的分数与反馈信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntelligentGradeResultVO {
    /**
     * 学生回答的得分（0-100）。
     */
    private BigDecimal score;

    /**
     * 对学生回答的文字点评。
     */
    private String feedback;

    /**
     * 面向学生的改进建议列表，可为空。
     */
    private List<String> suggestions;
}

