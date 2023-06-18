package com.xiaocong.content.service.impl;

import com.xiaocong.content.model.po.CourseBase;
import com.xiaocong.content.mapper.CourseBaseMapper;
import com.xiaocong.content.service.CourseBaseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

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

}
