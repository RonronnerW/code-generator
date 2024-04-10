package ${basePackage}.cli.command;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
    import ${basePackage}.generator.FileGenerator;
import ${basePackage}.model.DataModel;
import lombok.Data;
import picocli.CommandLine;

import java.util.concurrent.Callable;

/**
 * @author ${author}
 * @date ${.now}
 * @description 读取json生成命令
 */
@Data
@CommandLine.Command(name = "json-generate", mixinStandardHelpOptions = true, description = "读取json生成命令")
public class JsonGenerateCommand implements Callable {


    /**
     * json文件路径
     */
    @CommandLine.Option(
            names = {"-f", "--file"},
            arity = "0..1",
            description = "json 文件路径",
            echo = true,
            interactive = true)
    private String filePath;

    @Override
    public Integer call() throws Exception{

        String dataModelStr = FileUtil.readUtf8String(filePath);
        DataModel dataModel = JSONUtil.toBean(dataModelStr, DataModel.class);
        FileGenerator.doGenerate(dataModel);
        return 0;
    }
}
