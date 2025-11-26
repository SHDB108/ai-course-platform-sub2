package com.example.aicourse.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aicourse.entity.LearningRecommendation;
import com.example.aicourse.vo.knowledge.LearningRecommendationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LearningRecommendationMapper extends BaseMapper<LearningRecommendation> {
    /**
     * 【新增】根据学生和课程ID，查询已聚合的、可直接展示的学习推荐列表
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param count 限制查询的数量
     * @return 包含完整信息的学习推荐VO列表
     */
    @Select("SELECT " +
            "r.id AS id, " +
            "r.recommendation_type AS recommendationType, " +
            "r.target_id AS targetId, " +
            "r.reason AS reason, " +
            "CASE " +
            "  WHEN r.recommendation_type = 'KNOWLEDGE_POINT' THEN kp.name " +
            "  ELSE '未知目标' " +
            "END AS targetName " +
            "FROM t_learning_recommendation r " +
            "LEFT JOIN t_knowledge_point kp ON r.target_id = kp.id AND r.recommendation_type = 'KNOWLEDGE_POINT' " +
            "WHERE r.student_id = #{studentId} " +
            "  AND r.course_id = #{courseId} " +
            "  AND r.is_dismissed = 0 " +
            "ORDER BY r.gmt_create DESC " +
            "LIMIT #{limit}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "recommendationType", column = "recommendationType"),
            @Result(property = "targetId", column = "targetId"),
            @Result(property = "reason", column = "reason"),
            @Result(property = "targetName", column = "targetName")
    })
    List<LearningRecommendationVO> selectEnrichedRecommendations(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId,
            @Param("limit") Integer count
    );
}
