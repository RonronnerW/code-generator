package ${basePackage}.generator;

import cn.hutool.extra.template.TemplateException;
import ${basePackage}.model.DataModel;
import ${basePackage}.generator.StaticFileGenerator;
import ${basePackage}.generator.DynamicFileGenerator;
import java.io.IOException;
import java.io.File;

// 宏定义
<#macro generateFile indent fileInfo>
${indent}source = new File(inputRootPath, "${fileInfo.inputPath}").getAbsolutePath();
${indent}desc = new File(outputRootPath, "${fileInfo.outputPath}").getAbsolutePath();
<#if fileInfo.generateType == "static">
${indent}StaticFileGenerator.copyFilesByHutool(source, desc);
<#else>
${indent}DynamicFileGenerator.doGenerate(source, desc, dataModel);
</#if>
</#macro>

public class FileGenerator {
    public static void doGenerate(DataModel dataModel) throws TemplateException, IOException, freemarker.template.TemplateException {
        String source;
        String desc;
        String inputRootPath = "${fileConfig.inputRootPath}";
        String outputRootPath = "${fileConfig.outputRootPath}";

    <#list modelConfig.models as modelInfo>
    <#-- 有分组 -->
    <#if modelInfo.groupKey??>
    <#list modelInfo.models as subModelInfo>
        ${subModelInfo.type} ${subModelInfo.fieldName} = dataModel.${modelInfo.groupKey}.${subModelInfo.fieldName};
    </#list>
    <#else>
        ${modelInfo.type} ${modelInfo.fieldName} = dataModel.${modelInfo.fieldName};
    </#if>
    </#list>

    <#list fileConfig.files as fileInfo>
        <#if fileInfo.groupKey??>
        <#if fileInfo.condition??>
        if(${fileInfo.condition}) {
            <#list fileInfo.files as fileInfo>
            <@generateFile indent="            " fileInfo=fileInfo />
            </#list>
        }
        <#else>
        <@generateFile indent="        " fileInfo=fileInfo />
        </#if>
        <#else>
        <#if fileInfo.condition??>
        if (${fileInfo.condition}) {
            <@generateFile indent="            " fileInfo=fileInfo />
        }
        <#else>
        <@generateFile indent="        " fileInfo=fileInfo />
        </#if>
        </#if>
    </#list>
    }
}
