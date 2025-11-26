package com.example.aicourse.controller;

import com.example.aicourse.utils.Result;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/resources")
public class ResourceController {

    /**
     * 获取资源详情（Shell/Mock 实现）
     *
     * 对于任意请求的资源ID，返回一个有效的"shell"资源对象。
     * 当实际文件URL缺失时，前端会显示"资源待整合"占位符。
     *
     * @param id 资源ID（接受任意值）
     * @return 包含mock数据的成功响应
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getResource(@PathVariable String id) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("title", "Knowledge Point " + id + " (Demo)");
        data.put("courseName", "Target Course");
        data.put("chapterName", "Target Chapter");
        data.put("url", "");  // Empty string to trigger frontend placeholder
        data.put("duration", 0);

        return Result.ok(data);
    }
}
