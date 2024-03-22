package com.wang.generator;

import cn.hutool.extra.template.TemplateException;
import com.wang.model.DataModel;
import com.wang.generator.StaticFileGenerator;
import com.wang.generator.DynamicFileGenerator;
import java.io.IOException;
import java.io.File;

// 宏定义

public class FileGenerator {
    public static void doGenerate(DataModel dataModel) throws TemplateException, IOException, freemarker.template.TemplateException {
        String source;
        String desc;
        String inputRootPath = ".source/acm-template-pro";
        String outputRootPath = "E:/项目/code-generator/generated/acm-template-pro-generator/gg";

        boolean needGit = dataModel.needGit;
        boolean loop = dataModel.loop;
        String author = dataModel.mainTemplate.author;
        String outputText = dataModel.mainTemplate.outputText;

        source = new File(inputRootPath, "src/com/wang/acm/MainTemplate.java.ftl").getAbsolutePath();
        desc = new File(outputRootPath, "src/com/wang/acm/MainTemplate.java").getAbsolutePath();
        DynamicFileGenerator.doGenerate(source, desc, dataModel);
        if(needGit) {
            source = new File(inputRootPath, ".gitignore").getAbsolutePath();
            desc = new File(outputRootPath, ".gitignore").getAbsolutePath();
            StaticFileGenerator.copyFilesByHutool(source, desc);
            source = new File(inputRootPath, "README.md").getAbsolutePath();
            desc = new File(outputRootPath, "README.md").getAbsolutePath();
            StaticFileGenerator.copyFilesByHutool(source, desc);
        }
    }
}
