package com.wang.generator.file;

import cn.hutool.extra.template.TemplateException;

import java.io.IOException;

public class MainFileGenerator {
    public static void doGenerate(Object dataModel) throws TemplateException, IOException, freemarker.template.TemplateException {
        String source;
        String desc;
        String inputRootPath = "${fileConfig.inputRootPath}";
        String outputRootPath = "${fileConfig.outputRootPath}";

        source = inputRootPath + "${fileConfig.files.inputPath}";
        desc = outputRootPath + "${fileConfig.files.outputPath}";
        DynamicFileGenerator.doGenerate(source, desc, dataModel);

    }
}
