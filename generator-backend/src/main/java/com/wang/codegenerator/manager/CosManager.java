package com.wang.codegenerator.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.*;
import com.qcloud.cos.transfer.Download;
import com.qcloud.cos.transfer.TransferManager;
import com.wang.codegenerator.config.CosClientConfig;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jakarta.annotation.PostConstruct;
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

    private TransferManager transferManager;

    @PostConstruct // 该注解是bean加载完成后执行
    public void init() {

        // 自定义线程池大小，建议在客户端与 COS 网络充足（例如使用腾讯云的 CVM，同地域上传 COS）的情况下，设置成16或32即可，可较充分的利用网络资源
        // 对于使用公网传输且网络带宽质量不高的情况，建议减小该值，避免因网速过慢，造成请求超时。
        ExecutorService threadPool = Executors.newFixedThreadPool(32);
        // 传入一个 threadpool, 若不传入线程池，默认 TransferManager 中会生成一个单线程的线程池。
        transferManager = new TransferManager(cosClient, threadPool);
    }

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

    /**
     * 下载文件到本地
     * @param key 本地路径
     * @return
     */
    public Download download(String key, String desc) throws InterruptedException {
        // 返回一个异步结果 Download, 可同步的调用 waitForCompletion 等待下载结束, 成功返回 void, 失败抛出异常
        String bucketName = cosClientConfig.getBucket();
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, key);
        Download download = transferManager.download(getObjectRequest, new File(desc));
        download.waitForCompletion();
        return download;
    }

    /**
     * 下载文件到本地
     * @param key 本地路径
     * @return
     */
    public void downloadToLocal(String key, String desc){
        // 方法2 下载文件到本地的路径，例如 把文件下载到本地的 /path/to/路径下的localFile文件中
        String bucketName = cosClientConfig.getBucket();
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, key);
        cosClient.getObject(getObjectRequest, new File(desc));
    }

}
