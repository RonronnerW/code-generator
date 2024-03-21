package ${basePackage}.cli.command;

import cn.hutool.core.io.FileUtil;
import picocli.CommandLine.Command;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "list", description = "查看文件列表", mixinStandardHelpOptions = true)
public class ListCommand implements Callable {

    @Override
    public Object call() throws Exception {
        String inputRootPath = "${fileConfig.inputRootPath}";
        List<File> files = FileUtil.loopFiles(inputRootPath);
        for (File file : files) {
            // 打印文件信息
            System.out.println(file);
        }
        return 0;
    }
}