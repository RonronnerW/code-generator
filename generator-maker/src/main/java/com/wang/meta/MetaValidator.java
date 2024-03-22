package com.wang.meta;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.wang.meta.enums.FileGenerateTypeEnum;
import com.wang.meta.enums.FileTypeEnum;
import com.wang.meta.enums.ModelTypeEnum;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class MetaValidator {

    public static void doValidaAndFill(Meta meta) {
        validaMainInfo(meta);

        validaFileConfig(meta);

        validaModelConfig(meta);
    }

    private static void validaModelConfig(Meta meta) {
        // modelConfig 校验和默认值
        Meta.ModelConfig modelConfig = meta.getModelConfig();
        if (modelConfig == null) {
            return;
        }
        List<Meta.ModelConfig.ModelInfo> modelInfoList = modelConfig.getModels();
        if (CollectionUtil.isEmpty(modelInfoList)) {
            return;
        }
        for (Meta.ModelConfig.ModelInfo modelInfo : modelInfoList) {
            String groupKey = modelInfo.getGroupKey();
            if(StrUtil.isNotEmpty(groupKey)) {
                List<Meta.ModelConfig.ModelInfo> submodelInfo = modelInfo.getModels();
                for (Meta.ModelConfig.ModelInfo model : submodelInfo) {
                    // 输出路径默认值
                    String fieldName = model.getFieldName();
                    if (StrUtil.isBlank(fieldName)) {
                        throw new MetaException("未填写 fieldName");
                    }

                    String modelInfoType = model.getType();
                    if (StrUtil.isEmpty(modelInfoType)) {
                        model.setType(ModelTypeEnum.STRING.getValue());
                    }
                }

                // 拼接分组发命令 提供给程序调用
                List<Meta.ModelConfig.ModelInfo> models = modelInfo.getModels();
                String collect = models.stream()
                        .map(submodel -> String.format("\"--%s\"", submodel.getFieldName()))
                        .collect(Collectors.joining(","));
                modelInfo.setAllArgsStr(collect);
            } else {
                // 输出路径默认值
                String fieldName = modelInfo.getFieldName();
                if (StrUtil.isBlank(fieldName)) {
                    throw new MetaException("未填写 fieldName");
                }

                String modelInfoType = modelInfo.getType();
                if (StrUtil.isEmpty(modelInfoType)) {
                    modelInfo.setType(ModelTypeEnum.STRING.getValue());
                }
            }

        }
    }

    private static void validaFileConfig(Meta meta) {
        // fileConfig 校验和默认值
        Meta.FileConfig fileConfig = meta.getFileConfig();
        if (fileConfig == null) {
            return;
        }

        // sourceRootPath 必填
        String sourceRootPath = fileConfig.getSourceRootPath();
        if (StrUtil.isBlank(sourceRootPath)) {
            throw new MetaException("未填写 sourceRootPath");
        }
        // inputRootPath: .source + sourceRootPath 的最后一个层级路径
        String inputRootPath = fileConfig.getInputRootPath();
        String defaultInputRootPath = ".source/" + FileUtil.getLastPathEle(Paths.get(sourceRootPath)).getFileName().toString();
        if (StrUtil.isEmpty(inputRootPath)) {
            fileConfig.setInputRootPath(defaultInputRootPath);
        }
        // outputRootPath: 默认为当前路径下的 generated
        String outputRootPath = fileConfig.getOutputRootPath();
        String defaultOutputRootPath = "generated";
        if (StrUtil.isEmpty(outputRootPath)) {
            fileConfig.setOutputRootPath(defaultOutputRootPath);
        }
        String fileConfigType = fileConfig.getType();
        String defaultType = FileTypeEnum.DIR.getValue();
        if (StrUtil.isEmpty(fileConfigType)) {
            fileConfig.setType(defaultType);
        }

        // fileInfo
        List<Meta.FileConfig.FilesInfo> fileInfoList = fileConfig.getFiles();
        if (CollectionUtil.isEmpty(fileInfoList)) {
            return;
        }
        for (Meta.FileConfig.FilesInfo fileInfo : fileInfoList) {
            String type = fileInfo.getType();
            // 是组的话跳过后续校验
            if (FileTypeEnum.GROUP.getValue().equals(type)){
//                continue;
                List<Meta.FileConfig.FilesInfo> files = fileInfo.getFiles();
                if(CollectionUtil.isEmpty(files)) {
                    return;
                }
                for (Meta.FileConfig.FilesInfo file : files) {
                    validaFileInfo(file);
                }
            } else {
                validaFileInfo(fileInfo);
            }

        }
    }

    private static void validaFileInfo(Meta.FileConfig.FilesInfo fileInfo) {
        // inputPath 必填
        String inputPath = fileInfo.getInputPath();
        if (StrUtil.isBlank(inputPath)) {
            throw new MetaException("未填写 inputPath");
        }
        // outputPath 默认等于 inputPath
        String outputPath = fileInfo.getOutputPath();
        if (StrUtil.isEmpty(outputPath)) {
            fileInfo.setOutputPath(inputPath);
        }

        // type: 默认 inputPath 有文件后缀（如.java）为 file，否则为 dir
        String type = fileInfo.getType();
        if (StrUtil.isBlank(type)) {
            // 无文件后缀
            if (StrUtil.isBlank(FileUtil.getSuffix(inputPath))) {
                fileInfo.setType(FileTypeEnum.DIR.getValue());
            } else {
                fileInfo.setType(FileTypeEnum.FILE.getValue());
            }
        }

        // generateType: 如果文件结尾不为 .ftl，默认为static，否则为 dynamic
        String generateType = fileInfo.getGenerateType();
        if (StrUtil.isBlank(generateType)) {
            // 动态模板
            if (inputPath.endsWith(".ftl")) {
                fileInfo.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());
            } else {
                fileInfo.setGenerateType(FileGenerateTypeEnum.STATIC.getValue());
            }
        }
    }

    private static void validaMainInfo(Meta meta) {
        // 基础信息校验和默认值
        // 基础信息校验和默认值
        String name = StrUtil.blankToDefault(meta.getName(),"my-generator");
        String description = StrUtil.emptyToDefault(meta.getDescription(),"我的模板代码生成器");
        String basePackage = StrUtil.blankToDefault(meta.getBasePackage(),"com.wang");
        String version = StrUtil.emptyToDefault(meta.getVersion(),"1.0");
        String author = StrUtil.emptyToDefault(meta.getAuthor(),"wang");
        String createTime = StrUtil.emptyToDefault(meta.getCreateTime(),DateUtil.now());

        meta.setName(name);
        meta.setDescription(description);
        meta.setBasePackage(basePackage);
        meta.setVersion(version);
        meta.setAuthor(author);
        meta.setCreateTime(createTime);
    }
}