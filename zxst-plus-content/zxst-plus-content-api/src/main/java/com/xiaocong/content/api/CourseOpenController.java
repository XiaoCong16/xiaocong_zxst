package com.xiaocong.content.api;

import com.xiaocong.content.model.dto.CoursePreviewDto;
import com.xiaocong.content.service.CourseBaseService;
import com.xiaocong.content.service.CoursePublishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/open")
public class CourseOpenController {
    @Autowired
    private CourseBaseService courseBaseInfoService;

    @Autowired
    private CoursePublishService coursePublishService;

    //    查询课程基本信息
    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getPreviewInfo(@PathVariable("courseId") Long courseId) {
        //获取课程预览信息
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);
        return coursePreviewInfo;
    }


}
