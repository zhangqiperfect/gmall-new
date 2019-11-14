package com.atguigu.gmall.ums.listener;

import com.atguigu.gmall.ums.feign.GmallMsmClient;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author ZQ
 * @create 2019-11-14 16:26
 */
@Component
public class UmsListener {

    @Autowired
    private  GmallMsmClient gmallMsmClient;

    /**
     * 处理insert的消息
     *
     * @param
     * @throws Exception
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "gmall.msm.create.queue", durable = "true"),
            exchange = @Exchange(
                    value = "GMALL-CODE-EXCHANGE",
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC),
            key = {"code."}))
    public void listenCreate(String phoneNo) throws Exception {
        this.gmallMsmClient.sendCode(phoneNo);
    }
}
