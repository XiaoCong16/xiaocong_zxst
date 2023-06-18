package com.xiaocong.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaocong.content.model.dto.CourseCategoryTreeDto;
import com.xiaocong.content.model.po.CourseCategory;

import java.util.List;

/**
 * <p>
 * 课程分类 服务类
 * </p>
 *
 * @author xiaocong
 * @since 2023-06-13
 */
public interface CourseCategoryService extends IService<CourseCategory> {

    List<CourseCategoryTreeDto> tree_nodes();
}
