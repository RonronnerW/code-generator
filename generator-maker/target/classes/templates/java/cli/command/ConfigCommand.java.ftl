package ${basePackage}.cli.command;

import cn.hutool.core.util.ReflectUtil;
import ${basePackage}.model.DataModel;
import picocli.CommandLine.Command;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;
@Command(name = "config", description = "查看参数信息", mixinStandardHelpOptions = true)
public class ConfigCommand implements Callable {

    @Override
    public Object call() throws Exception {
        Field[] fields = ReflectUtil.getFields(DataModel.class);
        for (Field field : fields) {
            System.out.println("参数名称：" + field.getName() + "\t" + "参数类型：" + field.getType());
        }
        return 0;
    }
}
