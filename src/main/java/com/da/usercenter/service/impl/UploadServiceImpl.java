package com.da.usercenter.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.da.usercenter.common.ErrorCode;
import com.da.usercenter.exception.BusinessException;
import com.da.usercenter.service.UploadService;
import com.da.usercenter.utils.GiteeImgBed;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class UploadServiceImpl implements UploadService {

    /**
     * 上传图片
     * @param files
     * @return 图片链接地址
     */
    @Override
    public List<String> uploadImg(MultipartFile[] files, Long id) throws IOException {
        ArrayList<String> imgUrlList = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            String originaFileName = files[i].getOriginalFilename();
            //上传图片不存在时
            if (originaFileName == null) {
                throw new BusinessException(ErrorCode.NULL_ERROR);
            }
            String suffix = originaFileName.substring(originaFileName.lastIndexOf("."));
            //设置图片名字
            String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + suffix;

            String paramImgFile = Base64.encode(files[i].getBytes());
            //设置转存到Gitee仓库参数
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("access_token", GiteeImgBed.ACCESS_TOKEN);
            paramMap.put("message", GiteeImgBed.ADD_MESSAGE);
            paramMap.put("content", paramImgFile);

            //转存文件路径
            String targetDir = GiteeImgBed.PATH + fileName;
            //设置请求路径
            String requestUrl = String.format(GiteeImgBed.CREATE_REPOS_URL, GiteeImgBed.OWNER,
                    GiteeImgBed.REPO_NAME, targetDir);
            String resultJson = HttpUtil.post(requestUrl, paramMap);
            JSONObject jsonObject = JSONUtil.parseObj(resultJson);
            //表示操作失败
            if (jsonObject == null || jsonObject.getObj("commit") == null) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "操作失败");
            }
            JSONObject content = JSONUtil.parseObj(jsonObject.getObj("content"));

            // 图片路径   这个路径记住后面要讲！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
            String img = "https://gitee.com/xiangdayyds/img/raw/master" + targetDir;
            imgUrlList.add(img);
        }
        return imgUrlList;
    }
}
