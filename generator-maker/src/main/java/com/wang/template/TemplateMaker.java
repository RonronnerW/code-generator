package com.wang.template;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.wang.meta.Meta;
import com.wang.meta.enums.FileGenerateTypeEnum;
import com.wang.meta.enums.FileTypeEnum;
import com.wang.template.enums.FileFilterRangeEnum;
import com.wang.template.enums.FileFilterRuleEnum;
import com.wang.template.model.FileFilterConfig;
import com.wang.template.model.TemplateMakerFileConfig;
import freemarker.template.utility.StringUtil;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class TemplateMaker {
    /**
     * @param meta                    元信息
     * @param id
     * @param originProjectPath       原始路径信息
     * @param templateMakerFileConfig 文件配置
     * @param modelInfo               数据模型信息
     * @param searchStr               原文件替换信息
     * @return
     */
    private static long makeTemplate(Meta meta, Long id, String originProjectPath, TemplateMakerFileConfig templateMakerFileConfig, Meta.ModelConfig.ModelInfo modelInfo, String searchStr) {
        // 没有 id 则生成
        if (id == null) {
            id = IdUtil.getSnowflakeNextId();
        }
        String projectPath = System.getProperty("user.dir");
        String tempDirPath = projectPath + File.separator + ".temp";
        String templatePath = tempDirPath + File.separator + FileUtil.getLastPathEle(Paths.get(originProjectPath)).toString() + "-" + id;

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
        //
        List<Meta.FileConfig.FilesInfo> newFileInfoList = new ArrayList<>();
        List<TemplateMakerFileConfig.FileInfoConfig> fileInfoConfigList = templateMakerFileConfig.getFiles();
        for (TemplateMakerFileConfig.FileInfoConfig fileInfoConfig : fileInfoConfigList) {
            String inputPath = fileInfoConfig.getPath();
            // （从这里开始改）如果是相对路径，要改为绝对路径
            if (!inputPath.startsWith(sourceRootPath)) {
                inputPath = sourceRootPath + File.separator + inputPath;
            }
            // 应用过滤器，获取过滤后的文件列表
            List<File> fileList = FileFilter.doFilter(inputPath, fileInfoConfig.getFileFilterConfigList());
            for (File file : fileList) {
                Meta.FileConfig.FilesInfo fileInfo = makeFileTemplate(modelInfo, searchStr, sourceRootPath, file);
                newFileInfoList.add(fileInfo);
            }
        }

        List<Meta.FileConfig.FilesInfo> noFtlFileInfoList = newFileInfoList.stream().filter(filesInfo -> !"ftl".equals(FileUtil.getSuffix(filesInfo.getInputPath()))).collect(Collectors.toList());
        newFileInfoList = noFtlFileInfoList;

        // 如果是文件分组
        TemplateMakerFileConfig.FilesGroupInfo filesGroupInfo = templateMakerFileConfig.getFilesGroupInfo();
        if (filesGroupInfo != null) {
            String condition = filesGroupInfo.getCondition();
            String groupKey = filesGroupInfo.getGroupKey();
            String groupName = filesGroupInfo.getGroupName();
            // 分组配置
            Meta.FileConfig.FilesInfo filesInfo = new Meta.FileConfig.FilesInfo();
            filesInfo.setType(FileTypeEnum.GROUP.getValue());
            filesInfo.setCondition(condition);
            filesInfo.setGroupKey(groupKey);
            filesInfo.setGroupName(groupName);
            filesInfo.setFiles(newFileInfoList);
            newFileInfoList = new ArrayList<>();
            newFileInfoList.add(filesInfo);
        }

        // 三 创建meta.json 信息
        String metaPath = sourceRootPath + File.separator + "meta.json";
        if (FileUtil.exist(metaPath)) {
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
        String fileInputPathAbsolute = filePath.getAbsolutePath().replaceAll("\\\\", "/");
        String fileOutputPathAbsolute = fileInputPathAbsolute + ".ftl";
        sourceRootPath = sourceRootPath.replaceAll("\\\\", "/");
        // 二 替换原来的内容, 生成模板文件
        // 1. 读取文件 相对路径
        String inputFilePath = fileInputPathAbsolute.replaceAll(sourceRootPath + "/", "");

        String outputFilePath = fileOutputPathAbsolute.replaceAll(sourceRootPath + "/", "");
        ;
        // 如果已有.ftl 文件 非首次制作
        String originContent = null;
        if (FileUtil.exist(fileOutputPathAbsolute)) {
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
        // 分组去重
        // 1. 判断是否有分组信息然后根据groupKey进行分组
        Map<String, List<Meta.FileConfig.FilesInfo>> fileGroupMap = fileInfoList.stream()
                .filter(filesInfo -> StrUtil.isNotBlank(filesInfo.getGroupKey()))
                .collect(Collectors.groupingBy(Meta.FileConfig.FilesInfo::getGroupKey)); // groupingBy 是根据参数进行分组
        // 2. 同组内进行去重
        Map<String, Meta.FileConfig.FilesInfo> newFileGroupMap = new HashMap<>();
        for(Map.Entry<String, List<Meta.FileConfig.FilesInfo>> entry: fileGroupMap.entrySet()) {
            List<Meta.FileConfig.FilesInfo> fileInfos = entry.getValue();
            List<Meta.FileConfig.FilesInfo> distinctFileInfos = new ArrayList<>(fileInfos.stream()
                    .flatMap(filesInfo -> filesInfo.getFiles().stream()) // 将内容展平
                    .collect(Collectors.toMap(Meta.FileConfig.FilesInfo::getInputPath, o -> o, (e, r) -> r))//去重
//                    .distinct() // distinct 方法是基于equals的
                    .values());
            // groupKey 相同 更新其他内容
            Meta.FileConfig.FilesInfo newFileInfos = CollUtil.getLast(fileInfos);// 获取新的分组信息
            newFileInfos.setFiles(distinctFileInfos); // 设置去重后的文件信息
            newFileGroupMap.put(entry.getKey(), newFileInfos);
        }
        // 3. 将文件信息添加到结果列表
        ArrayList<Meta.FileConfig.FilesInfo> resultFilesInfos = new ArrayList<>(newFileGroupMap.values());
        // 4. 未分组的文件添加到结果表
        List<Meta.FileConfig.FilesInfo> noGroupFileInfoList = new ArrayList<>(
                fileInfoList.stream()
                        .filter(filesInfo -> StrUtil.isBlank(filesInfo.getGroupKey()))
                        .collect(Collectors.toMap(Meta.FileConfig.FilesInfo::getInputPath, o -> o, (e, r) -> r))//去重
                        .values()
        );

        resultFilesInfos.addAll(noGroupFileInfoList);
        return resultFilesInfos;
    }

    public static void main(String[] args) {

        Meta meta = new Meta();
        meta.setName("acm-template-pro-generator");
        meta.setDescription("ACM 示例模板生成器");

        String projectPath = System.getProperty("user.dir");
        String originProjectPath = projectPath + File.separator + "generator-demo/springboot-init/springboot-init";
        String fileInputPath1 = "src/main/java/com/wang/springbootinit/common";
        String fileInputPath2 = "src/main/java/com/wang/springbootinit/constant";
        List<String> filePaths = Arrays.asList(fileInputPath1, fileInputPath2);
        // 模型参数信息（首次）
//        Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
//        modelInfo.setFieldName("outputText");
//        modelInfo.setType("String");
//        modelInfo.setDescription("mySum = ");

        // 模型参数信息（第二次）
        Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
        modelInfo.setFieldName("classname");
        modelInfo.setType("string");

        // 替换变量（首次）
//        String searchStr = "Sum: ";

        // 替换变量（第二次）
        String searchStr = "BaseResponse";
        TemplateMakerFileConfig templateMakerFileConfig = new TemplateMakerFileConfig();
        TemplateMakerFileConfig.FilesGroupInfo filesGroupInfo = new TemplateMakerFileConfig.FilesGroupInfo();
        filesGroupInfo.setGroupKey("test2");
        filesGroupInfo.setGroupName("测试222");
        filesGroupInfo.setCondition("loop2");
        templateMakerFileConfig.setFilesGroupInfo(filesGroupInfo);
        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig1 = new TemplateMakerFileConfig.FileInfoConfig();
        fileInfoConfig1.setPath(fileInputPath1);
        FileFilterConfig fileFilterConfig = FileFilterConfig.builder()
                .range(FileFilterRangeEnum.FILE_NAME.getValue())
                .value("Base")
                .rule(FileFilterRuleEnum.CONTAINS.getValue())
                .build();
        List<FileFilterConfig> fileFilterConfigList = Arrays.asList(fileFilterConfig);
        fileInfoConfig1.setFileFilterConfigList(fileFilterConfigList);
        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig2 = new TemplateMakerFileConfig.FileInfoConfig();
        fileInfoConfig2.setPath(fileInputPath2);
        List<TemplateMakerFileConfig.FileInfoConfig> fileInfoConfigList = Arrays.asList(fileInfoConfig1, fileInfoConfig2);
        templateMakerFileConfig.setFiles(fileInfoConfigList);

        long id = makeTemplate(meta, 1L, originProjectPath, templateMakerFileConfig, modelInfo, searchStr);
        System.out.println(id);
    }

}
