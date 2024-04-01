package com.wang.template;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import com.wang.template.enums.FileFilterRangeEnum;
import com.wang.template.enums.FileFilterRuleEnum;
import com.wang.template.model.FileFilterConfig;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件过滤
 */
public class FileFilter {

    /**
     * 支持文件或目录过滤
     * @param filePath 路径 支持传入文件或目录， 能够对多个文件进行过滤
     * @param fileFilterConfigList 规则
     * @return 返回满足规则的文件列表
     */
    public static List<File> doFilter(String filePath, List<FileFilterConfig> fileFilterConfigList) {
        List<File> files = FileUtil.loopFiles(filePath);
        return files.stream()
                .filter(file -> doSingleFileFilter(file, fileFilterConfigList))
                .collect(Collectors.toList());
    }
    /**
     * 单个文件过滤
     * @param file 单个文件
     * @param fileFilterConfigList 过滤规则
     * @return 是否保留
     */
    public static boolean doSingleFileFilter(File file, List<FileFilterConfig> fileFilterConfigList) {
        String fileName = file.getName();
        String fileContent = FileUtil.readUtf8String(file);
        // 所有校验器校验后的结果
        boolean result = true;

        if(CollectionUtil.isEmpty(fileFilterConfigList)) {
            return true;
        }
        // 遍历校验规则
        for (FileFilterConfig fileFilterConfig : fileFilterConfigList) {
            String range = fileFilterConfig.getRange();
            String rule = fileFilterConfig.getRule();
            String value = fileFilterConfig.getValue();

            // 范围枚举
            FileFilterRangeEnum fileFilterRangeEnum = FileFilterRangeEnum.getEnumByValue(range);
            if (fileFilterRangeEnum == null) {
                continue;
            }

            // 要校验啥
            String content = null;
            switch (fileFilterRangeEnum) {
                case FILE_NAME:
                    content = fileName;
                    break;
                case FILE_CONTENT:
                    content = fileContent;
                    break;
                default:
            }

            // 规则枚举
            FileFilterRuleEnum fileFilterRuleEnum = FileFilterRuleEnum.getEnumByValue(rule);
            if (fileFilterRuleEnum == null) {
                continue;
            }
            switch (fileFilterRuleEnum) {
                case CONTAINS:
                    result = content.contains(value);
                    break;
                case STARTS_WITH:
                    result = content.startsWith(value);
                    break;
                case ENDS_WITH:
                    result = content.endsWith(value);
                    break;
                case REGEX:
                    result = content.matches(value);
                    break;
                case EQUALS:
                    result = content.equals(value);
                    break;
                default:
            }

            if(!result) {
                return false;
            }
        }
        return true;
    }
}
