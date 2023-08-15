package com.xiaocong.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaocong.content.model.dto.SaveTeachPlanDto;
import com.xiaocong.content.model.dto.TeachplanDto;
import com.xiaocong.content.model.po.Teachplan;

import java.util.List;

/**
 * <p>
 * 课程计划 服务类
 * </p>
 *
 * @author xiaocong
 * @since 2023-06-13
 */
public interface TeachplanService extends IService<Teachplan> {
    public List<TeachplanDto> findTeachTree(long courseId);

    /**
     * 新增，修改，保存课程计划接口
     * @param teachplan
     */
    public void saveTeachplan(SaveTeachPlanDto teachplan);

    void deleteTeachPlan(Long id);

    void moveDown(Long id);

    void moveUp(Long id);
}
