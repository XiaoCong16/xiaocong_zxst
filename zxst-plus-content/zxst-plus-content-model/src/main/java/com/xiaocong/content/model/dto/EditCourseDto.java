package com.xiaocong.content.model.dto;

import com.xiaocong.base.exception.ValidationGroups;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class EditCourseDto extends AddCourseDto {
    @ApiModelProperty(value = "课程id",required = true)
    @NotNull(groups = {ValidationGroups.Update.class},message = "课程id不能为空")
    private Long id;
}
