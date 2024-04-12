package ${basePackage}.model;

import lombok.Data;

<#macro generateModel indent modelInfo>
<#if modelInfo.description??>
${indent}/**
${indent} * ${modelInfo.description}
${indent} */
</#if>
<#--${indent}public ${modelInfo.type} ${modelInfo.fieldName}<#if modelInfo.defaultValue??> = ${modelInfo.defaultValue?c}</#if>;-->
<#if modelInfo.defaultValue??>
<#if modelInfo.defaultValue=="true" || modelInfo.defaultValue=="false">
${indent}public ${modelInfo.type} ${modelInfo.fieldName} = ${modelInfo.defaultValue};
<#else>
${indent}public ${modelInfo.type} ${modelInfo.fieldName} = "${modelInfo.defaultValue}";
</#if>
<#else>
${indent}public ${modelInfo.type} ${modelInfo.fieldName};
</#if>
</#macro>
/**
* 数据模型
*/
@Data
public class DataModel {
<#list modelConfig.models as modelInfo>
    <#if modelInfo.groupKey??>
    /**
     * ${modelInfo.groupName}
     */
    public ${modelInfo.type} ${modelInfo.groupKey} = new ${modelInfo.type}();

    /**
     * ${modelInfo.description}
     */
    @Data
    public static class ${modelInfo.type} {
    <#list modelInfo.models as modelInfo>
        <@generateModel indent="        " modelInfo=modelInfo />
    </#list>
    }
    <#else>
    <@generateModel indent="    " modelInfo=modelInfo></@generateModel>
    </#if>


</#list>
}