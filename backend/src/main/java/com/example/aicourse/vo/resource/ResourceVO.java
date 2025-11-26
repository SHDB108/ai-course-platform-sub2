package com.example.aicourse.vo.resource;

import lombok.Data;

/**
 * 精简的课程资源描述，供跨子系统调用使用。
 */
@Data
public class ResourceVO {
    private Long id;
    private Long courseId;
    private String filename;
    private String type; // VIDEO, DOCUMENT, etc.
    private String downloadUrl;
}
