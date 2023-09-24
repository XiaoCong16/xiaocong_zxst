package com.xiaocong.content.service.jobhandler;

import com.xiaocong.base.exception.zxstException;
import com.xiaocong.content.service.CoursePublishService;
import com.xiaocong.messagesdk.model.po.MqMessage;
import com.xiaocong.messagesdk.service.MessageProcessAbstract;
import com.xiaocong.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {
    @Autowired
    CoursePublishService coursePublishService;

    @Override
    public boolean execute(MqMessage mqMessage) {
//        课程id
        long courseId = Long.valueOf(mqMessage.getBusinessKey1());

//        向es写索引数据

//        向redis写缓存

//        向minio上传html页面
        generateCourseHtml(mqMessage, courseId);
//        返回true任务 完成

        return false;
    }

    //    页面静态化html
    private void generateCourseHtml(MqMessage mqMessage, long courseId) {
//        任务id
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageOne = mqMessageService.getStageOne(taskId);
        if (stageOne > 0) {
            log.debug("任务1课程静态化完成无需处理");
        }
//        开始进行课程静态化
        //生成html页面
        File file = coursePublishService.generateCourseHtml(courseId);
//        上传到minio
        coursePublishService.uploadCourseHtml(courseId, file);
//        任务处理完成写任务处理完成
        mqMessageService.completedStageOne(taskId);
    }

    //    保存课程索引
    private void saveCourseIndex(MqMessage mqMessage, long courseId) {
        //        任务id
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageTwo = mqMessageService.getStageTwo(taskId);
        if (stageTwo > 0) {
            log.debug("任务2保存课程索引完成无需处理");
        }

        mqMessageService.completedStageTwo(taskId);
    }

    //保存课程缓存
    private void saveCourseCache(MqMessage mqMessage, long courseId) {
        //        任务id
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageThree = mqMessageService.getStageThree(taskId);
        if (stageThree > 0) {
            log.debug("任务3保存课程索引完成无需处理");
        }
        mqMessageService.completedStageThree(taskId);
    }

    @XxlJob("CoursePublishJobHandler")
    public void shardingJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
//        调用抽象类的方法执行任务
        process(shardIndex, shardTotal, "course_publish", 30, 60);

    }
}
