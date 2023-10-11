package com.da.usercenter.controller;

import com.da.usercenter.common.ErrorCode;
import com.da.usercenter.common.ResponseResult;
import com.da.usercenter.exception.BusinessException;
import com.da.usercenter.model.entity.User;
import com.da.usercenter.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
//@CrossOrigin(origins = {"http://8.130.133.165"},allowCredentials = "true")
@CrossOrigin(origins = {"http://127.0.0.1:5173"},allowCredentials = "true")
public class UploadController {

    public static final int AVATAR_MAX_SIZE = 10 * 1024 * 1024;

    private final Logger logger = LoggerFactory.getLogger(UploadController.class);

    public static final List<String> AVATAR_TYPES = new ArrayList<String>();

    @Resource
    private UserService userService;

    /** 初始化允许上传的头像的文件类型 */
    static {
        AVATAR_TYPES.add("image/jpeg");
        AVATAR_TYPES.add("image/png");
        AVATAR_TYPES.add("image/bmp");
        AVATAR_TYPES.add("image/gif");
        AVATAR_TYPES.add("text/plain");
        AVATAR_TYPES.add("application/vnd.ms-excel");
        AVATAR_TYPES.add("application/msword");
    }


    @RequestMapping("/uploadFile")
    public ResponseResult<Boolean> changeAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        // 是否登录
        User currentUser = userService.getCurrentUser(request);
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        // 判断上传的文件是否为空
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        // 判断上传的文件大小是否超出限制值
        if (file.getSize() > AVATAR_MAX_SIZE) { // getSize()：返回文件的大小，以字节为单位
            // 是：抛出异常
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"不允许上传超过" + (AVATAR_MAX_SIZE / 1024) + "KB的头像文件");
        }

        // 判断上传的文件类型是否超出限制
        String contentType = file.getContentType();
        logger.info("文件类型contentType：{}", contentType);

        if (!AVATAR_TYPES.contains(contentType)) {
            // 是：抛出异常
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"不允许上传超过" + ("不支持使用该类型的文件作为头像，允许的文件类型：" + AVATAR_TYPES));

        }

        // 获取当前项目的绝对磁盘路径
        String parent = "/www/wwwroot/awata";
        // 保存头像文件的文件夹
        File dir = new File(parent);
        if (!dir.exists()) {
            //若是文件路径不存在，就新建文件路径
            dir.mkdirs();
        }


        // 保存的头像文件的文件名
        String suffix = "";

        String originalFilename = file.getOriginalFilename();

        int index = originalFilename.indexOf(".");
        if (index > 0) {
            suffix = originalFilename.substring(index);
        }

        // 拼接文件名
        String filename = currentUser.getId() + suffix;

        // 创建文件对象，表示保存的头像文件
        File dest = new File(dir, filename);

        try {
            file.transferTo(dest);
        } catch (IllegalStateException e) {
            // 抛出异常
            logger.info("文件状态异常，可能文件已被移动或删除");
        } catch (IOException e) {
            // 抛出异常
            logger.info("上传文件时读写错误，请稍后重新尝试");
        }

        User user = new User();
        user.setId(currentUser.getId());
        user.setProfilePhoto(parent + "\\" +  filename);
        boolean res = userService.updateById(user);
        return ResponseResult.success(res);
    }
}