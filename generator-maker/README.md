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





# 配置能力增强

## 1. 一个参数对应某个文件是否生成

1. 修改元信息，modeInfo中添加对应的模型参数
2. 文件信息与模型参数关联，在fileConfig添加字段绑定模型参数
3. 文件生成代码中使用模型参数控制是否生成文件

## 2. 一个参数对应多个文件是否生成

最简单的方式是给文件绑定相同的参数，但是修改该参数比较麻烦，或者对文件进行分组，两种方案：

1. fileInfo添加分组信息，缺点是获取分组下的所有文件不方便

2. 把文件组当成一个特殊的文件夹，把同组文件放到该组配置下

## 3. 一个参数控制多处代码修改以及文件是否生成

让同一个参数出现在contidion中控制文件生成，又出现在动态模板中作为生成代码的参数即可

## 4. 定义一组参数，控制代码修改或文件生成

对模型参数进行分组，各组下的模型参数互相隔离、保证不会出现命名冲突

1. 对同组参数进行封装
2. FreeMaker的 `@ArgGroup` 注解可以用于标识分组，将内容自动填充到对象中

## 5. 定义可选开启的参数组

1. 给每个参数组创建一个独立的 Picocli Command类，然后让该类去触发另一个Command类
2. 编写Picocli命令类，在类中使用静态内部类的方式定义分组命令，使用主类run/call方法控制命令交互





# 模板制作工具

不用手动制作模板文件，由代码生成 .ftl 数据模板文件到临时的.temp目录下，代码生成器从此读取数据模板文件生成代码

向制作工具输入：基本信息 + 输入文件 +模型参数（+ 输出规则）

由制作工具输出：模板文件 ＋ 元信息配置



template包下是模板制作工具的具体代码

步骤：

1. 提供输入参数，包括项目基本信息，项目原始目录，原始文件，模型参数
2. 基于字符替换算法，使用模型参数名称替换原始文件的指定内容，并创建ftl模板文件
3. 使用输入信息创建meta.json元信息文件



MyBatis 动态参数语法字符串 和 FreeMarker 模板的参数替换 语法冲突：

解决方法：在`PostMapper.xml.ftl`文件中，用`<#noparse>`语法设置某些字段不被 FreeMarker 解析：

```xml
<select id="listPostWithDelete" resultType="${basePackage}.springbootinit.model.entity.Post">
    select *
    from post
    where updateTime >= <#noparse>#{minUpdateTime}</#noparse>
</select>
```

