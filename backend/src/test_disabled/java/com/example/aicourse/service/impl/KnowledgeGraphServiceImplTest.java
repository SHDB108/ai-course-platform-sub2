//package com.example.aicourse.service.impl;
//
//import com.example.aicourse.repository.KnowledgePointMapper;
//import com.example.aicourse.repository.KnowledgePointRelationMapper;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class KnowledgeGraphServiceImplTest {
//
//    @Mock
//    private KnowledgePointMapper knowledgePointMapper;
//
//    @Mock
//    private KnowledgePointRelationMapper relationMapper;
//
//    @InjectMocks
//    private KnowledgeGraphServiceImpl knowledgeGraphService;
//
//    @Test
//    void saveGraphFromMap_shouldParseAndSaveNodesAndEdgesCorrectly() throws Exception {
//        // 1. 准备测试数据
//        Long courseId = 1L;
//
//        // 创建模拟的图数据
//        Map<String, List<Map<String, String>>> graphData = new HashMap<>();
//
//        // 节点数据
//        List<Map<String, String>> nodes = Arrays.asList(
//            createNode("Spring Boot", "核心框架"),
//            createNode("Auto-Configuration", "自动配置")
//        );
//
//        // 边数据
//        List<Map<String, String>> edges = Arrays.asList(
//            createEdge("Auto-Configuration", "Spring Boot", "PART_OF")
//        );
//
//        graphData.put("nodes", nodes);
//        graphData.put("edges", edges);
//
//        // 使用ArgumentCaptor捕获传递给mapper的参数
//        ArgumentCaptor<com.example.aicourse.entity.KnowledgePoint> kpCaptor = ArgumentCaptor.forClass(com.example.aicourse.entity.KnowledgePoint.class);
//        ArgumentCaptor<com.example.aicourse.entity.KnowledgePointRelation> relCaptor = ArgumentCaptor.forClass(com.example.aicourse.entity.KnowledgePointRelation.class);
//
//        // 模拟mapper.insert的行为，以便我们可以捕获ID
//        when(knowledgePointMapper.insert(kpCaptor.capture())).thenAnswer(invocation -> {
//            com.example.aicourse.entity.KnowledgePoint kp = invocation.getArgument(0);
//            // 模拟数据库生成ID
//            if ("Spring Boot".equals(kp.getName())) {
//                kp.setId(101L);
//            } else {
//                kp.setId(102L);
//            }
//            return 1;
//        });
//
//        // 2. 执行
//        knowledgeGraphService.saveGraphFromMap(courseId, graphData);
//
//        // 3. 断言
//        // 验证KnowledgePointMapper.insert被调用了两次
//        verify(knowledgePointMapper, times(2)).insert(any());
//        // 验证KnowledgePointRelationMapper.insert被调用了一次
//        verify(relationMapper, times(1)).insert(relCaptor.capture());
//
//        // 验证捕获到的节点数据
//        assertEquals(2, kpCaptor.getAllValues().size());
//        assertEquals("Spring Boot", kpCaptor.getAllValues().get(0).getName());
//        assertEquals("Auto-Configuration", kpCaptor.getAllValues().get(1).getName());
//
//        // 验证捕获到的关系数据
//        com.example.aicourse.entity.KnowledgePointRelation capturedRelation = relCaptor.getValue();
//        assertEquals(102L, capturedRelation.getSourceId()); // "Auto-Configuration"的ID
//        assertEquals(101L, capturedRelation.getTargetId());  // "Spring Boot"的ID
//        assertEquals("PART_OF", capturedRelation.getRelationType());
//    }
//
//    @Test
//    void saveGraphFromMap_withEmptyData_shouldHandleGracefully() {
//        // 1. 准备空数据
//        Long courseId = 1L;
//        Map<String, List<Map<String, String>>> emptyGraphData = new HashMap<>();
//        emptyGraphData.put("nodes", null);
//        emptyGraphData.put("edges", null);
//
//        // 2. 执行
//        knowledgeGraphService.saveGraphFromMap(courseId, emptyGraphData);
//
//        // 3. 断言 - 应该正常处理，不抛出异常
//        verify(knowledgePointMapper, times(1)).delete(any());
//        verify(knowledgePointMapper, never()).insert(any());
//        verify(relationMapper, never()).insert(any());
//    }
//
//    @Test
//    void saveGraphFromMap_withOnlyNodes_shouldSaveNodesOnly() {
//        // 1. 准备只有节点的数据
//        Long courseId = 1L;
//        Map<String, List<Map<String, String>>> graphData = new HashMap<>();
//
//        List<Map<String, String>> nodes = Arrays.asList(
//            createNode("Spring Boot", "核心框架"),
//            createNode("Auto-Configuration", "自动配置")
//        );
//
//        graphData.put("nodes", nodes);
//        graphData.put("edges", null);
//
//        // 模拟mapper.insert
//        when(knowledgePointMapper.insert(any())).thenReturn(1);
//
//        // 2. 执行
//        knowledgeGraphService.saveGraphFromMap(courseId, graphData);
//
//        // 3. 断言
//        verify(knowledgePointMapper, times(2)).insert(any());
//        verify(relationMapper, never()).insert(any());
//    }
//
//    // 辅助方法：创建节点数据
//    private Map<String, String> createNode(String name, String description) {
//        Map<String, String> node = new HashMap<>();
//        node.put("name", name);
//        node.put("description", description);
//        return node;
//    }
//
//    // 辅助方法：创建边数据
//    private Map<String, String> createEdge(String source, String target, String relation) {
//        Map<String, String> edge = new HashMap<>();
//        edge.put("source", source);
//        edge.put("target", target);
//        edge.put("relation", relation);
//        return edge;
//    }
//}