package com.xiaocong.media.service.jobhandler;

import com.xiaocong.media.model.po.MediaProcess;
import com.xiaocong.media.service.MediaFileService;
import com.xiaocong.media.service.MediaProcessService;
import com.xiaocong.media.utils.Mp4VideoUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * XxlJob开发示例（Bean模式）
 * <p>
 * 开发步骤：
 * 1、任务开发：在Spring Bean实例中，开发Job方法；
 * 2、注解配置：为Job方法添加注解 "@XxlJob(value="自定义jobhandler名称", init = "JobHandler初始化方法", destroy = "JobHandler销毁方法")"，注解value值对应的是调度中心新建任务的JobHandler属性的值。
 * 3、执行日志：需要通过 "XxlJobHelper.log" 打印执行日志；
 * 4、任务结果：默认任务结果为 "成功" 状态，不需要主动设置；如有诉求，比如设置任务结果为失败，可以通过 "XxlJobHelper.handleFail/handleSuccess" 自主设置任务结果；
 *
 * @author xuxueli
 */

/**
 * 视频处理任务
 */
@Component
@Slf4j
public class VideoTask {
    private static Logger logger = LoggerFactory.getLogger(VideoTask.class);
    @Autowired
    MediaProcessService mediaProcessService;
    @Autowired
    MediaFileService mediaFileService;


    //    视频转码工具ffmpeg路径
    @Value("${videoprocess.ffmpegpath}")
    String ffmpegpath;

    /**
     * 1、简单任务示例（Bean模式）
     */
    @XxlJob("VideoJobHandler")
    public void demoJobHandler() throws Exception {
//        分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
//        cpu的核数
        int processors = Runtime.getRuntime().availableProcessors();
        //        查询待处理的任务
        List<MediaProcess> mediaProcessList = mediaProcessService.getMediaProcessList(shardIndex, shardTotal, processors);

        log.debug("查询待处理的任务:{}",mediaProcessList);
//        创建一个线程池
//    任务的数量
        int size = mediaProcessList.size();
        if (size <= 0) {
            return;
        }
        log.debug("取到的任务处理数量:{}", size);
        ExecutorService executorService = Executors.newFixedThreadPool(size);
//        使用计数器
        CountDownLatch countDownLatch = new CountDownLatch(size);
        for (MediaProcess mediaProcess : mediaProcessList) {
            executorService.execute(() -> {
                try {
                    //        开启任务
                    Long taskId = mediaProcess.getId();
                    boolean b = mediaProcessService.startTask(taskId);
                    if (!b) {
                        log.debug("抢占任务失败，任务id:{}", taskId);
                        return;
                    }
                    String bucket = mediaProcess.getBucket();
                    String filePath = mediaProcess.getFilePath();
//                文件id就是md5
                    String fileId = mediaProcess.getFileId();
                    //        执行视频转码
                    File file = mediaFileService.downloadFileFromMinIo(bucket, filePath);
                    if (file == null) {
                        log.debug("下载视频出错，任务id:{}，bucket：{},objectName:{}", taskId, bucket, filePath);
//                    保存任务失败的结果
                        mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "下载视频到本地失败");
                        return;
                    }
//                源avi视频路径
                    String video_path = file.getAbsolutePath();
//                转换后mp4文件的名称
                    String mp4_name = fileId + ".mp4";
//                转换后mp4文件的路径
                    File mp4File;
                    try {
                        mp4File = File.createTempFile("minio", ".mp4");
                    } catch (IOException e) {
                        log.debug("创建临时文件异常:{}", e.getMessage());
                        mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "创建临时文件异常");
                        return;
                    }
                    String mp4_path = mp4File.getAbsolutePath();
//                创建工具类对象
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegpath, video_path, mp4_name, mp4_path);
//                开始转换视频，成功将返回success
                    String result = videoUtil.generateMp4();
                    if (!"success".equals(result)) {
                        log.debug("视频转码失败:bucket：{},objectName:{}", bucket, filePath);
                        mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "视频转码失败");
                        return;
                    }
                    //        上传到minio
                    String objectName = getFilePath(fileId, ".mp4");
                    boolean b1 = mediaFileService.addFileToMinIo(mp4_path, "video/mp4", bucket, objectName);
                    if (!b1) {
                        log.debug("上传mp4视频到minio失败:taskId：{}", taskId);
                        mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "上传mp4视频到minio失败");
                        return;
                    }
                    String url = "/" + bucket + "/" + objectName;
//                保存处理结果
                    mediaProcessService.saveProcessFinishStatus(taskId, "2", fileId, url, null);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
//        指定最大限度的等待时间
        countDownLatch.await(30, TimeUnit.MINUTES);

    }

    private String getFilePath(String fileMd5, String fileExt) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }


}
