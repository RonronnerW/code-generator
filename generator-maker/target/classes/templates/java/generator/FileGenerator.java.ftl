package ${basePackage}.generator;

import cn.hutool.extra.template.TemplateException;
import ${basePackage}.model.DataModel;
import ${basePackage}.generator.StaticFileGenerator;
import ${basePackage}.generator.DynamicFileGenerator;
import java.io.IOException;
import java.io.File;

public class FileGenerator {
    public static void doGenerate(DataModel dataModel) throws TemplateException, IOException, freemarker.template.TemplateException {
        String source;
        String desc;
        String inputRootPath = "${fileConfig.inputRootPath}";
        String outputRootPath = "${fileConfig.outputRootPath}";
    <#list fileConfig.files as fileInfo>
        source = new File(inputRootPath, "${fileInfo.inputPath}").getAbsolutePath();
        desc = new File(outputRootPath, "${fileInfo.outputPath}").getAbsolutePath();
        <#if fileInfo.generateType == "static">
        StaticFileGenerator.copyFilesByHutool(source, desc);
        <#else>
        DynamicFileGenerator.doGenerate(source, desc, dataModel);
        </#if>
    </#list>
    }
}
