package com.xiaocong.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaocong.base.model.PageParams;
import com.xiaocong.base.model.PageResult;
import com.xiaocong.content.model.dto.*;
import com.xiaocong.content.model.po.CourseBase;

/**
 * <p>
 * 课程基本信息 服务类
 * </p>
 *
 * @author xiaocong
 * @since 2023-06-13
 */
public interface CourseBaseService extends IService<CourseBase> {
    PageResult<CourseBase> list(PageParams pageParams, QueryCourseParamsDto queryCourseParams);

    CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);

    /**
     * 根据课程id查询课程信息
     * @param courseId 课程id
     * @return
     */
    CourseBaseInfoDto getCourseBaseInfo(Long courseId);

    /**
     * 修改课程信息
     * @param companyId
     * @param addCourseDto
     * @return
     */
    CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto addCourseDto);
}
