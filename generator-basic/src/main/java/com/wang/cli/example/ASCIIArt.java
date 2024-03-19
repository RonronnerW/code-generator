package com.wang.cli.example;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
// some exports omitted for the sake of brevity
// 1. 创建一个实现 Runnable 或 Callable 的接口
// 2. Command注解 mixinStandardHelpOptions设置为true可以自动添加 --help 和--version
@Command(name = "ASCIIArt", version = "ASCIIArt 1.0", mixinStandardHelpOptions = true)
public class ASCIIArt implements Runnable {
    // 3. 通过Option注解将字段设置为命令行选项
    @Option(names = {"-s", "--font-size"}, description = "Font size")
    int fontSize = 19;
    //4. 通过Parameters注解将字段设置为命令行参数
    @Parameters(paramLabel = "<word>", defaultValue = "Hello, picocli",
            description = "Words to be translated into ASCII art.")
    private String[] words = {"Hello,", "picocli"};
    // 5. Picocli会自动将命令行参数注入到注解字段中
    // 6. 在类的run 或 call 中定义业务逻辑，命令行解析成功后被调用
    @Override
    public void run() {
        // 改成自己的run方法
        System.out.println("fontsize = " + fontSize);
        System.out.println("words = " + String.join(",", words));
    }
    // 7. 在main方法中，通过CommandLine对象的execute方法处理用户输入的命令
    // 8. CommandLine.execute 方法返回一个退出代码
    public static void main(String[] args) {
        int exitCode = new CommandLine(new ASCIIArt()).execute(args);
        System.exit(exitCode);
    }
}