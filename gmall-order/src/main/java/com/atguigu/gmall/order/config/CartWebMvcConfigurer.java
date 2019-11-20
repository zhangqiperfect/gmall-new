package com.atguigu.gmall.order.config;


import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author ZQ
 * @create 2019-11-13 16:43
 */
@Configuration
public class CartWebMvcConfigurer implements WebMvcConfigurer {
    @Autowired
    private LoginInterceptor loginInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //.excludePathPatterns("/order/pay/success");排除拦截路径
        registry.addInterceptor(loginInterceptor).addPathPatterns("/**").excludePathPatterns("/order/pay/success");
    }
}
