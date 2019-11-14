package com.atguigu.gmall.cart.interceptor;

import com.atguigu.core.utils.CookieUtils;
import com.atguigu.core.utils.JwtUtils;
import com.atguigu.gmall.cart.config.JwtProperties;
import com.atguigu.gmall.cart.vo.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

/**
 * @author ZQ
 * @create 2019-11-13 11:56
 */
@EnableConfigurationProperties(JwtProperties.class)
@Component
public class LoginInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    private JwtProperties jwtProperties;
    private static ThreadLocal<UserInfo> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfo userInfo = new UserInfo();
        //获取cookie信息
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        String userKey = CookieUtils.getCookieValue(request, jwtProperties.getUserKeyName());
//      判断userkeys是否为空
        if (StringUtils.isEmpty(userKey)) {
            userKey = UUID.randomUUID().toString();
            CookieUtils.setCookie(request, response, jwtProperties.getUserKeyName(), userKey, jwtProperties.getExpire());
        }
//        不管有没有登录都要设置userKey
        userInfo.setUserKey(userKey);
//        如果Gamll_token不为空，进行解析
        if (!StringUtils.isEmpty(token)) {
            Map<String, Object> userInfoMap = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            if (!CollectionUtils.isEmpty(userInfoMap)) {
                userInfo.setUserId(  Long.valueOf(userInfoMap.get("id").toString()));
            }
        }
        threadLocal.set(userInfo);
        return true;
    }

    public static UserInfo getUserInfo() {
        return threadLocal.get();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //以为使用的是tmocat线程池，请求结束不代表线程结束，线程存在则ThreadLocal对象保存的userInfo中
        threadLocal.remove();
}
}
