package com.wang.model;

import lombok.Data;

/**
 * 动态模板配置
 */
@Data
public class DataModel {
    /**
     * 是否生成 .gitignore文件
     */
    public boolean needGit = true;
    /**
     * 是否生成循环
     */
    public boolean loop;

    /**
     * 核心模板
     */
    public MainModel mainModel = new MainModel();

    public static class MainModel {
        /**
         * 作者注释
         */
        public String author;
        /**
         * 输出信息
         */
        public String outputText;
    }
}