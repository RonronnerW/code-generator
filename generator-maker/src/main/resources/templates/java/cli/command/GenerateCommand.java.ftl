package ${basePackage}.cli.command;

import cn.hutool.core.bean.BeanUtil;
import ${basePackage}.generator.FileGenerator;
import ${basePackage}.model.DataModel;
import lombok.Data;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

<#macro generateComand indent modelInfo>
${indent}@Option(names = {<#if modelInfo.abbr??>"-${modelInfo.abbr}", </#if>"--${modelInfo.fieldName}"}, arity = "0..1", <#if modelInfo.description??>description = "${modelInfo.description}", </#if>interactive = true, echo = true)
${indent}private ${modelInfo.type} ${modelInfo.fieldName}<#if modelInfo.defaultValue??> = ${modelInfo.defaultValue?c}</#if>;
<#--<#if modelInfo.defaultValue??>-->
<#--<#if modelInfo.defaultValue=="true" || modelInfo.defaultValue=="false">-->
<#--${indent}private ${modelInfo.type} ${modelInfo.fieldName} = ${modelInfo.defaultValue};-->
<#--<#else>-->
<#--${indent}private ${modelInfo.type} ${modelInfo.fieldName} = "${modelInfo.defaultValue}";-->
<#--</#if>-->
<#--<#else>-->
<#--${indent}private ${modelInfo.type} ${modelInfo.fieldName};-->
<#--</#if>-->
</#macro>
<#-- 生成命令调用 -->
<#macro generateCommand indent modelInfo>
${indent}System.out.println("输入${modelInfo.groupName}配置：");
${indent}CommandLine ${modelInfo.groupKey}CommandLine = new CommandLine(${modelInfo.type}Command.class);
${indent}${modelInfo.groupKey}CommandLine.execute(${modelInfo.allArgsStr});
</#macro>
/**
 * 子命令类
 */
@Command(name = "generate", description = "生成代码", mixinStandardHelpOptions = true)
@Data
public class GenerateCommand implements Callable {

<#list modelConfig.models as modelInfo>
    <#if modelInfo.groupKey??>
    static private DataModel.${modelInfo.type} ${modelInfo.groupKey} = new DataModel.${modelInfo.type}();

    @Command(name = "${modelInfo.groupKey}", description = "${modelInfo.groupName}")
    @Data
    public static class ${modelInfo.type}Command implements Callable{
        <#list modelInfo.models as subModelInfo>
        <@generateComand indent="        " modelInfo=subModelInfo></@generateComand>
        </#list>

        @Override
        public Object call() throws Exception {
            <#list modelInfo.models as subModelInfo>
            ${modelInfo.groupKey}.${subModelInfo.fieldName} = ${subModelInfo.fieldName};
            </#list>

            return 0;
        }
    }
    <#else>
    <@generateComand indent="    " modelInfo=modelInfo></@generateComand>
    </#if>
    </#list>

    <#-- 生成调用方法 -->
    public Integer call() throws Exception {
        <#list modelConfig.models as modelInfo>
        <#if modelInfo.groupKey??>
        <#if modelInfo.condition??>
        if (${modelInfo.condition}) {
        <@generateCommand indent="            " modelInfo = modelInfo />
        }
        <#else>
        <@generateCommand indent="        " modelInfo = modelInfo />
        </#if>
        </#if>
        </#list>
        <#-- 填充数据模型对象 -->
        DataModel dataModel = new DataModel();
        BeanUtil.copyProperties(this, dataModel);
        <#list modelConfig.models as modelInfo>
        <#if modelInfo.groupKey??>
        dataModel.${modelInfo.groupKey} = ${modelInfo.groupKey};
        </#if>
        </#list>
        FileGenerator.doGenerate(dataModel);
        return 0;
    }
}
