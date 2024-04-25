package com.wang.generator.main;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.wang.generator.file.DynamicFileGenerator;
import com.wang.generator.other.JarGenerator;
import com.wang.generator.other.ScriptGenerator;
import com.wang.meta.Meta;
import com.wang.meta.MetaManager;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

/**
 * 使用模板方法模式定义制作工具的流程
 * 子类可以重写具体步骤
 */
public abstract class GenerateTemplate {
    public void doGenerate(Meta meta, String outputPath) throws IOException, InterruptedException, TemplateException {
        if (!FileUtil.exist(outputPath)) {
            FileUtil.mkdir(outputPath);
        }
        // 2. 原始代码复制到 .source 目录下
        sourceCodeCopy(meta, outputPath);

        // 3. 根据 .ftl 文件生成代码
        generateCode(meta, outputPath);

        // 4. 构建 jar 包
        generateJar(outputPath);

        // 5. 封装脚本
        generateScript(outputPath, meta);

        // 6. 生成精简版的程序（产物包）
        String path = generateDist(outputPath, meta);

        // 7. 压缩
        buildZip(path);
    }

    public void doGenerate() throws IOException, InterruptedException, TemplateException {
        // 1. 获取元信息
        Meta meta = MetaManager.getMeta();

        // 获取当前项目的路径 code-generator
        String projectPath = System.getProperty("user.dir");
        // 获取输出路径 code-generator/generated/acm-template-pro-generator
        String outputPath = projectPath + File.separator + "generated" + File.separator + meta.getName();
        if (!FileUtil.exist(outputPath)) {
            FileUtil.mkdir(outputPath);
        }
        doGenerate(meta, outputPath);
    }

    /**
     * 制作压缩包
     *
     * @param outputPath
     * @return 压缩包路径
     */
    protected String buildZip(String outputPath) {
        String zipPath = outputPath + ".zip";
        ZipUtil.zip(outputPath, zipPath);
        return zipPath;
    }

    /**
     * 生成精简版
     * @param outputPath 输出目录
     * @param meta 元信息
     */
    protected String generateDist(String outputPath, Meta meta) {
        String distOutputPath = outputPath + "-dist";
        String sourceCopyPath = outputPath+File.separator+".source";
        String jarName = String.format("%s-%s-jar-with-dependencies.jar", meta.getName(), meta.getVersion());
        String shellOutputFilePath = outputPath + File.separator + "generator";
        String jarPath = "target/" + jarName;

        //  - 拷贝jar包
        String targetAbsolutePath = distOutputPath + File.separator + "target";
        FileUtil.mkdir(targetAbsolutePath);
        String jarAbsolutePath = outputPath + File.separator + jarPath;
        FileUtil.copy(jarAbsolutePath, targetAbsolutePath, true);

        //  - 拷贝脚本文件
        FileUtil.copy(shellOutputFilePath, distOutputPath, true);
        FileUtil.copy(shellOutputFilePath + ".bat", distOutputPath, true);

        //  - 拷贝原始文件
        FileUtil.copy(sourceCopyPath, distOutputPath, true);

        return distOutputPath;
    }

    /**
     * 生成脚本
     * @param outputPath 输出路径
     * @param meta 元信息
     * @throws IOException 异常
     */

    protected void generateScript(String outputPath, Meta meta) throws IOException {
        String shellOutputFilePath = outputPath + File.separator + "generator";
        String jarName = String.format("%s-%s-jar-with-dependencies.jar", meta.getName(), meta.getVersion());
        String jarPath = "target/" + jarName;
        ScriptGenerator.doGenerate(shellOutputFilePath, jarPath);
    }

    /**
     * 生成jar包
     * @param outputPath 输出路径
     * @throws IOException 异常
     * @throws InterruptedException 异常
     */
    protected void generateJar(String outputPath) throws IOException, InterruptedException {
        JarGenerator.doGenerate(outputPath);
    }

    /**
     * 拷贝源文件到.source目录下
     * @param meta 元信息
     * @param outputPath 输出路径
     */
    protected void sourceCodeCopy(Meta meta, String outputPath) {

        // 将模板复制到输出路径的source下
        String sourceRootPath = meta.getFileConfig().getSourceRootPath();
        String sourceCopyPath = outputPath+File.separator+".source";
        FileUtil.copy(sourceRootPath, sourceCopyPath, true);
    }

    /**
     * 根据ftl生成代码
     * @param meta 元信息
     * @param outputPath 输出路径
     * @throws IOException 异常
     * @throws TemplateException 异常
     */
    protected void generateCode(Meta meta, String outputPath) throws IOException, TemplateException {
        String inputFilePath;
        String outputFilePath;

        ClassPathResource classPathResource = new ClassPathResource("");
//        String inputResourcePath = classPathResource.getAbsolutePath();
        String inputResourcePath = "";

        // Java包的基础路径
        // com.wang
        String outputBasePackage = meta.getBasePackage();
        // com/wang
        String outputBasePackagePath = StrUtil.join("/", StrUtil.split(outputBasePackage, "."));
        String outputBaseJavaPackagePath = outputPath + File.separator + "src/main/java" + File.separator + outputBasePackagePath;

        // model.DataModel
        inputFilePath = inputResourcePath + File.separator + "templates/java/model/DataModel.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + File.separator + "/model/DataModel.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // cli.command.ConfigCommand
        inputFilePath = inputResourcePath + File.separator + "templates/java/cli/command/ConfigCommand.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/cli/command/ConfigCommand.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // cli.command.GenerateCommand
        inputFilePath = inputResourcePath + File.separator + "templates/java/cli/command/GenerateCommand.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/cli/command/GenerateCommand.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // cli.command.JsonGenerateCommand
        inputFilePath = inputResourcePath + File.separator + "templates/java/cli/command/JsonGenerateCommand.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/cli/command/JsonGenerateCommand.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // cli.command.ListCommand
        inputFilePath = inputResourcePath + File.separator + "templates/java/cli/command/ListCommand.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/cli/command/ListCommand.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // cli.CommandExecutor
        inputFilePath = inputResourcePath + File.separator + "templates/java/cli/CommandExecutor.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/cli/CommandExecutor.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // Main
        inputFilePath = inputResourcePath + File.separator + "templates/java/Main.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/Main.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // generator.StaticFileGenerator
        inputFilePath = inputResourcePath + File.separator + "templates/java/generator/StaticFileGenerator.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/generator/StaticFileGenerator.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // generator.DynamicFileGenerator
        inputFilePath = inputResourcePath + File.separator + "templates/java/generator/DynamicFileGenerator.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/generator/DynamicFileGenerator.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // generator.MainGenerator
        inputFilePath = inputResourcePath + File.separator + "templates/java/generator/FileGenerator.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/generator/FileGenerator.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // pom.xml
        inputFilePath = inputResourcePath + File.separator + "templates/pom.xml.ftl";
        outputFilePath = outputPath + File.separator + "pom.xml";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // 生成README.md
        inputFilePath = inputResourcePath + File.separator + "templates/README.md.ftl";
        outputFilePath = outputPath + File.separator + "README.md";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);
    }

}
