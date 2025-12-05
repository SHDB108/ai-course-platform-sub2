-- Add missing columns to t_knowledge_point_progress table
-- These columns are required by the KnowledgePointProgress entity

ALTER TABLE t_knowledge_point_progress
ADD COLUMN notes TEXT COMMENT 'Study notes',
ADD COLUMN review_count INT DEFAULT 0 COMMENT 'Review count',
ADD COLUMN review_interval INT DEFAULT 1 COMMENT 'Review interval days',
ADD COLUMN review_status VARCHAR(20) DEFAULT 'SCHEDULED' COMMENT 'Review status',
ADD COLUMN next_review_time DATETIME COMMENT 'Next review time';
