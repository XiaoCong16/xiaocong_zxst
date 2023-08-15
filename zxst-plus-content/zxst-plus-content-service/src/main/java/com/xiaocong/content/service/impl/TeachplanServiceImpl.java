package com.xiaocong.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaocong.base.exception.zxstException;
import com.xiaocong.content.mapper.TeachplanMapper;
import com.xiaocong.content.model.dto.SaveTeachPlanDto;
import com.xiaocong.content.model.dto.TeachplanDto;
import com.xiaocong.content.model.po.Teachplan;
import com.xiaocong.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    @Transactional
    public void saveTeachplan(SaveTeachPlanDto teachplan) {
        Long teachplanId = teachplan.getId();
//        新增
        if (teachplanId == null) {
            Teachplan teachplan1 = new Teachplan();
            BeanUtils.copyProperties(teachplan, teachplan1);
            int count = findMaxOrderby(teachplan);
            teachplan1.setOrderby(count + 1);
            this.save(teachplan1);
        } else {
//            修改
            Teachplan teachplan1 = this.getById(teachplanId);
            BeanUtils.copyProperties(teachplan, teachplan1);
            this.updateById(teachplan1);
        }
    }

    private int findMaxOrderby(SaveTeachPlanDto teachplan) {
        //            查询出最大的order字段
        Long courseId = teachplan.getCourseId();
        Long parentid = teachplan.getParentid();
        int count = baseMapper.selectMaxOrder(courseId, parentid);
        return count;
    }

    @Override
    @Transactional
    public void deleteTeachPlan(Long id) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("parentid", id);
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new zxstException("课程计划信息还有子级信息，无法操作");
        } else {
            this.removeById(id);
        }

    }

    @Override
    @Transactional
    public void moveDown(Long id) {
        Teachplan teachplan = baseMapper.selectById(id);
        Long parentid = teachplan.getParentid();
        Integer orderby = teachplan.getOrderby();
        //        根据父id和orderby查询出比他大的那个
        Teachplan nextTeachplan = baseMapper.selectNextOrderby(parentid, orderby);
        if (nextTeachplan == null) {
            throw new zxstException("不能下移");
        }
        Integer nextTeachplanOrderby = nextTeachplan.getOrderby();
        Integer teachplanOrderby = teachplan.getOrderby();
        //        将他们两个的orderby交换
        nextTeachplan.setOrderby(teachplanOrderby);
        teachplan.setOrderby(nextTeachplanOrderby);
        //更新
        List list = new ArrayList<Teachplan>();
        list.add(teachplan);
        list.add(nextTeachplan);
        this.updateBatchById(list);
    }

    @Override
    @Transactional
    public void moveUp(Long id) {
        Teachplan teachplan = baseMapper.selectById(id);
        Long parentid = teachplan.getParentid();
        Integer orderby = teachplan.getOrderby();
        //        根据父id和orderby查询出比他小的那个
        Teachplan previousTeachplan = baseMapper.selectPreviousOrderby(parentid, orderby);
        if (previousTeachplan == null) {
            throw new zxstException("不能上移");
        }
        Integer previousTeachplanOrderby = previousTeachplan.getOrderby();
        Integer teachplanOrderby = teachplan.getOrderby();
//        将他们两个的orderby交换
        previousTeachplan.setOrderby(teachplanOrderby);
        teachplan.setOrderby(previousTeachplanOrderby);

        //更新
        List list = new ArrayList<Teachplan>();
        list.add(teachplan);
        list.add(previousTeachplan);
        this.updateBatchById(list);
    }
}
