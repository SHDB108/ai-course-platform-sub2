package com.example.aicourse.service;

import com.example.aicourse.vo.MyDashboardVO;

import java.util.List;

/**
 * 能力模型服务接口
 */
public interface CapabilityService {

    /**
     * 计算并获取学生的能力分数
     * @param studentId 学生ID
     * @return 能力分数列表
     */
    List<MyDashboardVO.CapabilityScoreVO> calculateAndGetCapabilityScores(Long studentId);
}
