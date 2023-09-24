package com.xiaocong.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaocong.base.exception.CommonError;
import com.xiaocong.base.exception.zxstException;
import com.xiaocong.content.config.MultipartSupportConfig;
import com.xiaocong.content.mapper.*;
import com.xiaocong.content.model.dto.CourseBaseInfoDto;
import com.xiaocong.content.model.dto.CoursePreviewDto;
import com.xiaocong.content.model.dto.TeachplanDto;
import com.xiaocong.content.model.po.*;
import com.xiaocong.content.service.CourseBaseService;
import com.xiaocong.content.service.CoursePublishService;
import com.xiaocong.content.service.TeachplanService;
import com.xiaocong.content.service.feign.MediaServiceClient;
import com.xiaocong.messagesdk.model.po.MqMessage;
import com.xiaocong.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * <p>
 * 课程发布 服务实现类
 * </p>
 *
 * @author xiaocong
 */
@Slf4j
@Service
public class CoursePublishServiceImpl extends ServiceImpl<CoursePublishMapper, CoursePublish> implements CoursePublishService {
    @Autowired
    CourseBaseService courseBaseService;

    @Autowired
    CourseBaseMapper courseBaseMapper;
    @Autowired
    CourseMarketMapper courseMarketMapper;
    @Autowired
    CoursePublishPreMapper coursePublishPreMapper;

    @Autowired
    CourseCategoryMapper courseCategoryMapper;
    @Autowired
    CoursePublishMapper coursePublishMapper;
    @Autowired
    TeachplanService teachplanService;

    @Autowired
    MqMessageService mqMessageService;

    @Autowired
    private Configuration configuration;

    @Autowired
    MediaServiceClient mediaServiceClient;


    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
//        课程基本信息,营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseService.getCourseBaseInfo(courseId);
//        课程计划信息
        List<TeachplanDto> teachplans = teachplanService.findTeachTree(courseId);
        coursePreviewDto.setCourseBase(courseBaseInfo);
        coursePreviewDto.setTeachplans(teachplans);
        return coursePreviewDto;
    }

    /**
     * @param courseId 课程id
     * @return void
     * @description 提交审核
     * @author Mr.M
     * @date 2023年9月21日23:10:44
     */
    @Override
    @Transactional
    public void commitAudit(Long companyId, Long courseId) {

        //约束校验
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        //课程审核状态
        String auditStatus = courseBase.getAuditStatus();
        //当前审核状态为已提交不允许再次提交
        if ("202003".equals(auditStatus)) {
            zxstException.cast("当前为等待审核状态，审核完成可以再次提交。");
        }
        //本机构只允许提交本机构的课程
        if (!courseBase.getCompanyId().equals(companyId)) {
            zxstException.cast("不允许提交其它机构的课程。");
        }

        //课程图片是否填写
        if (StringUtils.isEmpty(courseBase.getPic())) {
            zxstException.cast("提交失败，请上传课程图片");
        }

        //添加课程预发布记录
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        //课程基本信息加部分营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseService.getCourseBaseInfo(courseId);
        BeanUtils.copyProperties(courseBaseInfo, coursePublishPre);
        //课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        //转为json
        String courseMarketJson = JSON.toJSONString(courseMarket);
        //将课程营销信息json数据放入课程预发布表
        coursePublishPre.setMarket(courseMarketJson);
        /**
         * 课程分类在courseBaseService.getCourseBaseInfo(courseId);已经写了
         * //查询课程分类
         *          CourseCategory mtObj = courseCategoryMapper.selectById(courseBase.getMt());
         *          //        大分类名称
         *          String mtName = mtObj.getName();
         *          coursePublishPre.setMtName(mtName);
         *          CourseCategory stObj = courseCategoryMapper.selectById(courseBase.getSt());
         *          //        小分类名称
         *          String stName = stObj.getName();
         *          coursePublishPre.setStName(stName);
         */
//        查询课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachTree(courseId);
        if (teachplanTree.size() <= 0) {
            zxstException.cast("提交失败，还没有添加课程计划");
        }
        //转json
        String teachplanTreeString = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachplanTreeString);

        //设置预发布记录状态,已提交
        coursePublishPre.setStatus("202003");
        //教学机构id
        coursePublishPre.setCompanyId(companyId);
        //提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());
        CoursePublishPre coursePublishPreUpdate = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPreUpdate == null) {
            //添加课程预发布记录
            coursePublishPreMapper.insert(coursePublishPre);
        } else {
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        //更新课程基本表的审核状态
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }

    @Override
    @Transactional
    public void publish(Long companyId, Long courseId) {
//        查询预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null) {
            zxstException.cast("课程没有审核记录无法发布");
        }
        String status = coursePublishPre.getStatus();
        if (!"202004".equals(status)) {
            zxstException.cast("课程没有审核通过不允许发布");
        }
        if (!companyId.equals(coursePublishPre.getCompanyId())) {
            System.out.println(companyId + "=======================" + coursePublishPre.getCompanyId());
            zxstException.cast("本机构只能发布本机构的课程");
        }
        //        向课程发布表中写入数据
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre, coursePublish);
        CoursePublish coursePublish1 = coursePublishMapper.selectById(courseId);
        if (coursePublish1 == null) {
            coursePublishMapper.insert(coursePublish);
        } else {
            coursePublishMapper.updateById(coursePublish);
        }
//        将消息表写入数据
        saveCoursePublishMessage(courseId);
//        删除预发布表中数据
        coursePublishPreMapper.deleteById(courseId);
    }


    @Override
    public File generateCourseHtml(Long courseId) {
        //静态化文件
        File htmlFile = null;
        try {
            //加载模板
            //选指定模板路径,classpath下templates下
            //得到classpath路径
            String classpath = this.getClass().getResource("/").getPath();
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            //设置字符编码
            configuration.setDefaultEncoding("utf-8");
            Template template = configuration.getTemplate("course_template.ftl");
            //准备数据
            CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(courseId);
            Map<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);
            htmlFile = File.createTempFile("course", ".html");
            // 获取打印输出流
            PrintWriter printWriter = new PrintWriter(htmlFile.getAbsolutePath());
            template.process(map, printWriter);
        } catch (Exception e) {
            zxstException.cast(e.getMessage());
        }
        return htmlFile;
    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        if (file == null) {
            log.error("生成的静态html文件异常。课程id为：{}", courseId);
            zxstException.cast("上传静态文件异常");
        }
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String upload = mediaServiceClient.upload(multipartFile, "course/" + courseId + ".html");
        if (StringUtils.isEmpty(upload)) {
            log.error("远程调用，上传静态html文件异常。课程id为：{}", courseId);
            zxstException.cast("上传静态文件异常");
        }
    }

    private void saveCoursePublishMessage(Long courseId) {
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if (mqMessage == null) {
            zxstException.cast(CommonError.UNKOWN_ERROR);
        }
    }
}
