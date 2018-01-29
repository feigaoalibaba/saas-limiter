package com.jd.y.saas.saaslimiter.mgt.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jd.y.saas.saaslimiter.ResourceLimiter;
import com.jd.y.saas.saaslimiter.constant.MessageConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPubSub;

/**
 * 消息订阅者 处理类
 * @author gaofei12
 * @Desc
 * @date 2018/1/17
 */
@Component
public class MessageSubHandler extends JedisPubSub {

    @Autowired
    @Qualifier("singlePointAtomicLimiter")
    private ResourceLimiter singlePointAtomicLimiter;

    @Autowired
    @Qualifier("distributeRedisLimiter")
    private ResourceLimiter distributeRedisLimiter;


    public MessageSubHandler(){
        super();
    }

    public MessageSubHandler(ResourceLimiter singlePointAtomicLimiter) {
        this.singlePointAtomicLimiter = singlePointAtomicLimiter;
    }

    public ResourceLimiter getSinglePointAtomicLimiter() {
        return singlePointAtomicLimiter;
    }

    public void setSinglePointAtomicLimiter(ResourceLimiter singlePointAtomicLimiter) {
        this.singlePointAtomicLimiter = singlePointAtomicLimiter;
    }

    public ResourceLimiter getDistributeRedisLimiter() {
        return distributeRedisLimiter;
    }

    public void setDistributeRedisLimiter(ResourceLimiter distributeRedisLimiter) {
        this.distributeRedisLimiter = distributeRedisLimiter;
    }


    @Override
    public void onMessage(String channel, String message) {

        try {
            if (MessageConstant.MESSAGECHANEL.equals(channel)) {

                System.out.println("+++++++++++++"+message);
                /**
                 * 1. 获取 message 解析；
                 * 2. 更新订阅者的本地内存；
                 */
                JSONObject jsonMsg = JSON.parseObject(message);
                String resourceName = jsonMsg.getString("resourceName");
                int isValid = jsonMsg.getIntValue("isValid");
                long qps = jsonMsg.getLongValue("qps");
                String type = jsonMsg.getString("type");
                String operate = jsonMsg.getString("operate");

                if(MessageConstant.SINGLE.equals(type)){//单点限流更新数据

                    if("del".equals(operate)){
                        singlePointAtomicLimiter.removeResource(resourceName);
                        return;
                    }
                    if(isValid == 1){//有效 - 更新操作
                        singlePointAtomicLimiter.updateResourceQps(resourceName,qps);
                    }else if(isValid ==0){//无效 -删除操作
                        singlePointAtomicLimiter.removeResource(resourceName);
                    }

                }else if(MessageConstant.DISTRIBUTE.equals(type)){//全局分布式更新数据

                    if("del".equals(operate)){
                        singlePointAtomicLimiter.removeResource(resourceName);
                        return;
                    }

                    if(isValid == 1){//有效 - 更新操作
                        distributeRedisLimiter.updateResourceQps(resourceName,qps);
                    }else if(isValid ==0 || "del".equals(operate)){//无效 -删除操作
                        distributeRedisLimiter.removeResource(resourceName);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
