package com.wang.template;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.wang.meta.Meta;
import com.wang.meta.enums.FileGenerateTypeEnum;
import com.wang.meta.enums.FileTypeEnum;
import freemarker.template.utility.StringUtil;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;

public class TemplateMaker {
    public static void main(String[] args) {

        // 2. 输入文件信息 code-generator
        String projectPath = System.getProperty("user.dir");
        String originProjectPath = projectPath + File.separator + "generator-demo/acm-template";
        String sourcePath = projectPath+File.separator +".source";
        // 工作空间隔离
        // 工作空间隔离：2.复制目录
        long id = IdUtil.getSnowflakeNextId();
        String tempDirPath = projectPath + File.separator + ".temp";
        String templatePath = tempDirPath + File.separator +FileUtil.getLastPathEle(Paths.get(originProjectPath)).toString()+"-"+id;
        if (!FileUtil.exist(templatePath)) {
            FileUtil.mkdir(templatePath);
        }
        FileUtil.copy(originProjectPath,templatePath,true);

        // 一、输入信息
        // 1. 输入项目基本信息
        String name = "acm-template-pro-generator";
        String description = "ACM 示例模板生成器";

        // 2. 输入文件信息
        // originProjectPath 最后一层目录 acm-template (这里)
        String sourceRootPath = templatePath + File.separator + FileUtil.getLastPathEle(Paths.get(originProjectPath)).toString();
        // 注意 win 系统需要对路径进行转义
        sourceRootPath = sourceRootPath.replaceAll("\\\\", "/");

        String fileInputPath = "src/com/wang/acm/MainTemplate.java";
        String fileOutputPath = fileInputPath + ".ftl";

        // 3. 输入模型参数信息
        Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
        modelInfo.setFieldName("outputText");
        modelInfo.setType("String");
        modelInfo.setDescription("mySum = ");

        // 二 替换原来的内容, 生成模板文件
        // 1. 读取文件
        String inputFilePath = sourceRootPath+File.separator+fileInputPath;
        String originContent = FileUtil.readUtf8String(inputFilePath);
        String replaceContent = String.format("${%s}", modelInfo.getFieldName());
        // 替换文件
        String newFileContent = StringUtil.replace(originContent, "Sum: ", replaceContent);
        // 输出
        String outputFilePath = sourceRootPath+File.separator+fileOutputPath;
        File file = FileUtil.writeUtf8String(newFileContent, outputFilePath);

        // 三 创建meta.json 信息
        String metaPath = sourceRootPath+File.separator+"meta.json";
        // 填充对象
        Meta meta = new Meta();
        meta.setName(name);
        meta.setDescription(description);

        Meta.FileConfig fileConfig = new Meta.FileConfig();
        fileConfig.setInputRootPath(fileInputPath);
        fileConfig.setOutputRootPath(fileOutputPath);
        fileConfig.setSourceRootPath(sourceRootPath);
        Meta.FileConfig.FilesInfo filesInfo = new Meta.FileConfig.FilesInfo();
        filesInfo.setInputPath(fileInputPath);
        filesInfo.setOutputPath(fileOutputPath);
        filesInfo.setType(FileTypeEnum.FILE.getValue());
        filesInfo.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());
        ArrayList<Meta.FileConfig.FilesInfo> filesInfos = new ArrayList<>();
        filesInfos.add(filesInfo);
        fileConfig.setFiles(filesInfos);
        meta.setFileConfig(fileConfig);

        Meta.ModelConfig modelConfig = new Meta.ModelConfig();
        ArrayList<Meta.ModelConfig.ModelInfo> modelInfos = new ArrayList<>();
        modelInfos.add(modelInfo);
        modelConfig.setModels(modelInfos);
        meta.setModelConfig(modelConfig);

        // 输出元信息 meta.json
        FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(meta), metaPath);

    }
}
