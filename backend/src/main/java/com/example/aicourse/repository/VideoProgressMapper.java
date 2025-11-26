package com.example.aicourse.repository;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aicourse.entity.VideoProgress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

@Mapper
public interface VideoProgressMapper extends BaseMapper<VideoProgress>{
    @Select({
            "<script>",
            "SELECT * FROM t_video_progress",
            "WHERE resource_id IN",
            "<foreach item='id' collection='resourceIds' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    List<VideoProgress> selectByResourceIds(@Param("resourceIds") Collection<Long> resourceIds);

    @Select({
            "<script>",
            "SELECT * FROM t_video_progress",
            "WHERE student_id = #{studentId}",
            "AND resource_id IN",
            "<foreach item='id' collection='resourceIds' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    List<VideoProgress> selectByStudentAndResourceIds(@Param("studentId") Long studentId,
                                                      @Param("resourceIds") Collection<Long> resourceIds);
}
