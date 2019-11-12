package com.atguigu.gmall.auth;

import com.atguigu.core.utils.JwtUtils;
import com.atguigu.core.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class JwtTest {
    private static final String pubKeyPath = "D:\\tmp\\rsa.pub";

    private static final String priKeyPath = "D:\\tmp\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE1NzM1MzQ5Mjl9.A-n3mOtHBWwBSNXIw9C3gf73bv-Lkg1Z1O55o-HCJEmwuf0O_yMyUFyXMMkchy0sdeBvWmiwIs3rngg6D0vQR1XjoS6lNoMNY5Y-gVsH2aKZtQWiYSxnY_hLcqi8glOZVgnPxOhhlaBlw7d3ycPcSwLQsGllknEro5H5e3a4w9DE3HkBEzdkQrLyGBcuhx9nBVFQrkZ9CHHchwzzUH80p-jKCMHi4DyHY26xigRpXqv97gxOWFk0D0XqNpk19ud5MTvzcnVeaK4fSzPMI5qKAhh0C0745Fk4DUYkM2k9USlJMMEwXXSaFNNrGvxGEZX3P06LgWErsBePYKbtIct3wA";

        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}