package com.xiaocong.media.service;

import com.xiaocong.base.model.PageParams;
import com.xiaocong.base.model.PageResult;
import com.xiaocong.media.model.dto.QueryMediaParamsDto;
import com.xiaocong.media.model.dto.UploadFileParamsDto;
import com.xiaocong.media.model.dto.UploadFileResultDto;
import com.xiaocong.media.model.po.MediaFiles;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @description 媒资文件管理业务类
 * @author Mr.M
 * @date 2022/9/10 8:55
 * @version 1.0
 */
public interface MediaFileService {

 /**
  * @description 媒资文件查询方法
  * @param pageParams 分页参数
  * @param queryMediaParamsDto 查询条件
  * @return com.xiaocong.base.model.PageResult<com.xiaocong.media.model.po.MediaFiles>
  * @author Mr.M
  * @date 2022/9/10 8:57
 */
 public PageResult<MediaFiles> queryMediaFiels(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);


    UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String filePath);

    public MediaFiles addMediaFilesToDb(Long companyId,
                                        String fileMd5,
                                        UploadFileParamsDto uploadFileParamsDto,
                                        String bucket,
                                        String objectName);
}
