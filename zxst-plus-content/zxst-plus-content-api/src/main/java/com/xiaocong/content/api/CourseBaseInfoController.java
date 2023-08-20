package com.xiaocong.content.api;

import com.xiaocong.base.exception.ValidationGroups;
import com.xiaocong.base.model.PageParams;
import com.xiaocong.base.model.PageResult;
import com.xiaocong.content.model.dto.*;
import com.xiaocong.content.model.po.CourseBase;
import com.xiaocong.content.service.CourseBaseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 *
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

    @ApiOperation("新增课程信息")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated(ValidationGroups.Insert.class) AddCourseDto addCourseDto) {
        Long companyId = 1L;
        CourseBaseInfoDto courseBaseInfoDto = courseBaseService.createCourseBase(companyId, addCourseDto);
        return courseBaseInfoDto;
    }

    @ApiOperation("根据id查询课程接口")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto getCourseById(@PathVariable("courseId") Long courseId) {
        CourseBaseInfoDto result = courseBaseService.getCourseBaseInfo(courseId);
        return result;
    }


    @ApiOperation("修改课程信息")
    @PutMapping("/course")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated(ValidationGroups.Update.class) EditCourseDto addCourseDto) {
        Long companyId = 1232141425L;
        CourseBaseInfoDto courseBaseInfoDto = courseBaseService.updateCourseBase(companyId, addCourseDto);
        return courseBaseInfoDto;
    }

    @ApiOperation("删除课程信息")
    @DeleteMapping("/course/{courseId}")
    public void removeCourseBase(@PathVariable("courseId") Long courseId) {
        courseBaseService.removeCourse(courseId);
    }

}
