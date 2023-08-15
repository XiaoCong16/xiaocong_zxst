package com.xiaocong.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaocong.content.model.dto.TeachplanDto;
import com.xiaocong.content.model.po.Teachplan;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface TeachplanMapper extends BaseMapper<Teachplan> {
    public List<TeachplanDto> selectTreeNodes(long courseId);

    int selectMaxOrder(@Param("courseId") Long courseId, @Param("parentid") Long parentid);

    Teachplan selectPreviousOrderby(@Param("parentid") Long parentid, @Param("orderBy") Integer orderBy);

    Teachplan selectNextOrderby(@Param("parentid") Long parentid, @Param("orderBy") Integer orderBy);
}
