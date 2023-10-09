package com.mybilibili.service.websocket;


import com.alibaba.fastjson.JSONObject;
import com.mybilibili.domain.Danmu;
import com.mybilibili.domain.constant.UserMomentsConstant;
import com.mybilibili.service.DanmuService;
import com.mybilibili.service.util.RocketMQUtil;
import com.mybilibili.service.util.TokenUtil;
import io.netty.util.internal.StringUtil;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import org.apache.rocketmq.common.message.Message;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
//找到api的路径
@ServerEndpoint("/imserver/{token}")
public class WebSocketService {
    //日志记录
    private final Logger logger =  LoggerFactory.getLogger(this.getClass());
    //当前连接人数，原子类保证线程安全
    private static final AtomicInteger ONLINE_COUNT = new AtomicInteger(0);
    //保存每个客户端的WebSocketService
    public static final ConcurrentHashMap<String, WebSocketService> WEBSOCKET_MAP = new ConcurrentHashMap<>();
    //用于长链接通信
    private Session session;
    //唯一标识
    private String sessionId;

    private Long userId;

    //所有webservice共用的上下文文件,用于多例模式下的bean注入
    private static ApplicationContext APPLICATION_CONTEXT;

    public static void setApplicationContext(ApplicationContext applicationContext){
        WebSocketService.APPLICATION_CONTEXT = applicationContext;
    }

    //连接建立好后打开连接  @PathParam 获得链接后的名称，获取token来得到userId,用于后续的使用
    @OnOpen
    public void openConnection(Session session, @PathParam("token") String token){
        try{
            //尝试获取，防止游客模式下不能获取报错
            this.userId = TokenUtil.verifyToken(token);
        }catch (Exception ignored){}
        //获取sessionid
        this.sessionId = session.getId();
        this.session = session;
        //判断是否在map中存在
        if(WEBSOCKET_MAP.containsKey(sessionId)){
            //存在则删除后新增
            WEBSOCKET_MAP.remove(sessionId);
            WEBSOCKET_MAP.put(sessionId, this);
        }else{
            //没有则直接添加
            WEBSOCKET_MAP.put(sessionId, this);
            //在线人数加一
            ONLINE_COUNT.getAndIncrement();
        }
        logger.info("用户连接成功：" + sessionId + "，当前在线人数为：" + ONLINE_COUNT.get());
        try{
            //返回通知，成功 状态码0
            this.sendMessage("0");
        }catch (Exception e){
            //失败，返回错误
            logger.error("连接异常");
        }
    }


    //关闭 ：情况：服务端突然断开，正常的刷新和退出页面
    @OnClose
    public void closeConnection(){
        if(WEBSOCKET_MAP.containsKey(sessionId)){
            WEBSOCKET_MAP.remove(sessionId);
            ONLINE_COUNT.getAndDecrement();
        }
        logger.info("用户退出：" + sessionId + "当前在线人数为：" + ONLINE_COUNT.get());
    }

    //消息处理
    @OnMessage
    public void onMessage(String message){
        logger.info("用户信息：" + sessionId + "，报文：" + message);
        //判断message是否合法
        if(!StringUtil.isNullOrEmpty(message)){
            //非空
            try{
                //群发消息
                for(Map.Entry<String, WebSocketService> entry : WEBSOCKET_MAP.entrySet()){
                    //获取webservice
                    WebSocketService webSocketService = entry.getValue();
                    //优化：将消息放入mq中
                    DefaultMQProducer danmusProducer = (DefaultMQProducer)APPLICATION_CONTEXT.getBean("danmusProducer");
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("message", message);
                    jsonObject.put("sessionId", webSocketService.getSessionId());
                    Message msg = new Message(UserMomentsConstant.TOPIC_DANMUS, jsonObject.toJSONString().getBytes(StandardCharsets.UTF_8));
                    RocketMQUtil.asyncSendMsg(danmusProducer, msg);
                }
                if(this.userId != null){
                    //保存弹幕到数据库
                    Danmu danmu = JSONObject.parseObject(message, Danmu.class);
                    danmu.setUserId(userId);
                    danmu.setCreateTime(new Date());
                    //获取DanmuService
                    DanmuService danmuService = (DanmuService)APPLICATION_CONTEXT.getBean("danmuService");
                    danmuService.asyncAddDanmu(danmu);
                    //保存弹幕到redis
                    danmuService.addDanmusToRedis(danmu);
                }
            }catch (Exception e){
                logger.error("弹幕接收出现问题");
                e.printStackTrace();
            }
        }
    }
    //错误处理
    @OnError
    public void onError(Throwable error){
    }

    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }


    //在线人数统计
    //或直接指定时间间隔，例如：5秒
    @Scheduled(fixedRate=5000)
    private void noticeOnlineCount() throws IOException {
        for(Map.Entry<String, WebSocketService> entry : WebSocketService.WEBSOCKET_MAP.entrySet()){
            //取出
            WebSocketService webSocketService = entry.getValue();
            //判断
            if(webSocketService.session.isOpen()){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("onlineCount", ONLINE_COUNT.get());
                jsonObject.put("msg", "当前在线人数为" + ONLINE_COUNT.get());
                webSocketService.sendMessage(jsonObject.toJSONString());
            }
        }
    }


    public Session getSession() {
        return session;
    }

    public String getSessionId() {
        return sessionId;
    }



}
