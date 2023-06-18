package com.xiaocong.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaocong.base.model.PageParams;
import com.xiaocong.base.model.PageResult;
import com.xiaocong.content.model.dto.CourseCategoryTreeDto;
import com.xiaocong.content.model.dto.QueryCourseParamsDto;
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

}
