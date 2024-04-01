package com.wang.template.model;

import lombok.Data;

/**
 * 模版制作输出配置
 *
 */
@Data
public class TemplateMakerOutputConfig {
    /**
     * 同一个文件分组内和分组外都出现
     * 决定是否保留分组内分组外的配置
     */
    // 从未分组文件中移除组内的同名文件
    private boolean removeGroupFilesFromRoot = true;
}