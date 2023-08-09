package com.xiaocong.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaocong.content.mapper.TeachplanMapper;
import com.xiaocong.content.model.dto.SaveTeachPlanDto;
import com.xiaocong.content.model.dto.TeachplanDto;
import com.xiaocong.content.model.po.Teachplan;
import com.xiaocong.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 课程计划 服务实现类
 * </p>
 *
 * @author xiaocong
 */
@Slf4j
@Service
public class TeachplanServiceImpl extends ServiceImpl<TeachplanMapper, Teachplan> implements TeachplanService {

    public List<TeachplanDto> findTeachTree(long courseId) {
        return baseMapper.selectTreeNodes(courseId);
    }

    @Override
    public void saveTeachplan(SaveTeachPlanDto teachplan) {
        Long teachplanId = teachplan.getId();
//        新增
        if (teachplanId == null) {
            Teachplan teachplan1 = new Teachplan();
            BeanUtils.copyProperties(teachplan, teachplan1);
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("course_id", teachplan.getCourseId());
            queryWrapper.eq("parentid", teachplan.getParentid());
            int count = this.count(queryWrapper);
            teachplan1.setOrderby(count + 1);
            this.save(teachplan1);

        } else {
//            修改
            Teachplan teachplan1 = this.getById(teachplanId);
            BeanUtils.copyProperties(teachplan, teachplan1);
            this.updateById(teachplan1);
        }
    }
}
