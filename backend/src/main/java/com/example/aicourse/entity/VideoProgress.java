package com.example.aicourse.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_video_progress")
public class VideoProgress {
    @TableId
    private Long id; //
    private Long resourceId; //
    private Long studentId; //
    private String progress; // JSON: {"elapsed":120,"segments":[[0,30],[90,120]]}
    private Integer completion; // 0–100
    private LocalDateTime lastViewTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime gmtCreate; //

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime gmtModified; //
}