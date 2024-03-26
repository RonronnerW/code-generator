package com.wang.template.model;

import com.wang.meta.Meta;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 模板制作文件配置
 */
@Data
public class TemplateMakerFileConfig {

    private List<FileInfoConfig> files;
    private FilesGroupInfo filesGroupInfo;
    @NoArgsConstructor
    @Data
    public static class FileInfoConfig {

        /**
         * 文件（目录）路径
         */
        private String path;

        /**
         * 文件过滤配置
         */
        private List<FileFilterConfig> fileFilterConfigList;
    }

    @NoArgsConstructor
    @Data
    public static class FilesGroupInfo {
        private String condition;
        private String groupKey;
        private String groupName;

    }
}