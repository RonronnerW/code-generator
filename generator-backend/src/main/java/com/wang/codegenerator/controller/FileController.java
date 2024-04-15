package com.wang.codegenerator.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.utils.IOUtils;
import com.wang.codegenerator.common.BaseResponse;
import com.wang.codegenerator.common.ErrorCode;
import com.wang.codegenerator.common.ResultUtils;
import com.wang.codegenerator.config.CosClientConfig;
import com.wang.codegenerator.constant.FileConstant;
import com.wang.codegenerator.exception.BusinessException;
import com.wang.codegenerator.manager.CosManager;
import com.wang.codegenerator.model.dto.file.UploadFileRequest;
import com.wang.codegenerator.model.entity.Generator;
import com.wang.codegenerator.model.entity.User;
import com.wang.codegenerator.model.enums.FileUploadBizEnum;
import com.wang.codegenerator.service.GeneratorService;
import com.wang.codegenerator.service.UserService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.prefs.BackingStoreException;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.qcloud.cos.demo.BucketRefererDemo.cosClient;

/**
 * 文件接口
 *
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    @Resource
    private GeneratorService generatorService;

    /**
     * 文件上传
     *
     * @param multipartFile
     * @param uploadFileRequest
     * @param request
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
            UploadFileRequest uploadFileRequest, HttpServletRequest request) {
        String biz = uploadFileRequest.getBiz();
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        validFile(multipartFile, fileUploadBizEnum);
        User loginUser = userService.getLoginUser(request);
        // 文件目录：根据业务、用户来划分
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String filename = uuid + "-" + multipartFile.getOriginalFilename();
        String filepath = String.format("/%s/%s/%s", fileUploadBizEnum.getValue(), loginUser.getId(), filename);
        File file = null;
        try {
            // 上传文件
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath, file);
            // 返回可访问地址
            return ResultUtils.success(filepath);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }

    /**
     * 根据id获取生成器详情，再根据详情获取文件
     * @param id
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @GetMapping("/download")
    public void downloadFile(long id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(id<=0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Generator generator = generatorService.getById(id);
        if(generator==null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        String distPath = generator.getDistPath();
        if(StrUtil.isBlank(distPath)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 追踪事件
        log.info("用户 {} 下载了 {}", loginUser, distPath);
        // 设置响应头
        response.setContentType("application/octet-stream;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + distPath);

        // 优先从本地服务器缓存中获取
        String projectPath = System.getProperty("user.dir").replace("\\", "/");
        String descDir = String.format("%s/.temp/cache/%s", projectPath, id);
        String cachePath = FileUtil.normalize(descDir + File.separator + generator.getDistPath().substring(generator.getDistPath().lastIndexOf("/") + 1));
        if(FileUtil.exist(cachePath)) {
            Files.copy(Paths.get(cachePath), response.getOutputStream());
            return;
        }
        // 缓存中不存在， 从对象存储下载
        COSObjectInputStream cosObjectInput = null;
        try {
            COSObject cosObject = cosManager.getObject(distPath);
            cosObjectInput = cosObject.getObjectContent();
            // 处理下载到的流
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);

            // 写入响应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();

            // 保存到cache缓存中
            FileUtil.touch(cachePath);
            FileUtil.writeBytes(bytes, cachePath);

        } catch (Exception e) {
            log.error("file download error, filepath = " + distPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        } finally {
            if (cosObjectInput != null) {
                cosObjectInput.close();
            }
        }
    }

    /**
     * 校验文件
     *
     * @param multipartFile
     * @param fileUploadBizEnum 业务类型
     */
    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long ONE_M = 1024 * 1024L;
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (fileSize > ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1M");
            }
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }

}
