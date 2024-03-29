package com.wang.generator;

import com.wang.model.MainTemplateConfig;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;
import static com.wang.generator.StaticGenerator.copyFilesByHutool;

/**
 * 文件生成
 * 包括静态和动态
 */
public class MainGenerator {

    public static void doGenerate(MainTemplateConfig mainTemplateConfig) throws TemplateException, IOException {
        // 获取当前模块目录 dexcode-generator-basic 路径
        String projectPath = System.getProperty("user.dir");
//        File parentFile = new File(projectPath).getParentFile();
        // 输入路径：ACM示例代码目录
        String inputPath = new File(projectPath, "generator-demo/acm-template").getAbsolutePath();
        String outputPath = projectPath+File.separator+"generated";
        // 输出路径：projectPath
        copyFilesByHutool(inputPath, outputPath);

        String source = projectPath + File.separator + "generator-basic/src/main/resources/templates/MainTemplate.java.ftl";
        String desc = outputPath +File.separator+  "acm-template/src/com/wang/acm/MainTemplate.java";

        DynamicGenerator.doGenerate(source, desc, mainTemplateConfig);
    }
}
