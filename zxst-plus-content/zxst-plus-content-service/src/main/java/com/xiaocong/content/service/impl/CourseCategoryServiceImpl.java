package com.xiaocong.content.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaocong.content.mapper.CourseCategoryMapper;
import com.xiaocong.content.model.dto.CourseCategoryTreeDto;
import com.xiaocong.content.model.po.CourseCategory;
import com.xiaocong.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 课程分类 服务实现类
 * </p>
 *
 * @author xiaocong
 */
@Slf4j
@Service
public class CourseCategoryServiceImpl extends ServiceImpl<CourseCategoryMapper, CourseCategory> implements CourseCategoryService {

    @Override
    public List<CourseCategoryTreeDto> tree_nodes() {
        List<CourseCategory> list = this.list();
        List<CourseCategory> parentNodes = list.stream().filter(item -> {
            return item.getParentid().equals("1");
        }).collect(Collectors.toList());
        List<CourseCategoryTreeDto> result = parentNodes.stream().map(item -> {
            List<CourseCategoryTreeDto> childrenTreeNodes = childrenTreeNodes(item, list);
            CourseCategoryTreeDto courseCategoryTreeDto = new CourseCategoryTreeDto();
            BeanUtils.copyProperties(item, courseCategoryTreeDto);
            courseCategoryTreeDto.setChildrenTreeNodes(childrenTreeNodes);
            return courseCategoryTreeDto;
        }).collect(Collectors.toList());
        return result;
    }

    private List<CourseCategoryTreeDto> childrenTreeNodes(CourseCategory parentNode, List<CourseCategory> courseCategoryList) {
        return list().stream()
                .filter(
                        item -> item.getParentid().equals(parentNode.getId())
                ).map(item -> {
                    CourseCategoryTreeDto courseCategoryTreeDto = new CourseCategoryTreeDto();
                    BeanUtils.copyProperties(item, courseCategoryTreeDto);
                    return courseCategoryTreeDto;
                })
                .collect(Collectors.toList());
    }

}
