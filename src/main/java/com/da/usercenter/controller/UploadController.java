package com.da.usercenter.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.da.usercenter.common.ErrorCode;
import com.da.usercenter.common.ResponseResult;
import com.da.usercenter.exception.BusinessException;
import com.da.usercenter.model.entity.Team;
import com.da.usercenter.model.entity.User;
import com.da.usercenter.service.TeamService;
import com.da.usercenter.service.UploadService;
import com.da.usercenter.service.UserService;
import com.da.usercenter.utils.GiteeImgBed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 上传图床
 */
@RestController
@RequestMapping("/img")
@Transactional(rollbackFor = Exception.class)
//@CrossOrigin(origins = {"http://8.130.133.165"},allowCredentials = "true")
//@CrossOrigin(origins = {"http://127.0.0.1:5173"}, allowCredentials = "true")
public class UploadController {
    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private UploadService uploadService;



    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 上传头像
     *
     * @param files
     * @return
     * @throws Exception
     */
    @PostMapping("/uploadAwatar")
    public ResponseResult<Boolean> uploadAwatar(@RequestParam("file") MultipartFile[] files, HttpServletRequest request) throws Exception {
        User currentUser = userService.getCurrentUser(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long id = currentUser.getId();
        List<String> msgUrlList = uploadService.uploadMsg(files, id);

        //修改用户头像
        User user = new User();
        user.setId(id);
        user.setProfilePhoto(msgUrlList.get(0));
        // 更新 redis 缓存
        boolean res = userService.updateById(user);
        redisTemplate.delete("user:login:" + user.getId());
        User u = userService.getById(id);
        User safeUser = userService.getSafeUser(u);
        redisTemplate.opsForValue().set("user:login:" + user.getId(), safeUser);
        return ResponseResult.success(res);
    }

    /**
     * 上传队伍封面
     * @param files
     * @param id
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/uploadTeamImg")
    public ResponseResult<Boolean> uploadTeamImg(@RequestParam("file") MultipartFile[] files,@RequestParam("id") Long id, HttpServletRequest request) throws Exception {
        User currentUser = userService.getCurrentUser(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        if(files.length == 0 || id == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        List<String> msgUrlList = uploadService.uploadMsg(files, id);

        //修改队伍封面
        Team team = new Team();
        team.setId(id);
        team.setProfilePhoto(msgUrlList.get(0));
        boolean res = teamService.updateById(team);
        return ResponseResult.success(res);
    }




    /**
     * 删除图片
     *
     * @param imgPath
     * @return
     * @throws Exception
     */
    @DeleteMapping("/del")
    @ResponseBody
    public ResponseResult<Boolean> delImg(@RequestParam(value = "imgPath") String imgPath) throws Exception {
        //路径不存在不存在时
        if (imgPath == null || "".equals(imgPath.trim())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "路径不存在");
        }
        String path = imgPath.split("master/")[1];
        //上传图片不存在时
        if (path == null || "".equals(path.trim())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片不存在");
        }
        //设置请求路径
        String requestUrl = String.format(GiteeImgBed.GET_IMG_URL, GiteeImgBed.OWNER,
                GiteeImgBed.REPO_NAME, path);
        logger.info("请求Gitee仓库路径:{}", requestUrl);

        //获取图片所有信息
        String resultJson = HttpUtil.get(requestUrl);

        JSONObject jsonObject = JSONUtil.parseObj(resultJson);
        if (jsonObject == null) {
            logger.error("Gitee服务器未响应,message:{}", jsonObject);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Gitee服务器未响应");
        }
        //获取sha,用于删除图片
        String sha = jsonObject.getStr("sha");
        //设置删除请求参数
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("access_token", GiteeImgBed.ACCESS_TOKEN);
        paramMap.put("sha", sha);
        paramMap.put("message", GiteeImgBed.DEl_MESSAGE);
        //设置删除路径
        requestUrl = String.format(GiteeImgBed.DEL_IMG_URL, GiteeImgBed.OWNER,
                GiteeImgBed.REPO_NAME, path);
        logger.info("请求Gitee仓库路径:{}", requestUrl);
        //删除文件请求路径
        resultJson = HttpRequest.delete(requestUrl).form(paramMap).execute().body();
        HttpRequest.put(requestUrl).form(paramMap).execute().body();
        jsonObject = JSONUtil.parseObj(resultJson);
        //请求之后的操作
        if (jsonObject.getObj("commit") == null) {
            logger.error("Gitee服务器未响应,message:{}", jsonObject);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Gitee服务器未响应");
        }
        return ResponseResult.success(true);
    }

}
