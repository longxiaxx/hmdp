package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;
import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发送手机验证码
     */
    @Override
    public Result sendCode(String phone, HttpSession session) {
        //1、校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            //2、不符合返回错误信息
            return Result.fail("手机号格式无效");
        }
        //3、符合，生成验证码
        String code = RandomUtil.randomNumbers(6);
        //4、保存验证码到redis
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);
        //5、模拟发送验证码
        log.info("验证码：{}，手机号：{}", code, phone);
        //6、返回结果
        return Result.ok();
    }

    /**
     * 登录功能
     */
    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        //1、校验手机号
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            //2、不符合返回错误信息
            return Result.fail("手机号格式无效");
        }
        //3、从redis校验验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.equals(code)) {
            //4、不一致，返回错误信息
            return Result.fail("验证码无效");
        }
        //5、一致，根据手机号查询用户
        User user = query().eq("phone", phone).one();
        //6、判断用户是否存在
        if (user == null) {
            //7、不存在，创建用户
            user = createNewUser(phone);
        }

        //8、保存用户信息到redis
        //8.1 生成token
        String token = UUID.randomUUID().toString(true);
        //8.2 创建UserDTO转为HashMap
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        //8.3 存储到redis
        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        //8.4 设置过期时间
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);

        return Result.ok(token);

    }

    private User createNewUser(String phone) {
        //1、创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + phone);
        //2、保存用户
        save(user);
        return user;
    }
}
