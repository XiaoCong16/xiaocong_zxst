package com.xiaocong.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xiaocong.base.exception.zxstException;
import com.xiaocong.base.model.PageParams;
import com.xiaocong.base.model.PageResult;
import com.xiaocong.media.mapper.MediaFilesMapper;
import com.xiaocong.media.model.dto.QueryMediaParamsDto;
import com.xiaocong.media.model.dto.UploadFileParamsDto;
import com.xiaocong.media.model.dto.UploadFileResultDto;
import com.xiaocong.media.model.po.MediaFiles;
import com.xiaocong.media.service.MediaFileService;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Service
@Slf4j
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MinioClient minioClient;

    @Autowired
    MediaFileService currentProxy;
    //    存储普通文件
    @Value("${minio.bucket.files}")
    String bucket_mediafiles;

    //    存储视频
    @Value("${minio.bucket.video}")
    String bucket_video;

    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath) {
//        将文件上传到minio
//        文件类型
        String filename = uploadFileParamsDto.getFilename();
        String extension = filename.substring(filename.indexOf("."));
        String mimeType = getMimeType(extension);
//        文件路径格式
        String defaultFolderPath = getDefaultFolderPath();
        String fileMd5 = getFileMd5(new File(localFilePath));
        String objectName = defaultFolderPath + fileMd5 + extension;
        boolean result = addFileToMinIo(localFilePath, mimeType, bucket_mediafiles, objectName);
        if (!result) {
            zxstException.cast("上传文件失败");
        }
//        将文件信息保存到数据库
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_mediafiles, objectName);
//        准备返回对象
        if (mediaFiles == null) {
            zxstException.cast("文件上传后保存文件信息失败");
        }
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
        return uploadFileResultDto;
    }

    //    根据拓展名获取mimeType
    private String getMimeType(String extension) {
        if (extension == null) {
            return "";
        }
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch("");
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE; //通用mimetype
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }

    //    将文件上传到minio
    public boolean addFileToMinIo(String localFilePath, String mimeType, String bucket, String objectName) {
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .filename(localFilePath)
                    .contentType(mimeType)
                    .build();
            minioClient.uploadObject(uploadObjectArgs);
            log.debug("上传文件到MinIo成功，bucket:{},objectName:{}", bucket, objectName);
            return true;
        } catch (Exception e) {
            log.error("上传文件出错，bucket:{},objectName:{},错误信息:{}", bucket, objectName, e.getMessage());
            return false;
        }
    }

    //    获取文件默认存储路径为yyyy/mm/dd
    public String getDefaultFolderPath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String folder = sdf.format(new Date()).replace("-", "/") + "/";
        return folder;
    }

    //获取文件的md5
    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String fileMd5 = DigestUtils.md5Hex(fileInputStream);
            return fileMd5;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * @param companyId           机构id
     * @param fileMd5             文件md5值
     * @param uploadFileParamsDto 上传文件的信息
     * @param bucket              桶
     * @param objectName          对象名称
     * @return com.xiaocong.media.model.po.MediaFiles
     * @description 将文件信息添加到文件表
     * @author
     */
    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId,
                                        String fileMd5,
                                        UploadFileParamsDto uploadFileParamsDto,
                                        String bucket,
                                        String objectName) {

        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
//            文件id
            mediaFiles.setId(fileMd5);
//            机构id
            mediaFiles.setCompanyId(companyId);
//            桶
            mediaFiles.setBucket(bucket);
//            file_path
            mediaFiles.setFilePath(objectName);
//            url
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
//            上传时间
            mediaFiles.setCreateDate(LocalDateTime.now());
//            状态
            mediaFiles.setStatus("1");
//            审核状态
            mediaFiles.setAuditStatus("002003");
//            设置fileid
            mediaFiles.setFileId(fileMd5);
//            插入数据库
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert <= 0) {
                log.error("向数据库保存文件失败，,bucket:{},objectName:{}", bucket, objectName);
                return null;
            }
            return mediaFiles;
        }
        return mediaFiles;
    }

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto
            queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }
}
