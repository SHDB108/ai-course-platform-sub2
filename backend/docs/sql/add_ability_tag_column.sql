-- ============================================================
-- 学生能力模型雷达图功能 - 数据库迁移脚本
-- ============================================================
-- 功能说明：为知识点表添加能力标签字段，用于学生能力维度分析
-- 能力维度：THEORY(理论), PRACTICE(实践), LOGIC(逻辑), INNOVATION(创新)
-- 创建日期：2025-11-26
-- ============================================================

-- 1. 添加能力标签字段
ALTER TABLE t_knowledge_point
ADD COLUMN ability_tag VARCHAR(20) DEFAULT 'THEORY'
COMMENT '能力标签: THEORY-理论, PRACTICE-实践, LOGIC-逻辑, INNOVATION-创新';

-- 2. 创建索引以优化查询性能
CREATE INDEX idx_ability_tag ON t_knowledge_point(ability_tag);

-- 3. (可选) 根据知识点名称或描述初始化能力标签
-- 以下是示例SQL，可根据实际业务规则调整

-- 3.1 包含"实验"、"实践"、"项目"等关键词的知识点标记为PRACTICE
UPDATE t_knowledge_point
SET ability_tag = 'PRACTICE'
WHERE (name LIKE '%实验%' OR name LIKE '%实践%' OR name LIKE '%项目%'
       OR description LIKE '%实验%' OR description LIKE '%实践%' OR description LIKE '%项目%')
  AND ability_tag = 'THEORY';

-- 3.2 包含"算法"、"推理"、"分析"等关键词的知识点标记为LOGIC
UPDATE t_knowledge_point
SET ability_tag = 'LOGIC'
WHERE (name LIKE '%算法%' OR name LIKE '%推理%' OR name LIKE '%逻辑%' OR name LIKE '%分析%'
       OR description LIKE '%算法%' OR description LIKE '%推理%' OR description LIKE '%逻辑%')
  AND ability_tag = 'THEORY';

-- 3.3 包含"创新"、"设计"、"优化"等关键词的知识点标记为INNOVATION
UPDATE t_knowledge_point
SET ability_tag = 'INNOVATION'
WHERE (name LIKE '%创新%' OR name LIKE '%设计%' OR name LIKE '%优化%' OR name LIKE '%研究%'
       OR description LIKE '%创新%' OR description LIKE '%设计%' OR description LIKE '%优化%')
  AND ability_tag = 'THEORY';

-- 4. 验证数据
-- 查看各能力维度的知识点数量分布
SELECT
    ability_tag,
    COUNT(*) as count,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM t_knowledge_point), 2) as percentage
FROM t_knowledge_point
GROUP BY ability_tag
ORDER BY count DESC;

-- 5. 查看示例数据
SELECT id, name, ability_tag, course_id
FROM t_knowledge_point
LIMIT 20;

-- ============================================================
-- 回滚脚本 (如需回滚，请执行以下语句)
-- ============================================================
-- DROP INDEX idx_ability_tag ON t_knowledge_point;
-- ALTER TABLE t_knowledge_point DROP COLUMN ability_tag;
