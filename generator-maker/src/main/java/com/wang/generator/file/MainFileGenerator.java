package com.wang.generator.file;

import cn.hutool.extra.template.TemplateException;
import com.wang.model.MainModel;

import java.io.File;
import java.io.IOException;
import static com.wang.generator.file.StaticFileGenerator.copyFilesByHutool;

public class MainFileGenerator {
    public static void doGenerate(MainModel mainModel) throws TemplateException, IOException, freemarker.template.TemplateException {
        String source;
        String desc;
        String inputRootPath = "${fileConfig.inputRootPath}";
        String outputRootPath = "${fileConfig.outputRootPath}";

        source = inputRootPath + "${fileConfig.files.inputPath}";
        desc = outputRootPath + "${fileConfig.files.outputPath}";
        DynamicFileGenerator.doGenerate(source, desc, mainModel);

    }
}
