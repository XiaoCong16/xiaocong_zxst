package com.xiaocong.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaocong.content.mapper.CourseTeacherMapper;
import com.xiaocong.content.model.po.CourseTeacher;
import com.xiaocong.content.service.CourseTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 课程-教师关系表 服务实现类
 * </p>
 *
 * @author xiaocong
 */
@Slf4j
@Service
public class CourseTeacherServiceImpl extends ServiceImpl<CourseTeacherMapper, CourseTeacher> implements CourseTeacherService {

    @Override
    public List<CourseTeacher> getCourseTeacherList(Long id) {
        QueryWrapper<CourseTeacher> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", id);
        List<CourseTeacher> courseTeacherList = this.list(queryWrapper);
        return courseTeacherList;
    }

    @Override
    @Transactional
    public void saveOrUpdateCourseTeacher(CourseTeacher courseTeacher) {
        if (courseTeacher.getId() != null) {
//            修改
            this.updateById(courseTeacher);
        } else {
//            保存
            this.save(courseTeacher);
        }
    }

    @Override
    @Transactional
    public void removeCourseTeacher(Long courseId, Long courseTeacherId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("course_id", courseId);
        queryWrapper.eq("id", courseTeacherId);
        this.remove(queryWrapper);
    }
}
