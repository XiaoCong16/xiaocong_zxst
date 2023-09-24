package com.xiaocong.media.service.impl;

import com.alibaba.nacos.common.utils.IoUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xiaocong.base.exception.zxstException;
import com.xiaocong.base.model.PageParams;
import com.xiaocong.base.model.PageResult;
import com.xiaocong.base.model.RestResponse;
import com.xiaocong.media.mapper.MediaFilesMapper;
import com.xiaocong.media.mapper.MediaProcessMapper;
import com.xiaocong.media.model.dto.QueryMediaParamsDto;
import com.xiaocong.media.model.dto.UploadFileParamsDto;
import com.xiaocong.media.model.dto.UploadFileResultDto;
import com.xiaocong.media.model.po.MediaFiles;
import com.xiaocong.media.model.po.MediaProcess;
import com.xiaocong.media.service.MediaFileService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.DeletedObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Autowired
    MediaProcessMapper mediaProcessMapper;
    //    存储普通文件
    @Value("${minio.bucket.files}")
    String bucket_mediafiles;

    //    存储视频
    @Value("${minio.bucket.video}")
    String bucket_video;

    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath, String objectName) {
//        将文件上传到minio
//        文件类型
        String filename = uploadFileParamsDto.getFilename();
        String extension = filename.substring(filename.indexOf("."));
        String mimeType = getMimeType(extension);
        String fileMd5 = getFileMd5(new File(localFilePath));
//        文件路径格式
        if (StringUtils.isEmpty(objectName)) {
//            子目录
            String defaultFolderPath = getDefaultFolderPath();
            objectName = defaultFolderPath + fileMd5 + extension;
        }
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
            extension = "";
        }
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
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

    @Override
    public MediaFiles getFileById(String mediaId) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(mediaId);
        return mediaFiles;
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
//            判断如果是avi视频，写入待处理任务
            addWaitingTask(mediaFiles);
            return mediaFiles;
        }
        return mediaFiles;
    }

    //    添加待处理任务
    private void addWaitingTask(@NotNull MediaFiles mediaFiles) {
//        获取文件的mimetype
        String filename = mediaFiles.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(extension);

//        log.error("extension:{},mimeType:{}", extension, mimeType);
        if (mimeType != null && mimeType.equals("video/x-msvideo")) {
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles, mediaProcess);
//            状态时未处理
            mediaProcess.setStatus("1");
            mediaProcess.setCreateDate(LocalDateTime.now());
            mediaProcess.setFailCount(0);
            mediaProcess.setUrl(null);
            mediaProcessMapper.insert(mediaProcess);
        }

    }

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

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

    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
//        查询数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null) {
//            查询minio
            String bucket = mediaFiles.getBucket();
            String filePath = mediaFiles.getFilePath();
            GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(bucket).object(filePath).build();
            try {
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                if (inputStream != null) {
                    return RestResponse.success(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return RestResponse.success(false);
    }

    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
//        分块文件所在文件目录路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
//            查询minio
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucket_video)
                .object(chunkFileFolderPath + chunkIndex)
                .build();
        try {
            FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
//            文件已存在
            if (inputStream != null) {
                return RestResponse.success(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return RestResponse.success(false);
    }

    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath) {
        String mimeType = getMimeType(null);
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5) + chunk;
        System.out.println(mimeType);
//            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
//                    .bucket(bucket_video)
//                    .filename(localChunkFilePath)
//                    .object(chunkFileFolderPath + chunk)
//                    .contentType(mimeType)
//                    .build();
//            minioClient.uploadObject(uploadObjectArgs);
//        上传文件到minio
        boolean b = addFileToMinIo(localChunkFilePath, mimeType, bucket_video, chunkFileFolderPath);
//        没成功
        if (!b) {
            return RestResponse.validfail(false, "上传分块文件失败");
        }
//        成功
        return RestResponse.success(true);
    }

    @Override
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        //=======文件合并=======
        //        获取分块文件所在目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
//        找到分块文件调用minio的sdk进行合并
        List<ComposeSource> composeSources = Stream.iterate(0, i -> ++i).limit(chunkTotal)
                .map(
                        i -> ComposeSource.builder()
                                .bucket(bucket_video)
                                .object(chunkFileFolderPath + i)
                                .build()
                )
                .collect(Collectors.toList());
//        指定合并后的filename等信息
//        获取文件拓展名
        String filename = uploadFileParamsDto.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
//        合并后文件的名称
        String objectName = getFilePathByMd5(fileMd5, extension);
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket(bucket_video)
                .sources(composeSources)
                .object(objectName)
                .build();
        try {
            minioClient.composeObject(composeObjectArgs);
        } catch (Exception e) {
            log.error("合并文件出错，bucket：{}，objecName:{},错误信息：{}", bucket_video, objectName, e.getMessage());
            return RestResponse.validfail(false, "上传分块文件失败");
        }
        //====================文件合并后检验是否完整==========================
        //        检验源文件与上传的文件是否一致
        File file = downloadFileFromMinIo(bucket_video, objectName);
        String mergeFileMd5;
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
//            设置文件大小
            uploadFileParamsDto.setFileSize(file.length());
//            校验md5
            mergeFileMd5 = DigestUtils.md5Hex(fileInputStream);
            if (!fileMd5.equals(mergeFileMd5)) {
                log.error("校验合并文件md5值不一致，原始文件：{}，合并文件：{}", fileMd5, mergeFileMd5);
                return RestResponse.validfail(false, "文件上传失败");
            }
        } catch (Exception e) {
            return RestResponse.validfail(false, "文件上传失败");
        }

        //===============文件信息入库========================
        //        文件入库
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_video, objectName);
        if (mediaFiles == null) {
            return RestResponse.validfail(false, "文件入库失败");
        }
//        ==============清理分块文件===============
        clearChunkFiles(chunkFileFolderPath, chunkTotal);
        return RestResponse.success(true);
    }

    //    从minio下载文件
    public File downloadFileFromMinIo(String bucket, String objectName) {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .build();
        File tempFile = null;
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            inputStream = minioClient.getObject(getObjectArgs);
            tempFile = File.createTempFile(objectName, ".merge");
            fileOutputStream = new FileOutputStream(tempFile);
            IoUtils.copy(inputStream, fileOutputStream);
        } catch (Exception e) {
            log.error("文件下载失败,bucket:{},错误信息:{}", bucket, e.getMessage());
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return tempFile;
    }

    private void clearChunkFiles(String chunkFileFolderPath, int chunkTotal) {
        List<DeleteObject> deleteObjectList = Stream.iterate(0, i -> ++i).limit(chunkTotal)
                .map(i -> new DeleteObject(chunkFileFolderPath + i))
                .collect(Collectors.toList());
        RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket(bucket_video).objects(deleteObjectList).build();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
        results.forEach(f -> {
            DeleteError deleteError = null;
            try {
                deleteError = f.get();
            } catch (Exception e) {
                e.printStackTrace();
                log.error("清楚分块文件失败,objectname:{}", deleteError.objectName(), e);
            }
        });
    }

    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

    private String getFilePathByMd5(String fileMd5, String extension) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + extension;
    }


}
