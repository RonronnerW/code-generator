package com.wang.generator;

import cn.hutool.extra.template.TemplateException;
import com.wang.model.DataModel;
import com.wang.generator.StaticFileGenerator;
import com.wang.generator.DynamicFileGenerator;
import java.io.IOException;
import java.io.File;

public class FileGenerator {
    public static void doGenerate(DataModel dataModel) throws TemplateException, IOException, freemarker.template.TemplateException {
        String source;
        String desc;
        String inputRootPath = "E:/项目/code-generator/generator-demo/acm-template-pro";
        String outputRootPath = "E:/项目/code-generator/generated";
        source = new File(inputRootPath, "src/com/wang/acm/MainTemplate.java.ftl").getAbsolutePath();
        desc = new File(outputRootPath, "src/com/wang/acm/MainTemplate.java").getAbsolutePath();
        DynamicFileGenerator.doGenerate(source, desc, dataModel);
        source = new File(inputRootPath, ".gitignore").getAbsolutePath();
        desc = new File(outputRootPath, ".gitignore").getAbsolutePath();
        StaticFileGenerator.copyFilesByHutool(source, desc);
        source = new File(inputRootPath, "README.md").getAbsolutePath();
        desc = new File(outputRootPath, "README.md").getAbsolutePath();
        StaticFileGenerator.copyFilesByHutool(source, desc);
    }
}
