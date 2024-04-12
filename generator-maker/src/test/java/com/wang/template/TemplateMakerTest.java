package com.wang.template;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;
import com.wang.generator.main.MainGenerator;
import com.wang.meta.Meta;
import com.wang.template.enums.FileFilterRangeEnum;
import com.wang.template.enums.FileFilterRuleEnum;
import com.wang.template.model.*;
import freemarker.template.TemplateException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TemplateMakerTest {
    @Test
    public void test1() {

        Meta meta = new Meta();
        meta.setName("acm-template-pro-generator");
        meta.setDescription("ACM 示例模板生成器");

        String projectPath = System.getProperty("user.dir");
        String originProjectPath = new File(projectPath).getParent() + File.separator + "generator-demo/springboot-init/springboot-init";
        String fileInputPath1 = "src/main/java/com/wang/springbootinit/common";
        String fileInputPath2 = "src/main/resources/application.yml";


        // 文件过滤
        TemplateMakerFileConfig templateMakerFileConfig = new TemplateMakerFileConfig();
        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig1 = new TemplateMakerFileConfig.FileInfoConfig();
        fileInfoConfig1.setPath(fileInputPath1);
        List<FileFilterConfig> fileFilterConfigList = new ArrayList<>();
        FileFilterConfig fileFilterConfig = FileFilterConfig.builder()
                .range(FileFilterRangeEnum.FILE_NAME.getValue())
                .rule(FileFilterRuleEnum.CONTAINS.getValue())
                .value("Base")
                .build();
        fileFilterConfigList.add(fileFilterConfig);
        fileInfoConfig1.setFileFilterConfigList(fileFilterConfigList);

        templateMakerFileConfig.setFiles(Arrays.asList(fileInfoConfig1));

        // 文件分组
        TemplateMakerFileConfig.FileGroupConfig fileGroupConfig = new TemplateMakerFileConfig.FileGroupConfig();
        fileGroupConfig.setCondition("outputText2");
        fileGroupConfig.setGroupKey("mysql");
        fileGroupConfig.setGroupName("测试分组2");
        templateMakerFileConfig.setFileGroupConfig(fileGroupConfig);


        // 模型分组
        TemplateMakerModelConfig templateMakerModelConfig = new TemplateMakerModelConfig();
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = new TemplateMakerModelConfig.ModelGroupConfig();
        modelGroupConfig.setGroupKey("mysql");
        modelGroupConfig.setGroupName("数据库配置");
        templateMakerModelConfig.setModelGroupConfig(modelGroupConfig);
        TemplateMakerModelConfig.ModelInfo modelInfo1 = new TemplateMakerModelConfig.ModelInfo();
        modelInfo1.setFieldName("url");
        modelInfo1.setType("String");
        modelInfo1.setDescription("url");
        modelInfo1.setDefaultValue("jdbc:mysql://localhost:3306/my_db");
        modelInfo1.setReplaceText("jdbc:mysql://localhost:3306/my_db");

        TemplateMakerModelConfig.ModelInfo modelInfo2 = new TemplateMakerModelConfig.ModelInfo();
        modelInfo2.setFieldName("classname");
        modelInfo2.setType("String");
        modelInfo2.setDescription("classname");
        modelInfo2.setDefaultValue("BaseResponse");
        modelInfo2.setReplaceText("BaseResponse");
        List<TemplateMakerModelConfig.ModelInfo> modelInfos = Arrays.asList(modelInfo1, modelInfo2);
        templateMakerModelConfig.setModels(modelInfos);
        TemplateMakerOutputConfig templateMakerOutputConfig = new TemplateMakerOutputConfig();
        long id = TemplateMaker.makeTemplate(meta, 1L, originProjectPath, templateMakerFileConfig, templateMakerModelConfig, templateMakerOutputConfig);
        System.out.println(id);
    }

    /**
     * 测试 Bug 修复1：同配置多次生成，强制变为静态生成
     */
    @Test
    public void testMakeTemplateBug1() {
        Meta meta = new Meta();
        meta.setName("spring-boot-init-generator");
        meta.setDescription("Spring Boot 初始化模板生成器");

        String projectPath = System.getProperty("user.dir");
        String originProjectPath = new File(projectPath).getParent() + File.separator + "generator-demo/springboot-init/springboot-init";

        // 文件参数配置
        String inputFilePath1 = "src/main/resources/application.yml";
        TemplateMakerFileConfig templateMakerFileConfig = new TemplateMakerFileConfig();
        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig1 = new TemplateMakerFileConfig.FileInfoConfig();
        fileInfoConfig1.setPath(inputFilePath1);
        templateMakerFileConfig.setFiles(Arrays.asList(fileInfoConfig1));

        // 模型参数配置
        TemplateMakerModelConfig templateMakerModelConfig = new TemplateMakerModelConfig();

        // - 模型配置
        TemplateMakerModelConfig.ModelInfo modelInfoConfig1 = new TemplateMakerModelConfig.ModelInfo();
        modelInfoConfig1.setFieldName("url");
        modelInfoConfig1.setType("String");
        modelInfoConfig1.setDefaultValue("jdbc:mysql://localhost:3306/my_db");
        modelInfoConfig1.setReplaceText("jdbc:mysql://localhost:3306/my_db");
        templateMakerModelConfig.setModels(Arrays.asList(modelInfoConfig1));
        TemplateMakerOutputConfig templateMakerOutputConfig = new TemplateMakerOutputConfig();
        long id = TemplateMaker.makeTemplate(meta, 1744705904383320064L, originProjectPath, templateMakerFileConfig, templateMakerModelConfig, templateMakerOutputConfig);
        System.out.println(id);
    }

    /**
     * 测试 Bug 修复2：同目录多次生成时，会扫描新的 .ftl 文件
     */
    @Test
    public void testMakeTemplateBug2() {
        Meta meta = new Meta();
        meta.setName("spring-boot-init-generator");
        meta.setDescription("Spring Boot 初始化模板生成器");

        String projectPath = System.getProperty("user.dir");
        String originProjectPath = new File(projectPath).getParent() + File.separator + "generator-demo/springboot-init/springboot-init";

        // 文件参数配置
        String fileInputPath1 = "./";
        TemplateMakerFileConfig templateMakerFileConfig = new TemplateMakerFileConfig();
        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig1 = new TemplateMakerFileConfig.FileInfoConfig();
        fileInfoConfig1.setPath(fileInputPath1);
        templateMakerFileConfig.setFiles(Arrays.asList(fileInfoConfig1));

        // 模型参数配置
        TemplateMakerModelConfig templateMakerModelConfig = new TemplateMakerModelConfig();
        TemplateMakerModelConfig.ModelInfo modelInfoConfig1 = new TemplateMakerModelConfig.ModelInfo();
        modelInfoConfig1.setFieldName("className");
        modelInfoConfig1.setType("String");
        modelInfoConfig1.setReplaceText("BaseResponse");
        templateMakerModelConfig.setModels(Arrays.asList(modelInfoConfig1));
        TemplateMakerOutputConfig templateMakerOutputConfig = new TemplateMakerOutputConfig();
        long id = TemplateMaker.makeTemplate(meta, 1744712881222180864L, originProjectPath, templateMakerFileConfig, templateMakerModelConfig, templateMakerOutputConfig);
        System.out.println(id);
    }

    @Test
    public void templateConfigTest() {
        String templateConfig = ResourceUtil.readUtf8Str("TemplateMaker.json");
        TemplateMakerConfig bean = JSONUtil.toBean(templateConfig, TemplateMakerConfig.class);
        long id = TemplateMaker.makeTemplate(bean);
        System.out.println(id);
    }

    /**
     * 制作 SpringBoot 模板
     */
    @Test
    public void makeSpringBootTemplate() {
        String configStr = ResourceUtil.readUtf8Str("example/templateMaker1.json");
        TemplateMakerConfig templateMakerConfig = JSONUtil.toBean(configStr, TemplateMakerConfig.class);
        long id = TemplateMaker.makeTemplate(templateMakerConfig);

        configStr = ResourceUtil.readUtf8Str("example/templateMaker2.json");
        templateMakerConfig = JSONUtil.toBean(configStr, TemplateMakerConfig.class);
        TemplateMaker.makeTemplate(templateMakerConfig);

        configStr = ResourceUtil.readUtf8Str("example/templateMaker3.json");
        templateMakerConfig = JSONUtil.toBean(configStr, TemplateMakerConfig.class);
        TemplateMaker.makeTemplate(templateMakerConfig);

        configStr = ResourceUtil.readUtf8Str("example/templateMaker4.json");
        templateMakerConfig = JSONUtil.toBean(configStr, TemplateMakerConfig.class);
        TemplateMaker.makeTemplate(templateMakerConfig);

        configStr = ResourceUtil.readUtf8Str("example/templateMaker5.json");
        templateMakerConfig = JSONUtil.toBean(configStr, TemplateMakerConfig.class);
        TemplateMaker.makeTemplate(templateMakerConfig);

        configStr = ResourceUtil.readUtf8Str("example/templateMaker5_1.json");
        templateMakerConfig = JSONUtil.toBean(configStr, TemplateMakerConfig.class);
        TemplateMaker.makeTemplate(templateMakerConfig);

        configStr = ResourceUtil.readUtf8Str("example/templateMaker7.json");
        templateMakerConfig = JSONUtil.toBean(configStr, TemplateMakerConfig.class);
        TemplateMaker.makeTemplate(templateMakerConfig);

        configStr = ResourceUtil.readUtf8Str("example/templateMaker8.json");
        templateMakerConfig = JSONUtil.toBean(configStr, TemplateMakerConfig.class);
        TemplateMaker.makeTemplate(templateMakerConfig);


        System.out.println(id);
    }

    @Test
    public void testMainGenerate() throws TemplateException, IOException, InterruptedException {
        new MainGenerator().doGenerate();
    }


}