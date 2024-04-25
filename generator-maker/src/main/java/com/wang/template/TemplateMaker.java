package com.wang.template;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.wang.meta.Meta;
import com.wang.meta.enums.FileGenerateTypeEnum;
import com.wang.meta.enums.FileTypeEnum;
import com.wang.template.model.TemplateMakerConfig;
import com.wang.template.model.TemplateMakerFileConfig;
import com.wang.template.model.TemplateMakerModelConfig;
import com.wang.template.model.TemplateMakerOutputConfig;
import com.wang.template.utils.TemplateMakerUtils;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class TemplateMaker {
    /**
     * 制作模板文件和配置文件--封装参数
     * @param templateMakerConfig 制作模板参数
     * @return id
     */
    public static long makeTemplate(TemplateMakerConfig templateMakerConfig) {
        Long id = templateMakerConfig.getId();
        Meta meta = templateMakerConfig.getMeta();
        String originProjectPath = templateMakerConfig.getOriginProjectPath();
        TemplateMakerFileConfig fileConfig = templateMakerConfig.getFileConfig();
        TemplateMakerModelConfig modelConfig = templateMakerConfig.getModelConfig();
        TemplateMakerOutputConfig templateMakerOutputConfig = templateMakerConfig.getOutputConfig();
        return makeTemplate(meta, id, originProjectPath, fileConfig, modelConfig, templateMakerOutputConfig);
    }
    /**
     * 制作模板文件和配置文件
     * @param meta                     元信息
     * @param id                       id
     * @param originProjectPath        原始项目路径
     * @param templateMakerFileConfig  输入文件信息
     * @param templateMakerModelConfig 数据模型信息和替换内容
     * @return id
     */
    public static long makeTemplate(Meta meta, Long id, String originProjectPath,
                                    TemplateMakerFileConfig templateMakerFileConfig,
                                    TemplateMakerModelConfig templateMakerModelConfig,
                                    TemplateMakerOutputConfig templateMakerOutputConfig) {
        // 没有 id 则生成
        if (id == null) {
            id = IdUtil.getSnowflakeNextId();
        }
        // 项目目录 codegenerator/generator-maker
        String projectPath = System.getProperty("user.dir");
        String parentPath = new File(projectPath).getParent(); // codegenerator
        String tempDirPath = parentPath + File.separator + ".temp"; // 将制作文件拷贝到临时文件目录
        // 生成文件所在目录
//        String templatePath = tempDirPath + File.separator + FileUtil.getLastPathEle(Paths.get(originProjectPath)).toString() + "-" + id;
        String templatePath = tempDirPath + File.separator + id;
        // 目录不存在，则是首次制作，复制目录
        if (!FileUtil.exist(templatePath)) {
            FileUtil.mkdir(templatePath);
            FileUtil.copy(originProjectPath, templatePath, true);
        }

        // 一、输入信息
        // 添加 originProjectPath 最后一层目录
//        String sourceRootPath = templatePath + File.separator + FileUtil.getLastPathEle(Paths.get(originProjectPath)).toString();
        // 输入文件信息，获取项目根目录
        // 非首次制作配置文件已经存在sourceRootPath ，后续制作时无需在指定。自动获取空间下第一个目录
        String sourceRootPath = FileUtil.loopFiles(new File(templatePath), 1, null)
                .stream()
                .filter(File::isDirectory)
                .findFirst()
                .orElseThrow(RuntimeException::new)
                .getAbsolutePath();

        // 二 使用字符替换 生成模板文件
        List<Meta.FileConfig.FileInfo> newFileInfoList = getFilesInfos(templateMakerFileConfig, templateMakerModelConfig, sourceRootPath);

        // 处理模型信息
        List<Meta.ModelConfig.ModelInfo> newModelInfoLists = getModelInfos(templateMakerModelConfig);

        // 三 生成 meta.json 配置文件
        String metaPath = templatePath + File.separator + "meta.json";
        if (FileUtil.exist(metaPath)) {
            // 存在meta文件 追加
            Meta oldMeta = JSONUtil.toBean(FileUtil.readUtf8String(metaPath), Meta.class);
            BeanUtil.copyProperties(meta, oldMeta, CopyOptions.create().ignoreNullValue());
            meta = oldMeta;

            List<Meta.FileConfig.FileInfo> files = oldMeta.getFileConfig().getFiles();
            files.addAll(newFileInfoList);

            List<Meta.ModelConfig.ModelInfo> models = oldMeta.getModelConfig().getModels();
            models.addAll(newModelInfoLists);
            //去重
            oldMeta.getFileConfig().setFiles(distinctFiles(files));
            oldMeta.getModelConfig().setModels(distinctModels(models));

        } else {
            // 不存在
            Meta.FileConfig fileConfig = new Meta.FileConfig();
            fileConfig.setSourceRootPath(originProjectPath.replaceAll("\\\\", "/"));
            fileConfig.setInputRootPath(templatePath.replaceAll("\\\\", "/")+"/"+FileUtil.getLastPathEle(Paths.get(originProjectPath)).toString());
            ArrayList<Meta.FileConfig.FileInfo> fileInfos = new ArrayList<>(newFileInfoList);
            fileConfig.setFiles(fileInfos);
            meta.setFileConfig(fileConfig);

            Meta.ModelConfig modelConfig = new Meta.ModelConfig();
            ArrayList<Meta.ModelConfig.ModelInfo> modelInfos = new ArrayList<>(newModelInfoLists);
            modelConfig.setModels(modelInfos);
            meta.setModelConfig(modelConfig);
        }

        // （新增）对应有分组的情况，文件外层和分组去重
        if (templateMakerOutputConfig != null) {
            if (templateMakerOutputConfig.isRemoveGroupFilesFromRoot()){
                List<Meta.FileConfig.FileInfo> fileInfoList = meta.getFileConfig().getFiles();
                meta.getFileConfig().setFiles(TemplateMakerUtils.removeGroupFilesFromRoot(fileInfoList));
            }
        }

        // 输出元信息 meta.json
        FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(meta), metaPath);

        return id;
    }

    /**
     * 获取模型信息列表
     * @param templateMakerModelConfig 模型信息
     * @return 模型信息列表
     */
    private static List<Meta.ModelConfig.ModelInfo> getModelInfos(TemplateMakerModelConfig templateMakerModelConfig) {
        // 本次新增的模型配置列表
        List<Meta.ModelConfig.ModelInfo> newModelInfoLists = new ArrayList<>();
        if(templateMakerModelConfig==null) {
            return newModelInfoLists;
        }
        List<TemplateMakerModelConfig.ModelInfo> modelConfigLists = templateMakerModelConfig.getModels();
        if(CollUtil.isEmpty(modelConfigLists)) {
            return newModelInfoLists;
        }
        // 转换为Meta.ModelConfig.ModelInfo
        List<Meta.ModelConfig.ModelInfo> inputModelInfoLists = modelConfigLists.stream().map(models -> {
            Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
            BeanUtil.copyProperties(models, modelInfo);
            return modelInfo;
        }).toList();

        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();
        // 分组
        if(modelGroupConfig != null) {
            String condition = modelGroupConfig.getCondition();
            String groupKey = modelGroupConfig.getGroupKey();
            String groupName = modelGroupConfig.getGroupName();
            Meta.ModelConfig.ModelInfo groupModelInfo = new Meta.ModelConfig.ModelInfo();
            BeanUtil.copyProperties(modelGroupConfig, groupModelInfo);
            groupModelInfo.setModels(inputModelInfoLists);
            groupModelInfo.setGroupKey(groupKey);
            groupModelInfo.setCondition(condition);
            groupModelInfo.setGroupName(groupName);
            // 模型全放到一个分组内
            newModelInfoLists = new ArrayList<>();
            newModelInfoLists.add(groupModelInfo);

        } else {
            newModelInfoLists.addAll(inputModelInfoLists);
        }
        return newModelInfoLists;
    }

    /**
     * 获取文件信息
     * @param templateMakerFileConfig 文件信息
     * @param templateMakerModelConfig 模型信息
     * @param sourceRootPath 原始路径
     * @return 文件信息列表
     */
    private static List<Meta.FileConfig.FileInfo> getFilesInfos(TemplateMakerFileConfig templateMakerFileConfig, TemplateMakerModelConfig templateMakerModelConfig, String sourceRootPath) {
        List<Meta.FileConfig.FileInfo> newFileInfoList = new ArrayList<>();
        if(templateMakerFileConfig==null) {
            return newFileInfoList;
        }
        // 遍历模板制作文件配置  文件目录和过滤配置
        List<TemplateMakerFileConfig.FileInfoConfig> fileInfoConfigList = templateMakerFileConfig.getFiles();
        if(CollUtil.isEmpty(fileInfoConfigList)) {
            return newFileInfoList;
        }
        for (TemplateMakerFileConfig.FileInfoConfig fileInfoConfig : fileInfoConfigList) {
            String inputPath = fileInfoConfig.getPath();
            // 如果是相对路径，要改为绝对路径
            if (!inputPath.startsWith(sourceRootPath)) {
                inputPath = sourceRootPath + File.separator + inputPath;
            }
            // 应用过滤器，获取过滤后的文件列表
            List<File> fileList = FileFilter.doFilter(inputPath, fileInfoConfig.getFileFilterConfigList());
            for (File file : fileList) {
                Meta.FileConfig.FileInfo fileInfo = makeFileTemplate(templateMakerModelConfig,fileInfoConfig, sourceRootPath, file);
                newFileInfoList.add(fileInfo);
            }
        }
        // ftl 文件不作为处理文件
        newFileInfoList = newFileInfoList.stream().filter(fileInfo1 -> !"ftl".equals(FileUtil.getSuffix(fileInfo1.getOutputPath()))).collect(Collectors.toList());

        // 如果是文件分组
        TemplateMakerFileConfig.FileGroupConfig fileGroupConfig = templateMakerFileConfig.getFileGroupConfig();
        if (fileGroupConfig != null) {
            String condition = fileGroupConfig.getCondition();
            String groupKey = fileGroupConfig.getGroupKey();
            String groupName = fileGroupConfig.getGroupName();
            // 分组配置
            Meta.FileConfig.FileInfo fileInfo = new Meta.FileConfig.FileInfo();
            fileInfo.setType(FileTypeEnum.GROUP.getValue());
            fileInfo.setCondition(condition);
            fileInfo.setGroupKey(groupKey);
            fileInfo.setGroupName(groupName);
            fileInfo.setFiles(newFileInfoList);
            newFileInfoList = new ArrayList<>();
            newFileInfoList.add(fileInfo);
        }
        return newFileInfoList;
    }

    /**
     * 单个文件制作模板
     * @param templateMakerModelConfig 模型配置信息
     * @param sourceRootPath 项目根路径
     * @param filePath 文件路径
     * @return 文件配置
     */
    private static Meta.FileConfig.FileInfo makeFileTemplate(TemplateMakerModelConfig templateMakerModelConfig, TemplateMakerFileConfig.FileInfoConfig fileInfoConfig, String sourceRootPath, File filePath) {
        // 一 获取绝对文件绝对路径
        String fileInputPathAbsolute = filePath.getAbsolutePath().replaceAll("\\\\", "/");
        String fileOutputPathAbsolute = fileInputPathAbsolute + ".ftl";
        sourceRootPath = sourceRootPath.replaceAll("\\\\", "/");
        // 二 替换原来的内容, 生成模板文件
        // 1. 读取文件 获取相对路径
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
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();
        for (TemplateMakerModelConfig.ModelInfo model : templateMakerModelConfig.getModels()) {
            String fieldName = model.getFieldName();
            if(modelGroupConfig ==null) {
                replaceContent = String.format("${%s}", fieldName);
            } else {
                // 是分组，“挖坑”多一个层级
                String groupKey = modelGroupConfig.getGroupKey();
                replaceContent = String.format("${%s.%s}", groupKey, fieldName);
            }
            // 多次替换
            newFileContent = StrUtil.replace(newFileContent, model.getReplaceText(), replaceContent);
        }

        // 文件配置信息
        Meta.FileConfig.FileInfo fileInfo = new Meta.FileConfig.FileInfo();
        fileInfo.setInputPath(outputFilePath);
        fileInfo.setOutputPath(inputFilePath);
        fileInfo.setCondition(fileInfoConfig.getCondition());
        fileInfo.setType(FileTypeEnum.FILE.getValue());
        fileInfo.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());
        boolean equals = newFileContent.equals(originContent);
        // 输出模板文件 有模板文件 判断有没有修改 有修改生成模板
        if(!hasTemplateFile) {
            if(equals) {
                fileInfo.setInputPath(inputFilePath);
                fileInfo.setGenerateType(FileGenerateTypeEnum.STATIC.getValue());
            } else {
                FileUtil.writeUtf8String(newFileContent, fileOutputPathAbsolute);
            }
        } else if(!equals) {
            // 没有模板文件 直接生成
            FileUtil.writeUtf8String(newFileContent, fileOutputPathAbsolute);
        }
        return fileInfo;
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
    private static List<Meta.FileConfig.FileInfo> distinctFiles(List<Meta.FileConfig.FileInfo> fileInfoList) {
        // 分组去重
        // 1. 判断是否有分组信息然后根据groupKey进行分组
        Map<String, List<Meta.FileConfig.FileInfo>> fileGroupMap = fileInfoList.stream()
                .filter(fileInfo -> StrUtil.isNotBlank(fileInfo.getGroupKey()))
                .collect(Collectors.groupingBy(Meta.FileConfig.FileInfo::getGroupKey)); // groupingBy 是根据参数进行分组

        // 2. 同组内进行去重
        Map<String, Meta.FileConfig.FileInfo> newFileGroupMap = new HashMap<>();
        for(Map.Entry<String, List<Meta.FileConfig.FileInfo>> entry: fileGroupMap.entrySet()) {
            List<Meta.FileConfig.FileInfo> fileInfos = entry.getValue();
            List<Meta.FileConfig.FileInfo> distinctFileInfos = new ArrayList<>(fileInfos.stream()
                    .flatMap(fileInfo -> fileInfo.getFiles().stream()) // 将内容展平
                    .collect(Collectors.toMap(Meta.FileConfig.FileInfo::getOutputPath, o -> o, (e, r) -> r))//去重
//                    .distinct() // distinct 方法是基于equals的
                    .values());
            // groupKey 相同 更新其他内容
            Meta.FileConfig.FileInfo newFileInfos = CollUtil.getLast(fileInfos);// 获取新的分组信息
            newFileInfos.setFiles(distinctFileInfos); // 设置去重后的文件信息
            newFileGroupMap.put(entry.getKey(), newFileInfos);
        }

        // 3. 将文件信息添加到结果列表
        ArrayList<Meta.FileConfig.FileInfo> resultFileInfos = new ArrayList<>(newFileGroupMap.values());

        // 4. 未分组的文件添加到结果表
        List<Meta.FileConfig.FileInfo> noGroupFileInfoList = new ArrayList<>(
                fileInfoList.stream()
                        .filter(fileInfo -> StrUtil.isBlank(fileInfo.getGroupKey()))
                        .collect(Collectors.toMap(Meta.FileConfig.FileInfo::getOutputPath, o -> o, (e, r) -> r))//去重
                        .values()
        );

        resultFileInfos.addAll(noGroupFileInfoList);
        return resultFileInfos;
    }

}
