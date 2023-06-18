package com.xiaocong.base.model;


import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PageParams {
    //    当前页码
    @ApiModelProperty("当前页码")
    private Long pageNo = 1L;
    //    每页显示记录数
    @ApiModelProperty("每页显示记录数")
    private Long pageSize = 1L;

}
