package com.da.usercenter;

import cn.hutool.core.date.StopWatch;
import com.da.usercenter.common.PageRequest;
import com.da.usercenter.manager.RedisLimiterManager;
import com.da.usercenter.mapper.UserFollowsMapper;
import com.da.usercenter.mapper.UserMapper;
import com.da.usercenter.model.entity.User;
import com.da.usercenter.model.entity.UserTeam;
import com.da.usercenter.service.UserService;
import com.da.usercenter.service.UserTeamService;
import com.da.usercenter.utils.IpUtils;
import com.da.usercenter.utils.MailUtils;
import com.da.usercenter.utils.SMSUtils;
import com.da.usercenter.utils.ValidateCodeUtils;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@SpringBootTest
class UserCenterApplicationTests {

    @Resource
    private UserMapper userMapper;
    @Resource
    private UserService userService;
    @Resource
    private UserTeamService userTeamService;
    @Resource
    private UserFollowsMapper userFollowsMapper;

    @Resource
    private JavaMailSender javaMailSender;

    @Test
    public void testInsertUser(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 100000;
        // 分十组
        int batchSize = 100000;
        int j = 0;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {

            Random random = new Random();
            List<User> userList = new ArrayList<>();

            while(true){
                j++;
                User user = new User();
                user.setNickname("Da");
                user.setSex(1);
                user.setStates(0);
                user.setType(0);
                user.setLoginAccount("666888" + random.nextInt());
                user.setLoginPassword("12345678");
                user.setPhone("18370598888");
                user.setEmail("123@qq.com");
                user.setTags("[]");
                user.setProfilePhoto("https://z1.ax1x.com/2023/06/11/pCVNPyD.jpg");
                user.setProfile("我是一名程序员，^_^O(∩_∩)O哈哈~");
                userList.add(user);
                if(j % batchSize == 0){
                    break;
                }
            }

            // 异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
                userService.saveBatch(userList, batchSize);
            });
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());



    }

    @Test
    public void testUpdateUserById(){
        User user = new User();
        user.setId(1);
        user.setSex(1);
        user.setNickname("Daaaaaaa");
        userMapper.updateById(user);
    }

    @Test
    public void testGetUserById(){
        User user = userMapper.selectById(1);
        System.out.println(user);
    }

    @Test
    public void testSearchUsersByTags(){
        List<String> tags = Arrays.asList("python","java");
        List<User> users = userService.searchUsersByTags(tags);
        System.out.println(users);
        System.out.println("size = " + users.size());
    }

    @Test
    public void testGson(){
        String str = "['java','python','c','c++']";
        Gson gson = new Gson();
        List list = gson.fromJson(str, List.class);
        list.forEach(i ->{
            System.out.println(i);
        });
    }

    @Test
    public void testGetUserListByTeamId(){
        List<User> userList = userMapper.getUserListByTeamId(27);
        userList.forEach(user -> {
            System.out.println(user);
        });
    }

    @Test
    public void testSort(){
        List<UserTeam> userTeamList = userTeamService.list();
        userTeamList.sort(Comparator.comparing(UserTeam::getJoinTime));
        for (UserTeam userTeam : userTeamList) {
            System.out.println(userTeam.getJoinTime());
        }
    }

    @Test
    public void testInsert(){
        List<User> fansByUserId = userFollowsMapper.getFansByUserId(1);
        fansByUserId.forEach(user -> System.out.println(user));
    }


    @Test
    public void testSendMail(){
        String code = "6666";// 验证码
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject("【测试验证码】验证消息"); // 发送邮件的标题
        message.setText("登录操作，验证码："+ code + "，切勿将验证码泄露给他人，本条验证码有效期2分钟。"); // 发送邮件的内容
        message.setTo("2781335197@qq.com"); // 指定要接收邮件的用户邮箱账号
        message.setFrom("1349190697@qq.com"); // 发送邮件的邮箱账号，注意一定要和配置文件中的一致！
        javaMailSender.send(message); // 调用send方法发送邮件即可
    }


    @Test
    public void testSendCode(){
        RedisLimiterManager redisLimiterManager = new RedisLimiterManager();
        String code = ValidateCodeUtils.generateValidateCode(4).toString();
        // 调用阿里云api发送短信验证码
        System.out.println("生成的验证码为：" + code);
        SMSUtils.sendMessage("瑞吉外卖", "SMS_460765534", "18370952133", "6666");
    }




}
