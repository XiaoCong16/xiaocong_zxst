package com.xiaocong.media.service.impl;

import com.xiaocong.media.mapper.MediaFilesMapper;
import com.xiaocong.media.mapper.MediaProcessHistoryMapper;
import com.xiaocong.media.mapper.MediaProcessMapper;
import com.xiaocong.media.model.po.MediaFiles;
import com.xiaocong.media.model.po.MediaProcess;
import com.xiaocong.media.model.po.MediaProcessHistory;
import com.xiaocong.media.service.MediaFileService;
import com.xiaocong.media.service.MediaProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class MediaProcessServiceImpl implements MediaProcessService {
    @Autowired
    MediaProcessMapper mediaProcessMapper;

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MediaProcessHistoryMapper mediaProcessHistoryMapper;

    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        List<MediaProcess> mediaProcesses = mediaProcessMapper.selectListByShardIndex(shardTotal,shardIndex , count);

        return mediaProcesses;
    }

    @Override
    public boolean startTask(long id) {
        int result = mediaProcessMapper.startTask(id);
        return result <= 0 ? false : true;
    }

    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
//        需要更新的任务
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if (mediaProcess == null) {
            return;
        }

//        如果任务执行失败
        if (status.equals("3")) {
            mediaProcess.setStatus("3");
            mediaProcess.setFailCount(mediaProcess.getFailCount() + 1);
            mediaProcess.setErrormsg(errorMsg);
            mediaProcessMapper.updateById(mediaProcess);
            return;
        }
//        如果任务执行成功
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
//        更新media_file表中的url
        mediaFiles.setUrl(url);
        mediaFilesMapper.updateById(mediaFiles);

//        更新mediaprocess表的状态
        mediaProcess.setStatus("2");
        mediaProcess.setFinishDate(LocalDateTime.now());
        mediaProcessMapper.updateById(mediaProcess);

        //添加到历史记录
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
        mediaProcessHistoryMapper.insert(mediaProcessHistory);
        //删除mediaProcess
        mediaProcessMapper.deleteById(mediaProcess.getId());

    }
}
