package com.example.aicourse.vo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageVO<T> {
    private List<T> records;
    private long total;
    private long size;
    private long current;
    private long pages; // 总页数
}