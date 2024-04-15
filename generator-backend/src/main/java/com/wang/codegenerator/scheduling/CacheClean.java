package com.wang.codegenerator.scheduling;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CacheClean {
    //每天执行，预热推荐用户 秒-分-时-日-月-年
    @Scheduled(cron = "0 0 1 * * *")
    public void doCacheClean(){
        // 优先从本地服务器缓存中获取
        String projectPath = System.getProperty("user.dir").replace("\\", "/");
        String descDir = String.format("%s/.temp/cache", projectPath);
        if(FileUtil.exist(descDir)) {
            FileUtil.del(descDir);
        }
        log.info("执行定时任务删除服务器缓存文件: "+descDir);
    }
}
