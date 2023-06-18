package com.xiaocong.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaocong.base.model.PageParams;
import com.xiaocong.base.model.PageResult;
import com.xiaocong.content.mapper.CourseBaseMapper;
import com.xiaocong.content.model.dto.CourseCategoryTreeDto;
import com.xiaocong.content.model.dto.QueryCourseParamsDto;
import com.xiaocong.content.model.po.CourseBase;
import com.xiaocong.content.service.CourseBaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * <p>
 * 课程基本信息 服务实现类
 * </p>
 *
 * @author xiaocong
 */
@Slf4j
@Service
public class CourseBaseServiceImpl extends ServiceImpl<CourseBaseMapper, CourseBase> implements CourseBaseService {


    @Override
    public PageResult<CourseBase> list(PageParams pageParams, QueryCourseParamsDto queryCourseParams) {
        QueryWrapper<CourseBase> queryWrapper = new QueryWrapper<>();
        if (queryCourseParams != null) {
            if (!StringUtils.isEmpty(queryCourseParams.getCourseName())) {
                queryWrapper.like("name", queryCourseParams.getCourseName());
            }
            if (!StringUtils.isEmpty(queryCourseParams.getPublishStatus())) {

                queryWrapper.eq("status", queryCourseParams.getPublishStatus());
            }
            if (!StringUtils.isEmpty(queryCourseParams.getAuditStatus())) {
                queryWrapper.eq("audit_status", queryCourseParams.getAuditStatus());
            }
        }
        Page<CourseBase> page = new Page<CourseBase>();
        page.setCurrent(pageParams.getPageNo());
        page.setSize(pageParams.getPageSize());
        Page<CourseBase> courseBasePage = this.page(page, queryWrapper);
        List<CourseBase> list = courseBasePage.getRecords();
        return new PageResult<>(list, courseBasePage.getTotal(), courseBasePage.getCurrent(), courseBasePage.getSize());
    }


}
