package com.xiaocong.content.api;

import com.xiaocong.content.model.po.CourseTeacher;
import com.xiaocong.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import lombok.extern.java.Log;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "课程老师接口", tags = "课程老师接口")
@RestController
public class CourseTeacherController {

    @Autowired
    CourseTeacherService courseTeacherService;

    @GetMapping("/courseTeacher/list/{id}")
    @ApiImplicitParam(value = "courseId", name = "课程Id", required = true, dataType = "Long", paramType = "path")
    public List<CourseTeacher> getCourseTeacherList(@PathVariable("id") Long id) {
        List<CourseTeacher> courseTeacherList = courseTeacherService.getCourseTeacherList(id);
        return courseTeacherList;
    }

    @PostMapping("/courseTeacher")
    public void saveOrUpdateCourseTeacher(@RequestBody CourseTeacher courseTeacher) {
        courseTeacherService.saveOrUpdateCourseTeacher(courseTeacher);
    }

    @DeleteMapping("/courseTeacher/course/{courseId}/{courseTeacherId}")
    public void removeCourseTeacher(@PathVariable("courseId") Long courseId ,
                                    @PathVariable("courseTeacherId") Long courseTeacherId) {
        courseTeacherService.removeCourseTeacher(courseId, courseTeacherId);


    }


}
