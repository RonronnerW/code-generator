package com.wang.generator.file;

import cn.hutool.core.io.FileUtil;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DynamicFileGenerator {

    /**
     * 使用类加载器加载资源文件进行动态生成
     * @param source 源文件路径
     * @param desc 目的目录
     * @param data 元信息
     * @throws IOException
     * @throws TemplateException
     */
    public static void doGenerate(String source, String desc, Object data) throws IOException, TemplateException {
        // Step2. 创建一个FreeMarker的全局配置对象，可以统一指定模板文件所在的路径、模板文件的字符集等
        // new 出 Configuration 对象，参数为 FreeMarker 版本号
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);

        // 使用类加载器获取模板文件
        int lastIndexOf = source.lastIndexOf("/");
        String basePackagePath = source.substring(0, lastIndexOf);
        String templateName = source.substring(lastIndexOf+1);
        ClassTemplateLoader classTemplateLoader = new ClassTemplateLoader(DynamicFileGenerator.class, basePackagePath);
        configuration.setTemplateLoader(classTemplateLoader);
        // 指定模板文件所在的路径
//        configuration.setDirectoryForTemplateLoading(new File(source).getParentFile());
        // 设置模板文件使用的字符集
        configuration.setDefaultEncoding("utf-8");

        // Step3.
        // 创建模板对象，加载指定模板
        Template template = configuration.getTemplate(new File(templateName).getName(), "utf-8");

        //输出目录是否存在
        if(!FileUtil.exist(desc)) {
            FileUtil.touch(desc);
        }
        // Step4. 创建数据模型
        // Tips: 要保证数据的质量和规范性，可以用参数配置类来传递
        // 反之，灵活构造可以选用HashMap集合

        // Step5. 指定生成的文件
//        Writer out = new FileWriter(desc);
        // 解决中文乱码问题
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(Paths.get(desc)), StandardCharsets.UTF_8));
        // Step6. 调用process方法，处理并生成文件
        template.process(data, out);
        // 生成文件后关闭
        out.close();
    }

    /**
     * freeMaker动态文件生成-使用路径
     * @param source 源路径
     * @param desc 目标路径
     * @param data 模型数据
     * @throws IOException
     * @throws TemplateException
     */
    public static void doGenerateByPath(String source, String desc, Object data) throws IOException, TemplateException {
        // Step2. 创建一个FreeMarker的全局配置对象，可以统一指定模板文件所在的路径、模板文件的字符集等
        // new 出 Configuration 对象，参数为 FreeMarker 版本号
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);
        // 指定模板文件所在的路径
        configuration.setDirectoryForTemplateLoading(new File(source).getParentFile());
        // 设置模板文件使用的字符集
        configuration.setDefaultEncoding("utf-8");

        // Step3.
        // 创建模板对象，加载指定模板
        Template template = configuration.getTemplate(new File(source).getName(), "utf-8");

        //输出目录是否存在
        if(!FileUtil.exist(desc)) {
            FileUtil.touch(desc);
        }
        // Step4. 创建数据模型
        // Tips: 要保证数据的质量和规范性，可以用参数配置类来传递
        // 反之，灵活构造可以选用HashMap集合

        // Step5. 指定生成的文件
//        Writer out = new FileWriter(desc);
        // 解决中文乱码问题
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(Paths.get(desc)), StandardCharsets.UTF_8));
        // Step6. 调用process方法，处理并生成文件
        template.process(data, out);
        // 生成文件后关闭
        out.close();
    }

}
