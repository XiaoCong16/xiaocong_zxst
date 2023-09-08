package com.xiaocong.meida;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.j256.simplemagic.ContentType;
import io.minio.*;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;

public class MinioTest {

    static MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://175.178.151.17:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();


    @Test
    public void testUpload() {
        try {
            ContentInfo mimeTypeMatch = ContentInfoUtil.findExtensionMatch(".mp4");
            String mimeType = mimeTypeMatch.getMimeType();
            UploadObjectArgs testbucket = UploadObjectArgs.builder()
                    .bucket("testbucket")
                    .object("001/test001.mp4")//添加子目录
                    .filename("H:\\2022最新版Java学习路线图\\第4阶段—中间键&服务框架\\2、分布式开发框架Dubbo\\视频--分布式开发框架Dubbo\\视频\\01-今日内容.mp4")
                    .contentType(mimeType)//默认根据扩展名确定文件内容类型，也可以指定
                    .build();
            minioClient.uploadObject(testbucket);
            System.out.println("上传成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("上传失败");
        }

    }


    @Test
    public void testRemove() {
        try {
            RemoveObjectArgs testbucket = RemoveObjectArgs
                    .builder()
                    .bucket("testbucket")
                    .object("001/test001.mp4")
                    .build();
            minioClient.removeObject(testbucket);
            System.out.println("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("删除失败");
        }
    }

    @Test
    public void testDownload() {
        try {
            GetObjectArgs build = GetObjectArgs.builder().bucket("testbucket").object("001/test001.mp4").build();
            FilterInputStream object = minioClient.getObject(build);
            FileOutputStream fileOutputStream = new FileOutputStream(new File("H:\\2022最新版Java学习路线图\\第4阶段—中间键&服务框架\\2、分布式开发框架Dubbo\\视频--分布式开发框架Dubbo\\视频\\a.mp4"));
            IOUtils.copy(object,fileOutputStream);

            System.out.println("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("删除失败");
        }
    }

    @Test
    public void tes(){
        String filename = "wwowo.mp4";
        String extension = filename.substring(filename.indexOf("."));
        System.out.println(extension);
    }
}
