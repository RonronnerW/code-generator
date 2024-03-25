package com.wang.template;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TemplateMaker {
    /**
     *
     * @param meta 元信息
     * @param id
     * @param originProjectPath 原始路径信息
     * @param filePaths 文件输入路径 支持多个文件
     * @param modelInfo 数据模型信息
     * @param searchStr 原文件替换信息
     * @return
     */
    private static long makeTemplate(Meta meta, Long id, String originProjectPath,List<String> filePaths, Meta.ModelConfig.ModelInfo modelInfo, String searchStr) {
        // 没有 id 则生成
        if (id == null) {
            id = IdUtil.getSnowflakeNextId();
        }
        String projectPath = System.getProperty("user.dir");
        String tempDirPath = projectPath + File.separator + ".temp";
        String templatePath = tempDirPath + File.separator + FileUtil.getLastPathEle(Paths.get(originProjectPath)).toString()+"-"+id;

        // 判断：是否为首次制作
        // 目录不存在，则是首次制作，复制目录
        if (!FileUtil.exist(templatePath)) {
            FileUtil.mkdir(templatePath);
            FileUtil.copy(originProjectPath, templatePath, true);
        }

        // 一、输入信息
        // 2. 输入文件信息
        // originProjectPath 最后一层目录 acm-template (这里)
        String sourceRootPath = templatePath + File.separator + FileUtil.getLastPathEle(Paths.get(originProjectPath)).toString();
//        sourceRootPath = sourceRootPath.replaceAll("\\\\","/");

        // 二 生成模板
        // 输入文件为目录（这里，加上判断逻辑）
        List<Meta.FileConfig.FilesInfo> newFileInfoList = new ArrayList<>();
        for (String filePath : filePaths) {

            String inputFileAbsolutePath = sourceRootPath + File.separator + filePath;
            if(FileUtil.isDirectory(inputFileAbsolutePath)) {
                List<File> files = FileUtil.loopFiles(inputFileAbsolutePath);
                for (File file : files) {
                    Meta.FileConfig.FilesInfo filesInfo = makeFileTemplate(modelInfo, searchStr, sourceRootPath, file);
                    newFileInfoList.add(filesInfo);
                }
            } else {
                Meta.FileConfig.FilesInfo filesInfo = makeFileTemplate(modelInfo, searchStr, sourceRootPath, new File(inputFileAbsolutePath));
                newFileInfoList.add(filesInfo);
            }
        }

        // 三 创建meta.json 信息
        String metaPath = sourceRootPath+File.separator+"meta.json";
        if(FileUtil.exist(metaPath)) {
            // 存在meta文件 追加
            Meta oldMeta = JSONUtil.toBean(FileUtil.readUtf8String(metaPath), Meta.class);
            BeanUtil.copyProperties(meta, oldMeta, CopyOptions.create().ignoreNullValue());
            meta = oldMeta;

            List<Meta.FileConfig.FilesInfo> files = oldMeta.getFileConfig().getFiles();
            files.addAll(newFileInfoList);

            List<Meta.ModelConfig.ModelInfo> models = oldMeta.getModelConfig().getModels();
            models.add(modelInfo);
            //去重
            oldMeta.getFileConfig().setFiles(distinctFiles(files));
            oldMeta.getModelConfig().setModels(distinctModels(models));

            // 输出元信息 meta.json
//            FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(oldMeta), metaPath);
        } else {
            // 不存在
            Meta.FileConfig fileConfig = new Meta.FileConfig();
            fileConfig.setSourceRootPath(sourceRootPath.replaceAll("\\\\", "/"));

            ArrayList<Meta.FileConfig.FilesInfo> filesInfos = new ArrayList<>(newFileInfoList);
            fileConfig.setFiles(filesInfos);
            meta.setFileConfig(fileConfig);

            Meta.ModelConfig modelConfig = new Meta.ModelConfig();
            ArrayList<Meta.ModelConfig.ModelInfo> modelInfos = new ArrayList<>();
            modelInfos.add(modelInfo);
            modelConfig.setModels(modelInfos);
            meta.setModelConfig(modelConfig);
        }
        // 输出元信息 meta.json
        FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(meta), metaPath);

        return id;
    }

    private static Meta.FileConfig.FilesInfo makeFileTemplate(Meta.ModelConfig.ModelInfo modelInfo, String searchStr, String sourceRootPath, File filePath) {
        // 获取绝对文件绝对路径
        String fileInputPathAbsolute = filePath.getAbsolutePath().replaceAll("\\\\","/");
        String fileOutputPathAbsolute = fileInputPathAbsolute + ".ftl";
        sourceRootPath = sourceRootPath.replaceAll("\\\\","/");
        // 二 替换原来的内容, 生成模板文件
        // 1. 读取文件 相对路径
        String inputFilePath = fileInputPathAbsolute.replaceAll(sourceRootPath+"/", "");

        String outputFilePath = fileOutputPathAbsolute.replaceAll(sourceRootPath+"/", "");;
        // 如果已有.ftl 文件 非首次制作
        String originContent = null;
        if(FileUtil.exist(fileOutputPathAbsolute)) {
            originContent = FileUtil.readUtf8String(fileOutputPathAbsolute);
        } else {
            // 读取原文件
            originContent = FileUtil.readUtf8String(fileInputPathAbsolute);
        }
        String replaceContent = String.format("${%s}", modelInfo.getFieldName());
        // 替换文件
        String newFileContent = StringUtil.replace(originContent, searchStr, replaceContent);

        // 文件配置信息
        Meta.FileConfig.FilesInfo filesInfo = new Meta.FileConfig.FilesInfo();
        filesInfo.setInputPath(inputFilePath);
        filesInfo.setOutputPath(outputFilePath);
        filesInfo.setType(FileTypeEnum.FILE.getValue());
        filesInfo.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());
        if (newFileContent.equals(originContent)) {
            // 输出路径 = 输入路径
//            filesInfo.setOutputPath(inputFilePath);
//            filesInfo.setGenerateType(FileGenerateTypeEnum.STATIC.getValue());
        } else {
            // 生成模板文件
            filesInfo.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());
            // 输出
            File file = FileUtil.writeUtf8String(newFileContent, fileOutputPathAbsolute);
        }
        return filesInfo;
    }

    /**
     * 模型去重
     *
     * @param modelInfoList
     * @return
     */
    private static List<Meta.ModelConfig.ModelInfo> distinctModels(List<Meta.ModelConfig.ModelInfo> modelInfoList) {
//        toMap: 第一个参数为key，第二个参数为value（o->o表示原对象作为value）,第三个参数为方法（e表示原值，r表示重复值）
        List<Meta.ModelConfig.ModelInfo> newModelInfoList = new ArrayList<>(
                modelInfoList.stream()
                        .collect(Collectors.toMap(Meta.ModelConfig.ModelInfo::getFieldName, o -> o, (e, r) -> r))
                        .values()
        );
        return newModelInfoList;
    }
    /**
     * 文件去重
     *
     * @param fileInfoList
     * @return
     */
    private static List<Meta.FileConfig.FilesInfo> distinctFiles(List<Meta.FileConfig.FilesInfo> fileInfoList) {
        List<Meta.FileConfig.FilesInfo> newFileInfoList = new ArrayList<>(
                fileInfoList.stream()
                        .collect(Collectors.toMap(Meta.FileConfig.FilesInfo::getInputPath, o -> o, (e, r) -> r))
                        .values()
        );
        return newFileInfoList;
    }

    public static void main(String[] args) {

        Meta meta = new Meta();
        meta.setName("acm-template-pro-generator");
        meta.setDescription("ACM 示例模板生成器");

        String projectPath = System.getProperty("user.dir");
        String originProjectPath = projectPath + File.separator + "generator-demo/springboot-init/springboot-init";
        String fileInputPath1 = "src/main/java/com/wang/springbootinit/common";
        String fileInputPath2 = "src/main/java/com/wang/springbootinit/controller";
        List<String> filePaths = Arrays.asList(fileInputPath1, fileInputPath2);
        // 模型参数信息（首次）
//        Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
//        modelInfo.setFieldName("outputText");
//        modelInfo.setType("String");
//        modelInfo.setDescription("mySum = ");

        // 模型参数信息（第二次）
        Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
        modelInfo.setFieldName("className");
        modelInfo.setType("String");

        // 替换变量（首次）
//        String searchStr = "Sum: ";

        // 替换变量（第二次）
        String searchStr = "BaseResponse";

        long id = makeTemplate(meta, 1L, originProjectPath, filePaths, modelInfo, searchStr);
        System.out.println(id);
    }

}
