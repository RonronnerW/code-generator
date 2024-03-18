package com.wang.generator;

import com.wang.model.MainTemplateConfig;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

import static com.wang.generator.StaticGenerator.copyFilesByHutool;

public class MainGenerator {
    public static void main(String[] args) throws TemplateException, IOException {
        // 获取当前模块目录 dexcode-generator-basic 路径
        String projectPath = System.getProperty("user.dir");
        // 输入路径：ACM示例代码目录
        String inputPath = new File(projectPath, "generator-demo/acm-template").getAbsolutePath();
        // 输出路径：projectPath
        copyFilesByHutool(inputPath, projectPath);

        String source = projectPath + File.separator + "generator-basic/src/main/resources/templates/MainTemplate.java.ftl";
        String desc = "acm-template/src/com/wang/acm/MainTemplate.java";
        MainTemplateConfig mainTemplateConfig = new MainTemplateConfig();
        mainTemplateConfig.setAuthor("wang");
        mainTemplateConfig.setLoop(true);
        mainTemplateConfig.setOutputText("test...");
        DynamicGenerator.doGenerator(source, desc, mainTemplateConfig);

    }
}
