package com.xiaocong.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaocong.content.model.po.CourseTeacher;

import java.util.List;

/**
 * <p>
 * 课程-教师关系表 服务类
 * </p>
 *
 * @author xiaocong
 * @since 2023-06-13
 */
public interface CourseTeacherService extends IService<CourseTeacher> {

    List<CourseTeacher> getCourseTeacherList(Long id);

    void saveOrUpdateCourseTeacher(CourseTeacher courseTeacher);

    void removeCourseTeacher(Long courseId, Long courseTeacherId);
}
