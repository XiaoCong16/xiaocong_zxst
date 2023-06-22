package com.xiaocong.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaocong.base.exception.zxstException;
import com.xiaocong.base.model.PageParams;
import com.xiaocong.base.model.PageResult;
import com.xiaocong.content.mapper.CourseBaseMapper;
import com.xiaocong.content.mapper.CourseCategoryMapper;
import com.xiaocong.content.mapper.CourseMarketMapper;
import com.xiaocong.content.model.dto.AddCourseDto;
import com.xiaocong.content.model.dto.CourseBaseInfoDto;
import com.xiaocong.content.model.dto.CourseCategoryTreeDto;
import com.xiaocong.content.model.dto.QueryCourseParamsDto;
import com.xiaocong.content.model.po.CourseBase;
import com.xiaocong.content.model.po.CourseCategory;
import com.xiaocong.content.model.po.CourseMarket;
import com.xiaocong.content.service.CourseBaseService;
import com.xiaocong.content.service.CourseMarketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
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

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

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

    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto) {

        //       1 课程基本信息表course_base写入数据
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(addCourseDto, courseBase);
        courseBase.setCompanyId(companyId);
        courseBase.setCreateDate(LocalDateTime.now());
//        审核状态未提交
        courseBase.setAuditStatus("202002");
//        发布状态未发布
        courseBase.setStatus("203001");
        boolean save = this.save(courseBase);
        if (!save) {
            throw new RuntimeException("课程添加失败");
        }
        //     2   课程营销表course_market写入数据  a

        CourseMarket courseMarket = new CourseMarket();
//        课程id
        courseMarket.setId(courseBase.getId());
        BeanUtils.copyProperties(addCourseDto, courseMarket);
        boolean save1 = saveCourseMarket(courseMarket);
        if (!save1) {
            throw new RuntimeException("课程添加失败");
        }
//        3 从数据库中查询详细信息
        CourseBaseInfoDto courseBaseInfo = getCourseBaseInfo(courseBase.getId());
        return courseBaseInfo;
    }

    private boolean saveCourseMarket(CourseMarket courseMarket) {
        String charge = courseMarket.getCharge();
//        参数合法性校验
        if (StringUtils.isEmpty(charge)) {
            throw new RuntimeException("收费规则为空");
        }
//        如果课程为收费
        if ("201001".equals(charge)) {
            if (courseMarket.getPrice() == null || courseMarket.getPrice().floatValue() <= 0) {
//                throw new RuntimeException("课程价格为空或必须大于0");
                throw new zxstException("课程价格为空或必须大于0");
            }
        }
        CourseMarket courseMarket1 = courseMarketMapper.selectById(courseMarket.getId());
//如果数据库中没有就添加，有就更新
        int count = 0;
        if (courseMarket1 == null) {
            count = courseMarketMapper.insert(courseMarket);
        } else {
            BeanUtils.copyProperties(courseMarket, courseMarket1);
            courseMarket1.setId(courseMarket1.getId());
            count = courseMarketMapper.updateById(courseMarket1);
        }
        return count == 1;

    }


    //    查询课程信息
    private CourseBaseInfoDto getCourseBaseInfo(long courseId) {
        CourseBase courseBase = baseMapper.selectById(courseId);
        if (courseBase == null) {
            return null;
        }
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);

//        组装在一起
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
//        查询大分类的名称
        QueryWrapper<CourseCategory> courseCategoryQueryWrapper = new QueryWrapper<>();
        courseCategoryQueryWrapper.eq("id", courseBase.getMt());
        CourseCategory courseCategory = courseCategoryMapper.selectOne(courseCategoryQueryWrapper);
//        查询小分类的名称
        QueryWrapper<CourseCategory> courseCategoryQueryWrapper2 = new QueryWrapper<>();
        courseCategoryQueryWrapper2.eq("id", courseBase.getSt());
        CourseCategory courseCategory2 = courseCategoryMapper.selectOne(courseCategoryQueryWrapper2);
//        设置大分类的名称
        courseBaseInfoDto.setMtName(courseCategory.getName());
//        设置小分类的名称
        courseBaseInfoDto.setStName(courseCategory2.getName());
        return courseBaseInfoDto;
    }
}
