# 本地代码生成器

## 1. 准备原始代码，用于后续生成

generator-demo 存放原始代码

## 2. 生成静态文件和动态文件

### 静态文件生成

这里的`静态文件`指的是，可以直接复制、不做任何改动的文件

输入一个项目的目录，在另一个位置（新）生成一模一样的项目文件

***本质上就是复制文件***，有两种方法可以选择：

1. 使用现成的工具库直接复制完整目录 [Hutool](https://www.hutool.cn/)
2. 手动递归复制目录和文件

### 动态文件生成

借助**模板引擎**，实现模板编写和动态内容生成

模板引擎是一种用于生成动态内容的类库（或框架），通过将预定义的**模板**与特定**数据**合并，来生成最终的输出

优点：

1. 模板引擎已经提供了现成的模板文件**语法和解析**能力，开发者只需要按照特定要求去编写模板文件，使用`${参数}`语法，模板引擎就能**自动**将参数**注入**到模板中，得到完整文件，无需个人再编写解析逻辑了
2. 模板引擎将数据和模板分离，让不同职责的开发人员**独立工作**，比如后端专注于来发业务逻辑提供数据，前端专注于写模板，让**系统更易于维护**
3. 具有一定的安全特性，比如防止跨站脚本攻击

常见的模板引擎技术有以下几种，鱼皮选择了知名的、较为稳定、经典的FreeMarker模板引擎作为讲解和使用：

* Java的 [Thymeleaf](https://www.thymeleaf.org/)
* FreeMarker [FreeMarker](https://freemarker.apache.org/index.html)
* Velocity [velocity](https://gitee.com/apache/velocity)
* 前端的Mustache [mustache](https://github.com/mustache/mustache)

#### 实现

1. 定义数据模型 ` mainTemplateConfig.java`
2. 编写动态模板  ` MainTemplate.java.ftl`
3. 组合生成  `generator/MainGenerator`
4. 完善优化

## 3. Picocli 命令行开发

我们的生成器需要三种子命令：

- generate 子命令：生成文件
- list 子命令：查看要生成的原始文件列表信息
- config 子命令：查看允许用户传入的动态参数信息

开发步骤：

1. 创建命令执行器（主命令）：创建子命令类（`commond`包下），创建命令执行器`CommandExecutor`类-调用者，负责绑定所有子命令，并提供执行命令的方法
2. 分别实现每种子命令
3. 提供项目的全局调用入口：创建`Main`类，作为整个代码生成器项目的全局调用入口，作用是接收用户的参数、创建命令执行器并调用执行

## 4. 封装为jar包和脚本，生成代码

1. 构建程序jar包

   ```xml
   <build>
       <plugins>
           <plugin>
               <groupId>org.apache.maven.plugins</groupId>
               <artifactId>maven-assembly-plugin</artifactId>
               <version>3.3.0</version>
               <configuration>
                   <descriptorRefs>
                       <descriptorRef>jar-with-dependencies</descriptorRef>
                   </descriptorRefs>
                   <archive>
                       <manifest>
                           <mainClass>com.wang.Main</mainClass> <!-- 替换为你的主类的完整类名 -->
                       </manifest>
                   </archive>
               </configuration>
               <executions>
                   <execution>
                       <phase>package</phase>
                       <goals>
                           <goal>single</goal>
                       </goals>
                   </execution>
               </executions>
           </plugin>
       </plugins>
   </build>
   ```

2. 简化使用 封装脚本

   ```sh
   #!/bin/bash
   java -jar target/dexcode-generator-basic-1.0-SNAPSHOT-jar-with-dependencies.jar "$@"
   ```

   ```bash
   @echo off
   java -jar target/dexcode-generator-basic-1.0-SNAPSHOT-jar-with-dependencies.jar %*
   ```

   
