package com.xiaocong.content.api;

import com.xiaocong.content.model.dto.CourseCategoryTreeDto;
import com.xiaocong.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class CourseCategory {
    @Autowired
    CourseCategoryService categoryService;

    @GetMapping("course-category/tree-nodes")
    public List<CourseCategoryTreeDto> tree_nodes(){
        return  categoryService.tree_nodes();
    }
}
