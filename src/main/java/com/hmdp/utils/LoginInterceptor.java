package com.hmdp.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * 登录拦截器
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {



    /**
     * 拦截器预处理方法
     *
     * @param request  请求对象
     * @param response 响应对象
     * @param handler  处理器
     * @return 如果返回true，表示继续处理请求；如果返回false，表示拦截请求
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1、判断是否需要拦截
        if (UserHolder.getUser() == null) {
            response.setStatus(401);
            return false;
        }
        return true;
    }

    /**
     * 拦截器完成处理方法
     *
     * @param request  请求对象
     * @param response 响应对象
     * @param handler  处理器
     * @param ex       异常对象
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
        UserHolder.removeUser();
    }
}
