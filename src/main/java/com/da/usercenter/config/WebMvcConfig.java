package com.da.usercenter.config;

import com.da.usercenter.interceptor.RefreshLoginStatusInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * 跨域配置
 *
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private RedisTemplate redisTemplate;

    @Bean
    public WebMvcConfigurer corsConfigurer(){
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // 设置允许跨域的路由
                registry.addMapping("/**")
                        // 设置允许跨域请求的域名
                        .allowedOrigins("*")
                        // 再次加入前端Origin  localhost！=127.0.0.1
                        .allowedOrigins("http://127.0.0.1:5173")
//                        .allowedOrigins("http://8.130.133.165")
                        // 是否允许证书（cookies）
                        .allowCredentials(true)
                        // 设置允许的方法
                        .allowedMethods("*")
                        //允许请求头
                        .allowedHeaders("*")
                        // 跨域允许时间
                        .maxAge(3600);
            }
        };
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // token刷新的拦截器
        registry.addInterceptor(new RefreshLoginStatusInterceptor(redisTemplate)).addPathPatterns("/**").order(0);
    }



}