package com.hmdp.config;

import com.hmdp.utils.LoginInterceptor;
import com.hmdp.utils.RefreshTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Autowired
    private RefreshTokenInterceptor refreshTokenInterceptor;

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        // 添加刷新令牌的拦截器
        registry.addInterceptor(refreshTokenInterceptor).order(0);
        // 添加登录拦截器
        registry.addInterceptor(loginInterceptor)
                .excludePathPatterns("/user/login", "/user/code", "/shop/**", "/voucher/**", "/shop-type/**", "blog/hot", "upload/**")
                .order(1);
    }
}
