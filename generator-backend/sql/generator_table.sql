-- 创建库
create database if not exists code_generator;

-- 切换库
use code_generator;

-- 用户表
create table if not exists t_user
(
    id           bigint auto_increment comment 'id' primary key,
    user_account  varchar(256)                           not null comment '账号',
    user_password varchar(512)                           not null comment '密码',
    user_name     varchar(256)                           null comment '用户昵称',
    user_avatar   varchar(1024)                          null comment '用户头像',
    user_profile  varchar(512)                           null comment '用户简介',
    user_role     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    create_time   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete     tinyint      default 0                 not null comment '是否删除',
    index idx_user_account (user_account)
    ) comment '用户' collate = utf8mb4_unicode_ci;

-- 代码生成器表
create table if not exists t_generator
(
    id          bigint auto_increment comment 'id' primary key,
    name        varchar(128)                       null comment '名称',
    description text                               null comment '描述',
    base_package varchar(128)                       null comment '基础包',
    version     varchar(128)                       null comment '版本',
    author      varchar(128)                       null comment '作者',
    tags        varchar(1024)                      null comment '标签列表（json 数组）',
    picture     varchar(256)                       null comment '图片',
    file_config  text                               null comment '文件配置（json 字符串）',
    model_config text                               null comment '模型配置（json 字符串）',
    dist_path    text                               null comment '代码生成器产物路径',
    status      int      default 0                 not null comment '状态',
    user_id      bigint                             not null comment '创建用户 id',
    create_time  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete    tinyint  default 0                 not null comment '是否删除',
    index idx_user_id (user_id)
    ) comment '代码生成器' collate = utf8mb4_unicode_ci;

-- 模拟 用户数据
INSERT INTO code_generator.t_user(id,user_account,user_password,user_name,user_avatar,user_profile,user_role) VALUES (1,'wang','df3f595406c59c46af221924d171f30f','管理员豹警官','https://i0.hdslb.com/bfs/archive/502c3df0d2d64d7f60819e8dda97e3c1e59efdf2.jpg','豹警官：勇敢维护动物城和平，夏奇羊粉丝，甜甜圈的忠实爱好者。','admin');
INSERT INTO code_generator.t_user(id,user_account,user_password,user_name,user_avatar,user_profile,user_role) VALUES (2,'wang2','df3f595406c59c46af221924d171f30f','档案室豹警官','https://gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png','豹警官：勇敢维护动物城和平，夏奇羊粉丝，甜甜圈的忠实爱好者。','user');

-- 模拟 代码生成器数据
INSERT INTO code_generator.t_generator(id,name,description,base_package,version,author,tags,picture,file_config,model_config,dist_path,status,user_id) VALUES (1,'ACM 模板项目','ACM 模板项目生成器','com.dexcode','1.0','管理员豹警官','["Java"]','https://pic.yupi.icu/1/_r0_c1851-bf115939332e.jpg','{}','{}',null,0,1);
INSERT INTO code_generator.t_generator(id,name,description,base_package,version,author,tags,picture,file_config,model_config,dist_path,status,user_id) VALUES (2,'Spring Boot 初始化模板','Spring Boot 初始化模板项目生成器','com.dexcode','1.0','管理员豹警官','["Java"]','https://pic.yupi.icu/1/_r0_c0726-7e30f8db802a.jpg','{}','{}',null,0,1);
INSERT INTO code_generator.t_generator(id,name,description,base_package,version,author,tags,picture,file_config,model_config,dist_path,status,user_id) VALUES (3,'鱼皮外卖','鱼皮外卖项目生成器','com.dexcode','1.0','管理员豹警官','["Java", "前端"]','https://pic.yupi.icu/1/_r1_c0cf7-f8e4bd865b4b.jpg','{}','{}',null,0,1);
INSERT INTO code_generator.t_generator(id,name,description,base_package,version,author,tags,picture,file_config,model_config,dist_path,status,user_id) VALUES (4,'鱼皮用户中心','鱼皮用户中心项目生成器','com.dexcode','1.0','管理员豹警官','["Java", "前端"]','https://pic.yupi.icu/1/_r1_c1c15-79cdecf24aed.jpg','{}','{}',null,0,1);
INSERT INTO code_generator.t_generator(id,name,description,base_package,version,author,tags,picture,file_config,model_config,dist_path,status,user_id) VALUES (5,'鱼皮API开放平台','鱼皮API开放平台项目生成器','com.dexcode','1.0','管理员豹警官','["Java", "前端"]','https://pic.yupi.icu/1/_r1_c0709-8e80689ac1da.jpg','{}','{}',null,0,1);