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
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 刷新用户登录状态拦截器
 */
@Component
public class RefreshTokenInterceptor implements HandlerInterceptor {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    /**
     * 拦截器预处理方法
     *
     * @param request  请求对象
     * @param response 响应对象
     * @param handler  处理器
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1、获取请求头中的token
        String token = request.getHeader("authorization");
        if (StrUtil.isBlank(token)){
            return true;
        }
        //2、基于token获取redis中的用户
        String key = RedisConstants.LOGIN_USER_KEY + token;
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(key);
        //3、判断用户是否存在
        if (userMap.isEmpty()) {
            return true;
        }
        //5、将查询到的Hash数据转化为UserDTO对象
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
        //6、存在，保存用户信息到ThreadLocal
        UserHolder.saveUser(userDTO);
        //7、刷新token有效期
        stringRedisTemplate.expire(key, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
        //8、返回结果
        return true;
    }

    /**
     * 拦截器完成处理方法
     *
     * @param request  请求对象
     * @param response 响应对象
     * @param handler  处理器
     * @param ex       异常对象
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
        UserHolder.removeUser();
    }
}
