package com.mybilibili.service.config;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mybilibili.domain.UserFollowing;
import com.mybilibili.domain.UserMoment;
import com.mybilibili.domain.constant.UserMomentsConstant;
import com.mybilibili.service.UserFollowingService;
import com.mybilibili.service.websocket.WebSocketService;
import io.netty.util.internal.StringUtil;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//@Configuration
public class RocketMQConfig {

    @Value("${rocketmq.name.server.address}")
    private String nameServerAddr;


    @Autowired
    private RedisTemplate <String,String> redisTemplate;

    @Resource
    @Autowired
    private UserFollowingService userFollowingService;

    //生产者
    @Bean("momentsProducer")
    public DefaultMQProducer momentsProducer() throws Exception{
        //生成生产者实例
        DefaultMQProducer producer = new DefaultMQProducer(UserMomentsConstant.GROUP_MOMENTS);
        //设置工作地址 网址加端口
        producer.setNamesrvAddr(nameServerAddr);
        //启动
        producer.start();
        //返回实例
        return producer;
    }

    @Bean("momentsConsumer")
    public DefaultMQPushConsumer momentsConsumer() throws  Exception{
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(UserMomentsConstant.GROUP_MOMENTS);
        consumer.setNamesrvAddr(nameServerAddr);
        //订阅的主题
        consumer.subscribe(UserMomentsConstant.TOPIC_MOMENTS,"*");
        //注册信息监听器
        consumer.registerMessageListener(new MessageListenerConcurrently() {
          @Override
          //处理结果  参数：MessageExt消息类型的扩充类，当消息使用  ConsumeConcurrentlyContext处理的上下文
          public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
          //获取第一个message
              MessageExt msg = msgs.get(0);
           if(msg == null){
               return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
           }
           //消息体获取
           String bodyStr = new String(msg.getBody());
           //获取用户动态，  JSONObject类型转换
              UserMoment userMoment = JSONObject.toJavaObject(JSONObject.parseObject(bodyStr),UserMoment.class);
              Long userId = userMoment.getUserId();
              //获取关注列表
              List<UserFollowing > fanList = userFollowingService.getUserFans(userId);
              for(UserFollowing fan : fanList){
                  //redis的key设置
                  String key = "subscribed-" + fan.getUserId();
                  //获取该用户订阅的对象，对象类型为string
                  String subscribedListStr = redisTemplate.opsForValue().get(key);
                  //用户动态列表
                  List<UserMoment> subscribedList;
                  //为空生成新的空列表
                  if(StringUtil.isNullOrEmpty(subscribedListStr)){
                      subscribedList = new ArrayList<>();
                  }else{
                      //非空则转换成对应列表
                      subscribedList = JSONArray.parseArray(subscribedListStr,UserMoment.class);
                  }
                  //用户动态添加到列表中
                  subscribedList.add(userMoment);

                  redisTemplate.opsForValue().set(key,JSONObject.toJSONString(subscribedList));
              }
              return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
          }
        });
        //启动
        consumer.start();
        return consumer;
    }


    @Bean("danmusProducer")
    public DefaultMQProducer danmusProducer() throws Exception{
        // 实例化消息生产者Producer
        DefaultMQProducer producer = new DefaultMQProducer(UserMomentsConstant.GROUP_DANMUS);
        // 设置NameServer的地址
        producer.setNamesrvAddr(nameServerAddr);
        // 启动Producer实例
        producer.start();
        return producer;
    }

    @Bean("danmusConsumer")
    public DefaultMQPushConsumer danmusConsumer() throws Exception{
        // 实例化消费者
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(UserMomentsConstant.GROUP_DANMUS);
        // 设置NameServer的地址
        consumer.setNamesrvAddr(nameServerAddr);
        // 订阅一个或者多个Topic，以及Tag来过滤需要消费的消息
        consumer.subscribe(UserMomentsConstant.TOPIC_DANMUS, "*");
        // 注册回调实现类来处理从broker拉取回来的消息
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                MessageExt msg = msgs.get(0);
                byte[] msgByte = msg.getBody();
                String bodyStr = new String(msgByte);
                JSONObject jsonObject = JSONObject.parseObject(bodyStr);
                String sessionId = jsonObject.getString("sessionId");
                String message = jsonObject.getString("message");
                WebSocketService webSocketService = WebSocketService.WEBSOCKET_MAP.get(sessionId);
                if(webSocketService.getSession().isOpen()){
                    try {
                        webSocketService.sendMessage(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // 标记该消息已经被成功消费
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        // 启动消费者实例
        consumer.start();
        return consumer;
    }

}
