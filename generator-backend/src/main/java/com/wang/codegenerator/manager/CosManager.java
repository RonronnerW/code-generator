package com.wang.codegenerator.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.*;
import com.wang.codegenerator.config.CosClientConfig;
import java.io.File;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * Cos 对象存储操作
 *
 *
 */
@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 上传对象
     *
     * @param key 唯一键
     * @param localFilePath 本地文件路径
     * @return
     */
    public PutObjectResult putObject(String key, String localFilePath) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                new File(localFilePath));
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传对象
     *
     * @param key 唯一键
     * @param file 文件
     * @return
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 下载对象
     * @param key 唯一键
     * @return 下载对象
     */
    public COSObject getObject(String key) {
        // 方法1 获取下载输入流
        String bucketName = cosClientConfig.getBucket();
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, key);
        return cosClient.getObject(getObjectRequest);
    }
}
