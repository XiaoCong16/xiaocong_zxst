package com.xiaocong.media.api;

import com.xiaocong.base.model.RestResponse;
import com.xiaocong.media.model.dto.UploadFileParamsDto;
import com.xiaocong.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;

@Api(value = "大文件上传接口", tags = "大文件上传接口")
@RestController
@Slf4j
public class BigFileController {

    @Autowired
    MediaFileService mediaFileService;

    Long beginTime = 0L;
    Long endTime = 0L;

    @ApiOperation(value = "文件上传前检查文件")
    @PostMapping("/upload/checkfile")
    public RestResponse<Boolean> checkfile(
            @RequestParam("fileMd5") String fileMd5
    ) throws Exception {
        beginTime = System.currentTimeMillis();
        return mediaFileService.checkFile(fileMd5);
    }


    @ApiOperation(value = "分块文件上传前的检测")
    @PostMapping("/upload/checkchunk")
    public RestResponse<Boolean> checkchunk(@RequestParam("fileMd5") String fileMd5,
                                            @RequestParam("chunk") int chunk) throws Exception {
        return mediaFileService.checkChunk(fileMd5, chunk);
    }

    @ApiOperation(value = "上传分块文件")
    @PostMapping("/upload/uploadchunk")
    public RestResponse uploadchunk(@RequestParam("file") MultipartFile file,
                                    @RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("chunk") int chunk) throws Exception {
        File tempFile = File.createTempFile(fileMd5, "tmp");
        file.transferTo(tempFile);
        return mediaFileService.uploadChunk(fileMd5, chunk, tempFile.getAbsolutePath());
    }

    @ApiOperation(value = "合并文件")
    @PostMapping("/upload/mergechunks")
    public RestResponse mergechunks(@RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("fileName") String fileName,
                                    @RequestParam("chunkTotal") int chunkTotal) throws Exception {
        Long companyId = 1232141425L;
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setFilename(fileName);
        uploadFileParamsDto.setTags("视频文件");
        uploadFileParamsDto.setFileType("001002");
        RestResponse response = mediaFileService.mergechunks(companyId, fileMd5, chunkTotal, uploadFileParamsDto);
        endTime = System.currentTimeMillis();
        log.info("文件上传开始时间：begintime：{}，结束时间：{}，总耗时：{}", beginTime, endTime, (endTime - beginTime) / 1000);
        return response;
    }


}
