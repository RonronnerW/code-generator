package com.wang.cli.command;

import cn.hutool.core.util.ReflectUtil;
import com.wang.model.MainTemplateConfig;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;
@Command(name = "config", description = "查看参数信息", mixinStandardHelpOptions = true)
public class ConfigCommand implements Callable {

    @Override
    public Object call() {
//        //反射实现
//        Class<?> myClass = MainTemplateConfig.class;
//        Field[] fields = myClass.getDeclaredFields();
        Field[] fields = ReflectUtil.getFields(MainTemplateConfig.class);
        for (Field field : fields) {
            System.out.println("参数名称：" + field.getName() + "\t" + "参数类型：" + field.getType());
        }
        return 0;
    }
}
