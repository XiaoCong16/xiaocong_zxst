package com.xiaocong.content.model.dto;

import com.xiaocong.content.model.po.Teachplan;
import com.xiaocong.content.model.po.TeachplanMedia;
import lombok.Data;

import java.util.List;

/**
 * 课程计划
 */
@Data
public class TeachplanDto extends Teachplan {
    //课程计划关联的媒资信息
    TeachplanMedia teachplanMedia;

    //子结点
    List<TeachplanDto> teachPlanTreeNodes;

}
