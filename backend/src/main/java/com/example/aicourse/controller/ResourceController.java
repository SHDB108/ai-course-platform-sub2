package com.example.aicourse.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.aicourse.entity.Course;
import com.example.aicourse.entity.KnowledgePoint;
import com.example.aicourse.entity.Resource;
import com.example.aicourse.repository.CourseMapper;
import com.example.aicourse.repository.KnowledgePointMapper;
import com.example.aicourse.repository.ResourceMapper;
import com.example.aicourse.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/resources")
public class ResourceController {

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private KnowledgePointMapper knowledgePointMapper;

    @Autowired
    private CourseMapper courseMapper;

    /**
     * Get resource details with smart ID handling.
     *
     * Supports two ID formats:
     * 1. Knowledge Point ID (e.g., "kp_22") - Finds associated video resource
     * 2. Direct Resource ID (e.g., "123") - Returns resource directly
     *
     * @param id Resource ID or Knowledge Point ID
     * @return Resource details with both requested ID and real resource ID
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getResource(@PathVariable String id) {
        try {
            Map<String, Object> data = new HashMap<>();
            Resource resource = null;
            String title = null;
            String courseName = "Unknown Course";
            String chapterName = "Unknown Chapter";

            // Smart ID handling
            if (id.startsWith("kp_")) {
                // Extract numeric ID from "kp_22" -> 22
                Long knowledgePointId = Long.parseLong(id.substring(3));

                // Get Knowledge Point
                KnowledgePoint kp = knowledgePointMapper.selectById(knowledgePointId);

                if (kp != null) {
                    title = kp.getName();

                    // Get Course name
                    if (kp.getCourseId() != null) {
                        Course course = courseMapper.selectById(kp.getCourseId());
                        if (course != null) {
                            courseName = course.getCourseName();
                        }

                        // Set courseId for frontend
                        data.put("courseId", kp.getCourseId());

                        // Try to find a video resource matching this knowledge point
                        // Strategy 1: Fuzzy match by filename containing KP name
                        QueryWrapper<Resource> wrapper = new QueryWrapper<>();
                        wrapper.eq("course_id", kp.getCourseId())
                               .eq("type", "VIDEO")
                               .like("filename", kp.getName());

                        resource = resourceMapper.selectOne(wrapper);

                        // Strategy 2: If no match, get the first video in the course
                        if (resource == null) {
                            wrapper = new QueryWrapper<>();
                            wrapper.eq("course_id", kp.getCourseId())
                                   .eq("type", "VIDEO")
                                   .last("LIMIT 1");

                            resource = resourceMapper.selectOne(wrapper);
                        }
                    }

                    // Set chapter name from parent knowledge point if exists
                    if (kp.getParentId() != null && kp.getParentId() != 0) {
                        KnowledgePoint parent = knowledgePointMapper.selectById(kp.getParentId());
                        if (parent != null) {
                            chapterName = parent.getName();
                        }
                    } else {
                        chapterName = title; // Use KP name as chapter if no parent
                    }
                }
            } else {
                // Direct resource ID
                Long resourceId = Long.parseLong(id);
                resource = resourceMapper.selectById(resourceId);

                if (resource != null) {
                    title = resource.getFilename();

                    // Get course name and set courseId
                    if (resource.getCourseId() != null) {
                        Course course = courseMapper.selectById(resource.getCourseId());
                        if (course != null) {
                            courseName = course.getCourseName();
                        }
                        // Set courseId for frontend
                        data.put("courseId", resource.getCourseId());
                    }

                    // Try to find associated knowledge point for chapter name
                    QueryWrapper<KnowledgePoint> kpWrapper = new QueryWrapper<>();
                    kpWrapper.eq("course_id", resource.getCourseId())
                             .like("name", resource.getFilename())
                             .last("LIMIT 1");

                    KnowledgePoint kp = knowledgePointMapper.selectOne(kpWrapper);
                    if (kp != null) {
                        if (kp.getParentId() != null && kp.getParentId() != 0) {
                            KnowledgePoint parent = knowledgePointMapper.selectById(kp.getParentId());
                            if (parent != null) {
                                chapterName = parent.getName();
                            }
                        } else {
                            chapterName = kp.getName();
                        }
                    } else {
                        chapterName = courseName; // Fallback to course name
                    }
                }
            }

            // Build response
            data.put("id", id); // Return the requested ID

            if (resource != null) {
                data.put("realResourceId", resource.getId()); // The actual database resource ID
                data.put("title", title != null ? title : resource.getFilename());
                data.put("url", resource.getDownloadUrl() != null ? resource.getDownloadUrl() : "");
                data.put("duration", 0); // Duration will be set by frontend video element
            } else {
                // No resource found - return placeholder
                data.put("realResourceId", null);
                data.put("title", title != null ? title : "Knowledge Point " + id);
                data.put("url", ""); // Empty triggers frontend placeholder
                data.put("duration", 0);
            }

            data.put("courseName", courseName);
            data.put("chapterName", chapterName);

            return Result.ok(data);

        } catch (NumberFormatException e) {
            return Result.error("Invalid ID format");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("Failed to load resource: " + e.getMessage());
        }
    }
}
