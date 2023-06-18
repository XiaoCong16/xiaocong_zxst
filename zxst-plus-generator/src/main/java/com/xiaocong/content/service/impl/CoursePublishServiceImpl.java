package com.xiaocong.content.service.impl;

import com.xiaocong.content.model.po.CoursePublish;
import com.xiaocong.content.mapper.CoursePublishMapper;
import com.xiaocong.content.service.CoursePublishService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * 课程发布 服务实现类
 * </p>
 *
 * @author xiaocong
 */
@Slf4j
@Service
public class CoursePublishServiceImpl extends ServiceImpl<CoursePublishMapper, CoursePublish> implements CoursePublishService {

}
