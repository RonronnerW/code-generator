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
import com.wang.template.model.TemplateMakerConfig;
import com.wang.template.model.TemplateMakerFileConfig;
import com.wang.template.model.TemplateMakerModelConfig;
import freemarker.template.utility.StringUtil;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class TemplateMaker {
    /**
     * 封装参数
     * @param templateMakerConfig 制作模板参数
     * @return id
     */
    public static long makeTemplate(TemplateMakerConfig templateMakerConfig) {
        Long id = templateMakerConfig.getId();
        Meta meta = templateMakerConfig.getMeta();
        String originProjectPath = templateMakerConfig.getOriginProjectPath();
        TemplateMakerFileConfig fileConfig = templateMakerConfig.getFileConfig();
        TemplateMakerModelConfig modelConfig = templateMakerConfig.getModelConfig();
        return makeTemplate(meta, id, originProjectPath, fileConfig, modelConfig);
    }
    /**
     * @param meta                    元信息
     * @param id                        id
     * @param originProjectPath       原始路径信息
     * @param templateMakerFileConfig 文件配置
     * @param templateMakerModelConfig 数据模型信息和替换内容
     * @return id
     */
    public static long makeTemplate(Meta meta, Long id, String originProjectPath, TemplateMakerFileConfig templateMakerFileConfig, TemplateMakerModelConfig templateMakerModelConfig) {
        // 没有 id 则生成
        if (id == null) {
            id = IdUtil.getSnowflakeNextId();
        }
        String projectPath = System.getProperty("user.dir");
        projectPath = new File(projectPath).getParent();
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
                Meta.FileConfig.FilesInfo fileInfo = makeFileTemplate(templateMakerModelConfig, sourceRootPath, file);
                newFileInfoList.add(fileInfo);
            }
        }
        // ftl 文件不作为处理文件
        List<Meta.FileConfig.FilesInfo> noFtlFileInfoList = newFileInfoList.stream().filter(filesInfo -> !"ftl".equals(FileUtil.getSuffix(filesInfo.getOutputPath()))).collect(Collectors.toList());
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

        // 处理模型信息
        List<TemplateMakerModelConfig.ModelInfo> modelConfigLists = templateMakerModelConfig.getModels();
        // 转换为Meta.ModelConfig.ModelInfo
        List<Meta.ModelConfig.ModelInfo> inputModelInfoLists = modelConfigLists.stream().map(models -> {
            Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
            BeanUtil.copyProperties(models, modelInfo);
            return modelInfo;
        }).toList();
        
        // 本次新增的模型配置列表
        List<Meta.ModelConfig.ModelInfo> newModelInfoLists = new ArrayList<>();

        TemplateMakerModelConfig.ModelGroupInfo modelGroupInfo = templateMakerModelConfig.getModelGroupInfo();
        // 分组
        if(modelGroupInfo!=null) {
            String condition = modelGroupInfo.getCondition();
            String groupKey = modelGroupInfo.getGroupKey();
            String groupName = modelGroupInfo.getGroupName();
            TemplateMakerModelConfig.ModelGroupInfo newModelGroupInfo = new TemplateMakerModelConfig.ModelGroupInfo();
            Meta.ModelConfig.ModelInfo groupModelInfo = new Meta.ModelConfig.ModelInfo();
            groupModelInfo.setModels(inputModelInfoLists);
            groupModelInfo.setGroupKey(groupKey);
            groupModelInfo.setCondition(condition);
            groupModelInfo.setGroupName(groupName);
            newModelInfoLists.add(groupModelInfo);
        } else {
            newModelInfoLists.addAll(inputModelInfoLists);
        }

        // 三 创建meta.json 信息
        String metaPath = templatePath + File.separator + "meta.json";
        if (FileUtil.exist(metaPath)) {
            // 存在meta文件 追加
            Meta oldMeta = JSONUtil.toBean(FileUtil.readUtf8String(metaPath), Meta.class);
            BeanUtil.copyProperties(meta, oldMeta, CopyOptions.create().ignoreNullValue());
            meta = oldMeta;

            List<Meta.FileConfig.FilesInfo> files = oldMeta.getFileConfig().getFiles();
            files.addAll(newFileInfoList);

            List<Meta.ModelConfig.ModelInfo> models = oldMeta.getModelConfig().getModels();
            models.addAll(newModelInfoLists);
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
            modelInfos.addAll(newModelInfoLists);
            modelConfig.setModels(modelInfos);
            meta.setModelConfig(modelConfig);
        }
        // 输出元信息 meta.json
        FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(meta), metaPath);

        return id;
    }

    private static Meta.FileConfig.FilesInfo makeFileTemplate(TemplateMakerModelConfig templateMakerModelConfig, String sourceRootPath, File filePath) {
        // 获取绝对文件绝对路径
        String fileInputPathAbsolute = filePath.getAbsolutePath().replaceAll("\\\\", "/");
        String fileOutputPathAbsolute = fileInputPathAbsolute + ".ftl";
        sourceRootPath = sourceRootPath.replaceAll("\\\\", "/");
        // 二 替换原来的内容, 生成模板文件
        // 1. 读取文件 相对路径
        String inputFilePath = fileInputPathAbsolute.replaceAll(sourceRootPath + "/", "");

        String outputFilePath = fileOutputPathAbsolute.replaceAll(sourceRootPath + "/", "");

        // 如果已有.ftl 文件 非首次制作
        String originContent = null;
        boolean hasTemplateFile = FileUtil.exist(fileOutputPathAbsolute);
        if (hasTemplateFile) {
            originContent = FileUtil.readUtf8String(fileOutputPathAbsolute);
        } else {
            // 读取原文件
            originContent = FileUtil.readUtf8String(fileInputPathAbsolute);
        }
//        String replaceContent = String.format("${%s}", modelInfo.getFieldName());
//        // 替换文件
//        String newFileContent = StringUtil.replace(originContent, searchStr, replaceContent);

        String newFileContent = originContent;
        String replaceContent;
        TemplateMakerModelConfig.ModelGroupInfo modelGroupInfo = templateMakerModelConfig.getModelGroupInfo();
        for (TemplateMakerModelConfig.ModelInfo model : templateMakerModelConfig.getModels()) {
            String fieldName = model.getFieldName();
            if(modelGroupInfo==null) {
                replaceContent = String.format("${%s}", fieldName);
            } else {
                // 是分组，“挖坑”多一个层级
                String groupKey = modelGroupInfo.getGroupKey();
                replaceContent = String.format("${%s.%s}", groupKey,fieldName);
            }
            // 多次替换
            newFileContent = StrUtil.replace(newFileContent, model.getReplaceText(), replaceContent);
        }

        // 文件配置信息
        Meta.FileConfig.FilesInfo filesInfo = new Meta.FileConfig.FilesInfo();
        filesInfo.setInputPath(outputFilePath);
        filesInfo.setOutputPath(inputFilePath);
        filesInfo.setType(FileTypeEnum.FILE.getValue());
        filesInfo.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());
        boolean equals = newFileContent.equals(originContent);
        // 有模板文件 判断有没有修改 有修改生成模板
        if(!hasTemplateFile) {
            if(equals) {
                filesInfo.setInputPath(inputFilePath);
                filesInfo.setGenerateType(FileGenerateTypeEnum.STATIC.getValue());
            } else {
                FileUtil.writeUtf8String(newFileContent, fileOutputPathAbsolute);
            }
        } else if(!equals) {
            // 没有模板文件 直接生成
            FileUtil.writeUtf8String(newFileContent, fileOutputPathAbsolute);
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
        // 分组去重
        // 1. 判断是否有分组信息然后根据groupKey进行分组
        Map<String, List<Meta.ModelConfig.ModelInfo>> ModelGroupMap = modelInfoList.stream()
                .filter(ModelsInfo -> StrUtil.isNotBlank(ModelsInfo.getGroupKey()))
                .collect(Collectors.groupingBy(Meta.ModelConfig.ModelInfo::getGroupKey)); // groupingBy 是根据参数进行分组
        // 2. 同组内进行去重
        Map<String, Meta.ModelConfig.ModelInfo> newModelGroupMap = new HashMap<>();
        for(Map.Entry<String, List<Meta.ModelConfig.ModelInfo>> entry: ModelGroupMap.entrySet()) {
            List<Meta.ModelConfig.ModelInfo> ModelInfos = entry.getValue();
            List<Meta.ModelConfig.ModelInfo> distinctModelInfos = new ArrayList<>(ModelInfos.stream()
                    .flatMap(ModelsInfo -> ModelsInfo.getModels().stream()) // 将内容展平
                    .collect(Collectors.toMap(Meta.ModelConfig.ModelInfo::getFieldName, o -> o, (e, r) -> r))//去重
                    .values());
            // groupKey 相同 更新其他内容
            Meta.ModelConfig.ModelInfo newModelInfos = CollUtil.getLast(ModelInfos);// 获取新的分组信息
            newModelInfos.setModels(distinctModelInfos); // 设置去重后的模型信息
            newModelGroupMap.put(entry.getKey(), newModelInfos);
        }
        // 3. 将模型信息添加到结果列表
        ArrayList<Meta.ModelConfig.ModelInfo> resultModelsInfos = new ArrayList<>(newModelGroupMap.values());
        // 4. 未分组的模型添加到结果表
        List<Meta.ModelConfig.ModelInfo> noGroupModelInfoList = new ArrayList<>(
                modelInfoList.stream()
                        .filter(ModelsInfo -> StrUtil.isBlank(ModelsInfo.getGroupKey()))
                        .collect(Collectors.toMap(Meta.ModelConfig.ModelInfo::getFieldName, o -> o, (e, r) -> r))//去重
                        .values()
        );

        resultModelsInfos.addAll(noGroupModelInfoList);
        return resultModelsInfos;
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
                    .collect(Collectors.toMap(Meta.FileConfig.FilesInfo::getOutputPath, o -> o, (e, r) -> r))//去重
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
                        .collect(Collectors.toMap(Meta.FileConfig.FilesInfo::getOutputPath, o -> o, (e, r) -> r))//去重
                        .values()
        );

        resultFilesInfos.addAll(noGroupFileInfoList);
        return resultFilesInfos;
    }

}
