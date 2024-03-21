package com.wang.generator.file;

import cn.hutool.core.io.FileUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DynamicFileGenerator {

    public static void doGenerate(String source, String desc, Object data) throws IOException, TemplateException {
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
