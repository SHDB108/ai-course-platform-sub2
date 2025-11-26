// ScoreRowVO.java
package com.example.aicourse.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreRowVO {
    private Long studentId;
    private Double score;
}