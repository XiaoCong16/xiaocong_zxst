package com.xiaocong.media.service;

import com.xiaocong.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MediaProcessService {
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count);
    boolean startTask(long id);
    void saveProcessFinishStatus(Long taskId,String status,String fileId,String url,String errorMsg);

}
