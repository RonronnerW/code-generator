package com.wang.template.model;

import com.wang.meta.Meta;
import lombok.Data;

@Data
public class TemplateMakerConfig {
    // Meta meta, Long id, String originProjectPath, TemplateMakerFileConfig templateMakerFileConfig, TemplateMakerModelConfig templateMakerModelConfig
    private Long id;
    private Meta meta = new Meta();
    private String originProjectPath;
    private TemplateMakerFileConfig fileConfig = new TemplateMakerFileConfig();
    private TemplateMakerModelConfig modelConfig = new TemplateMakerModelConfig();
}
