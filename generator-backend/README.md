# 云平台开发

在线平台需要支持用具在线搜索、使用、制作和分享各类代码生成器，帮助开发者提高定制化开发效率

1. **数据**线上化
   1. 元信息，即把元信息配置保存到数据库中
   1. 项目模板，即把静态文件和 FTL 模板文件存储到服务器中
   1. 代码生成器，即把代码生成器**产物包**存储到服务器中

1. **功能**线上化
   1. 在线查看生成器的信息
   1. 在线**使用**生成器
   1. 在线**制作**生成器




# 生成器在线使用

1. 用户登录云平台后，打开某个生成器的详情页面 点击在线使用生成器
2. 跳转到填写表单参数页面 请求后端
3. 后端从数据库中查询生成器信息 获取到代码生成器的制作工具的产物包的路径
4. 后端从对象存储下载产物包到本地解压 
5. 后端操作解压后的文件夹 通过用户的参数以及制作工具的脚本文件 得到生成的最终代码
6. 后端将代码返回给用户下载
7. 清除下载的资源 防止磁盘满溢



# 生成器在线制作

在线使用制作工具，通过输入制作信息、上传模板文件，就能得到制作好的生成器

1. 用户打开生成器制作工具表单，上传生成器信息和模型文件
2. 后端下载模型文件到本地
3. 构造生成器需要的元信息对象，并指定输出路径
4. 后端调用maker制作工具，输入上述参数，得到代码生成器
5. 后端将代码生成器返回给用户，前端下载



处理资源路径问题：

之前的maker项目中resource下的ftl模板文件都是通过路径读取的，然而项目被制作为jar包引入后，无法再通过文件路径获取模板文件。

解决方案：不再通过路径获取资源，而是通过类加载器根据资源相对路径获取资源



# 核心功能优化

**下载优化方式（包括下载生成器接口/使用生成器接口/制作生成器接口）**

1. 遵循最佳实践。下载是从腾讯云的对象存储，考虑优化对象存储下载时，应该充分了解第三方服务，参考优化方式。例如，[对象存储 请求速率与性能优化-最佳实践-文档中心-腾讯云 (tencent.com)](https://cloud.tencent.com/document/product/436/13653)

2. 流式处理。下载大文件时，除了下载速度慢之外，还会额外占用服务器的CPU、内存、磁盘。如果文件较大且不需要服务端处理的文件，可以选用流式处理通过循环方式写入流。

3. 本地缓存。将下载过一次的文件保存在服务器上，再次使用时直接从服务器获取，不用再从对象存储获取。

   缓存的4个核心要素：

   1. 缓存哪些内容？
   2. 缓存如何淘汰？
   3. 缓存 key 如何涉及？
   4. 如何保证缓存一致性？

**定时任务定期清理缓存文件**

使用Spring Scheduler：

1. 主启动类 添加注解 `@EnableScheduling`
2. 定时执行的方法添加 `@Sceduling` 注解，并指定cron表达式或者执行频率 [在线Cron表达式生成器 (qqe2.com)](https://cron.qqe2.com/)

定时任务要注意控制同一时间只有1台服务器执行：

1. 分离定时任务和主程序，只在一个服务器运行定时任务，成本太大
2. 写死配置，只有固定ip的服务器执行定时任务
3. 动态配置，数据库、redis、配置中心
4. 分布式锁，只有抢到锁的服务器的才能执行定时任务


