package com.atguigu.gmall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "2016101300674166";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQCJHwckm1jMK4tp1DuD09XjtjAna3vjNPAyPhLkMFFV2AuqCc5GMK3SgnlUkKNJiXkwLn562q9aNiCRW8g/EJqNd5T97+8sPatzXNgOU7bf00tgBbqjJZ5smhAQ0NrN/J/So+A288p/MLCoLg7vDcICb11tGTq+Pz2maq3kT9tX89fFs6AS7lYRwKDUiOYF1c1StVwgQWLQC/XYpqz9MVGQtunglQ1cm68+rA1FoziK47NSfFgs1IndAhECdEVzl78c2+yOqRjTrIblLzuEFQeilt51Mog3F165tlQNamFb2NW6p4T52kExM1M5Jkf3JKreVmgZhbOS44XgBZY5nOhdAgMBAAECggEBAIEI2cfxCBbZw0/LkFX1uomWoKn305Vz87TUBSMG9QM7U36ny5zxoiE+9r5FfeNtosVce1lRAUJ7PRqMrFvsXARUS7jLWN7hCb592DNjQ+xNAdlSiteMRxEbyZKJd93vpNarsAsGT8BxKUyaSyNaZv+znM9VtpnhGTrJmOoI2/he9hBnqxUR49vOubHa39DvNCWPuyeJ2IpudGa7e7n5m+w20siiVLXd4VR/3S21AOGE2+iSX33uVGTWR8a3eihkuRl3xvafwf85MPSydTJNDrasb5GJ+vEwSIe9YfjkcIVvSf0ZCawbYI/QHxvSC8+SGPyKngpvFCy9gFg77yK7sGECgYEAwfT6HZApSBjxV6dYZ5eJQqLkl1/MkadzBTz4YWW7DLGTQvjXOGevVcQQ381ZwV7Slyo9PN9WfNnQOpmPXi0a8KfyT2SVC4hQ9H6SR9hOAjq/HnMNqS5uJ0Cuck865lwWwpFZbdR4/xLLav1WrkeHzN0M48rbBN0AOYBLvfr44xMCgYEAtPvOBJEPzUJaxQGrBjPKaXWE/C3KD7zgAnxVQSzGpBZ8ScJcgqqo11O/d4idwMzeeoL6TK6AdyHEoXSeob0LZSltYZiRAsWMuAjA132s6lvf9a5JnSLZQu3ub/rL6s1qaIVPygQNKAmMaDvfYCnCn1BoH7MQ9/wMnwEyymcLBM8CgYBQYIwyKibhaOzDDWeWbncEdWrTzHCDP+hrmEu6WSU96m0DiQnvpxBDM6BmjrwOZZRR7sA39LnrvXwMfMysE3chmgfRnPYjNFeQKs/GFD6nr9656KYVoVcmzyg72Neo1SrdcMyltjJ6SjigWuJMEPqXDFgmIk/HYSqRqbg5v8LanwKBgQCDT8d0bMOoS6KMGd+6ik0sIwYv6hEXRhTJ+Ofqd9BQMbhP0+NHMwd549uhoM/EmWA6R1nP1TSEO03tTy3hb8YayeoAy6868ZZd2IwCTb7t82cVXDUw+53i/7rmKHNXk17HIyJ3EmVxTSxAXGB/5wy8hpxlk4iWRJvrqfTuAcFu5QKBgQCe3lZhNAoXLPBULzcC0fy45OBBop73gpbNF9EViOCwgzH4sm5Q/t2yyVxQkO9mi8x+2px5pdEeWYLf5MZmr+qkdQUCFa/mqwka2vM6gvT59PHr6Rs0L2AJbx2sYJNWf2eA4BZnDbeFpE4QJuXApfp8TkKnqA128mMA7HS1g0yF+Q==";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqdKUNmy3c3qa0wXaHp3/DBZsH/252C0pvlCxgibkmk1ApXvfL5JMCSYjosdD85owZlSlX1L+NQfGNLrA4izibo1k1SQgeALnZHHkguv5MMY4pa8L3IYz2FXDd2RNKL2jflL9IhlzfGR34ehN88JhJO4lf+VScDiqvz2OEep5+Z0DsPvfLFM1o7ZYN+HZkBNfgkVCrxO5Cm3APLtWaT0+9h17H9TSeAbox5wju1Ttgi5PJLCj5nOMXeVwnegLlX8GYVz0baioNygZEqvOpBQbzB6XGcZ1KcF4YGQ+VKO3OkqFVHq66Pma32meo1NWEhJ9pagNsFSVC3H4jVAwDe7DcwIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url="http://zcmuxq406t.52http.net/api/order/pay/success";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url=null;

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
