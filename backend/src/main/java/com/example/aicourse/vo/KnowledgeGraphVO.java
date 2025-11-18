package com.example.aicourse.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 图可视化结构：nodes + edges。
 */
@Data
@NoArgsConstructor
public class KnowledgeGraphVO {
    private List<NodeVO> nodes = new ArrayList<>();
    private List<EdgeVO> edges = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NodeVO {
        private String id;
        private String name;
        private Integer category;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EdgeVO {
        private String source;
        private String target;
        private String label;
    }
}
