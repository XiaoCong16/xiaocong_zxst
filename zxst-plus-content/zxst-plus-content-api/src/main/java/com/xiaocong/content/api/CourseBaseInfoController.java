package com.xiaocong.content.api;

import com.xiaocong.base.model.PageParams;
import com.xiaocong.base.model.PageResult;
import com.xiaocong.content.model.dto.CourseCategoryTreeDto;
import com.xiaocong.content.model.dto.QueryCourseParamsDto;
import com.xiaocong.content.model.po.CourseBase;
import com.xiaocong.content.service.CourseBaseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2023/2/11 15:44
 */
@Api(value = "课程信息管理接口", tags = "课程信息管理接口")
@RestController
@Slf4j
public class CourseBaseInfoController {

    @Autowired
    private CourseBaseService courseBaseService;

    @ApiOperation("课程查询接口")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParams) {
        PageResult<CourseBase> list = courseBaseService.list(pageParams, queryCourseParams);
        return list;
    }





}
