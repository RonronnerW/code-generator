# 代码生成器制作工具

## 1. 读取元信息

元信息放到meta.json文件中

```json
{
  "name": "acm-template-pro-generator",
  "description": "ACM 示例模板生成器",
  "basePackage": "com.wang",
  "version": "1.0",
  "author": "wang",
  "createTime": "2023-11-22",
  "fileConfig": {
      // 控制文件生成的配置
  },
  "modelConfig": {
      // 控制数据模型信息
  }
}
```

创建Meta类用于接收json字段，使用IDEA插件 `GsonFormatPlus` 将json文件转java类代码

**读取元信息-单例模式**

使用ResourceUtil工具类从资源路径下读取json文件，然后使用JSONUtil.toBean() 方法将json字符串转java对象

程序运行期间保留一个Meta对象即可，使用**双检锁单例模式**获取元信息

```java
public class MetaManager {
    private static volatile Meta meta;
    public static Meta getMeta() {
        if(meta==null) {
            synchronized (MetaManager.class) {
                if(meta==null) {
                    meta = initMeta();
                }
            }
        }
        return meta;
    }

    private static Meta initMeta() {
        String metaJson = ResourceUtil.readUtf8Str("meta.json");
        Meta meta = JSONUtil.toBean(metaJson, Meta.class);
        MetaValidator.doValidaAndFill(meta);
        return meta;
    }
}
```

## 2. 生成数据模型

使用FreeMaker框架根据元信息的modelConfig制作ftl数据模型文件

`DataModel.java.ftl`

## 3. 生成Picocli命令类 

类似的在resource下制作命令类的数据模型文件

`ConfigCommand.java.ftl`

`GenerateCommand.java.ftl`

`ListCommand.java.ftl`

`CommandExecutor.java.ftl`

## 4. 生成代码生成文件

`DynamicFileGenerator.java.ftl`

`StaticFileGenerator.java.ftl`

`FileGenerator.java.ftl`

## 5. 程序构建jar包

使用Java内置的Process类执行maven打包命令

`JarGenerator.java`

## 6. 程序封装脚本文件

`ScriptGenerator.java`

