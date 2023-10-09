package com.mybilibili.service.util;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.CountDownLatch2;
import org.apache.rocketmq.common.message.Message;

import java.util.concurrent.TimeUnit;

public class RocketMQUtil {


    //发送方法：同步、异步
    //同步发送消息
    public static void syncSendMsg(DefaultMQProducer producer, Message msg) throws Exception{
        SendResult result = producer.send(msg);
        System.out.println(result);
    }

    //异步消息
    public static void asyncSendMsg(DefaultMQProducer producer,Message message) throws Exception{
        //计数器,发送两次
        int messageCount = 2;
        //倒计时计数
        CountDownLatch2 countDownLatch2 = new CountDownLatch2(messageCount);
        for(int i =0; i <messageCount; i++){
            producer.send(message, new SendCallback() {
                //回调，用于调节信息
                //发送成功的回调，失败的提醒
                @Override
                public void onSuccess(SendResult sendResult) {
                    //成功、计数器减一，输出结果消息
                    countDownLatch2.countDown();
                    System.out.println(sendResult.getMsgId());
                }

                @Override
                public void onException(Throwable throwable) {
                    //减一，输出异常相关消息
                    countDownLatch2.countDown();
                    System.out.println("发送消息时发生了异常" +throwable);
                    throwable.printStackTrace();
                }
            });
        }
        //计数器停留5秒
        countDownLatch2.await(5, TimeUnit.SECONDS);
    }
}
