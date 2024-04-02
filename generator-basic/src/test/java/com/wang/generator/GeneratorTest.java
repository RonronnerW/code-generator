package com.wang.generator;

import com.wang.model.MainTemplateConfig;
import freemarker.template.TemplateException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class GeneratorTest {
    @Test
    public void testStaticGenerator() {
        // 获取当前模块目录 dexcodegenerator-basic 路径
        String projectPath = System.getProperty("user.dir");
        // 输入路径：ACM示例代码目录
        String inputPath = new File(new File(projectPath).getParent(), "generator-demo/acm-template").getAbsolutePath();
        // 输出路径：dexcodegenerator-basic
        String outputPath = projectPath+"/generated";
        StaticGenerator.copyFilesByHutool(inputPath, outputPath);
    }

    @Test
    public void testDynamicGenerator() throws TemplateException, IOException {
        String property = System.getProperty("user.dir");
        String source = new File(property).getParent() + File.separator + "generator-basic/src/main/resources/templates/MainTemplate.java.ftl";
        String desc = new File(property).getParent() + File.separator + "generator-basic/generated/MainTemplate.java";
        MainTemplateConfig mainTemplateConfig = new MainTemplateConfig();
        mainTemplateConfig.setAuthor("wang");
        mainTemplateConfig.setLoop(true);
        mainTemplateConfig.setOutputText("test...");
        DynamicGenerator.doGenerate(source, desc, mainTemplateConfig);
    }
    @Test
    public void testGenerator() throws TemplateException, IOException {
        MainTemplateConfig mainTemplateConfig = new MainTemplateConfig();
        mainTemplateConfig.setAuthor("wang");
        mainTemplateConfig.setLoop(true);
        mainTemplateConfig.setOutputText("test...");
        MainGenerator.doGenerate(mainTemplateConfig);
    }
}