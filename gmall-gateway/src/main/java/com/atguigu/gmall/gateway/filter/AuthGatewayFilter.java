package com.atguigu.gmall.gateway.filter;

import com.atguigu.core.utils.JwtUtils;
import com.atguigu.gmall.gateway.config.JwtProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.lang.annotation.Annotation;

/**
 * @author ZQ
 * @create 2019-11-12 19:05
 */
@Component
@EnableConfigurationProperties({JwtProperties.class})
public class AuthGatewayFilter implements GatewayFilter, Order {
    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        获取cookie中的token信息
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        if (cookies == null || !cookies.containsKey(jwtProperties.getCookieName())) {
//        判断是否存在，不存在重定向到登录界面
            response.setStatusCode(HttpStatus.UNAUTHORIZED);//设置响应状态码，结束请求
            return response.setComplete();
        }
        String jwtTocken = cookies.getFirst(jwtProperties.getCookieName()).getValue();
        try {
            JwtUtils.getInfoFromToken(jwtTocken, jwtProperties.getPublicKey());
        } catch (Exception e) {
            e.printStackTrace();
        }
//        存在解析试试
        return chain.filter(exchange);
    }

    @Override
    public int value() {
        return 0;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }
}
