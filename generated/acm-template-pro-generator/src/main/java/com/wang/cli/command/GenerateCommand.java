package com.wang.cli.command;

import cn.hutool.core.bean.BeanUtil;
import com.wang.generator.FileGenerator;
import com.wang.model.DataModel;
import lombok.Data;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

/**
 * 子命令类
 */
@Command(name = "generate", description = "生成代码", mixinStandardHelpOptions = true)
@Data
public class GenerateCommand implements Callable {

    @Option(names = {"-n", "--needGit"}, arity = "0..1", description = "是否生成.gitignore文件", interactive = true, echo = true)
    private boolean needGit = true;
    @Option(names = {"-l", "--loop"}, arity = "0..1", description = "是否生成循环", interactive = true, echo = true)
    private boolean loop = false;
    static private DataModel.MainTemplate mainTemplate = new DataModel.MainTemplate();

    @Command(name = "mainTemplate", description = "核心模板")
    @Data
    public static class MainTemplateCommand implements Callable{
        @Option(names = {"-a", "--author"}, arity = "0..1", description = "作者注释", interactive = true, echo = true)
        private String author = "dexter";
        @Option(names = {"-o", "--outputText"}, arity = "0..1", description = "输出信息", interactive = true, echo = true)
        private String outputText = "sum = ";

        @Override
        public Object call() throws Exception {
            mainTemplate.author = author;
            mainTemplate.outputText = outputText;

            return 0;
        }
    }

    public Integer call() throws Exception {
        if (loop) {
            System.out.println("输入核心模板配置：");
            CommandLine commandLine = new CommandLine(MainTemplateCommand.class);
            commandLine.execute("--author","--outputText");
        }
        DataModel dataModel = new DataModel();
        BeanUtil.copyProperties(this, dataModel);
        dataModel.mainTemplate = mainTemplate;
        FileGenerator.doGenerate(dataModel);
        return 0;
    }
}
