package com.wang.template.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 模板制作模型配置
 */
@Data
public class TemplateMakerModelConfig {

    private ModelGroupConfig modelGroupConfig;
    private List<ModelInfo> models;

    @NoArgsConstructor
    @Data
    public static class ModelInfo {
        private String fieldName;
        private String type;
        private String description;
        private Object defaultValue;
        private String abbr;
        // 替换内容
        private String replaceText;
    }

    @NoArgsConstructor
    @Data
    public static class ModelGroupConfig {
        private String condition;
        private String groupKey;
        private String groupName;

        private String type;

        private String description;

    }
}