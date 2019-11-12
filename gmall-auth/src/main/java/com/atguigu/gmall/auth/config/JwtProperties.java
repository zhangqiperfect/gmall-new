package com.atguigu.gmall.auth.config;

import com.atguigu.core.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @author ZQ
 * @create 2019-11-12 16:23
 */
@Slf4j
@ConfigurationProperties(prefix = "auth.jwt")
@Data
public class JwtProperties {
    /* auth:
     jwt:
     pubKeyPath: D:\\tmp\\rsa.pub
     priKeyPath: D:\\tmp\\rsa.pub
     secret: zhangqikdkkkkk
     expire: 30 # 时间单位为分钟
     cookieName: Gmall_Token*/
    private String pubKeyPath;
    private String priKeyPath;
    private String secret;
    private Integer expire;
    private String cookieName;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct//在构造器之后执行
    public void init() {
        try {
            //初始化文件
            File pulickey = new File(pubKeyPath);
            File prikey = new File(priKeyPath);
            if (!pulickey.exists() || !prikey.exists()) {
                RsaUtils.generateKey(pubKeyPath, priKeyPath, secret);
            }
            //获取已存在的公私钥
            publicKey = RsaUtils.getPublicKey(pubKeyPath);
            privateKey = RsaUtils.getPrivateKey(priKeyPath);
        } catch (Exception e) {
            log.error("初始化公钥和私钥错误");
            e.printStackTrace();
        }

    }
}
