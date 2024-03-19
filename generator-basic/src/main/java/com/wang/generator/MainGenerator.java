package com.wang.generator;

import com.wang.model.MainTemplateConfig;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;
import static com.wang.generator.StaticGenerator.copyFilesByHutool;

public class MainGenerator {
    public static void main(String[] args) throws TemplateException, IOException {
        MainTemplateConfig mainTemplateConfig = new MainTemplateConfig();
        mainTemplateConfig.setAuthor("wang");
        mainTemplateConfig.setLoop(true);
        mainTemplateConfig.setOutputText("test...");
        doGenerator(mainTemplateConfig);

    }
    public static void doGenerator(MainTemplateConfig mainTemplateConfig) throws TemplateException, IOException {
        // 获取当前模块目录 dexcode-generator-basic 路径
        String projectPath = System.getProperty("user.dir");
        File parentFile = new File(projectPath).getParentFile();
        // 输入路径：ACM示例代码目录
        String inputPath = new File(parentFile, "generator-demo/acm-template").getAbsolutePath();
        // 输出路径：projectPath
        copyFilesByHutool(inputPath, projectPath);

        String source = projectPath + File.separator + "/src/main/resources/templates/MainTemplate.java.ftl";
        String desc = "acm-template/src/com/wang/acm/MainTemplate.java";

        DynamicGenerator.doGenerator(source, desc, mainTemplateConfig);
    }
}
