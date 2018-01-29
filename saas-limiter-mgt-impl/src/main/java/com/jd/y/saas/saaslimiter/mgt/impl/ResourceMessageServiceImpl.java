package com.jd.y.saas.saaslimiter.mgt.impl;


import com.alibaba.fastjson.JSONObject;
import com.jd.y.saas.saaslimiter.bean.ResourceQpsMessage;
import com.jd.y.saas.saaslimiter.mgt.ResourceMessageService;
import com.jd.y.saas.saaslimiter.constant.MessageConstant;
import com.alibaba.fastjson.JSON;
import com.jd.y.saas.saaslimiter.util.RedisClient;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gaofei12
 * @Desc
 * @date 2018/1/17
 */
@Service
public class ResourceMessageServiceImpl implements ResourceMessageService {

    RedisClient redisClient = new RedisClient();

    public int put(String field, String message) {

        int value = 1;

        long setSuccess = 0;
        long subscriberCount = 0;

        try {
            JSONObject jsonMsg = JSON.parseObject(message);

            //hash表的name
            String key = jsonMsg.getString("type");
            setSuccess = redisClient.hSet(key,field,message);

            if(setSuccess == 1 || setSuccess ==0){
                //TODO 可以通过异步来实现
                subscriberCount = redisClient.publish(MessageConstant.MESSAGECHANEL,message);
            }

        } catch (Exception e) {
            e.printStackTrace();
            value = -1;
        }

        return value;
    }


    public int del(String field,String message) {

        int value = 1;

        try {
            long delSuccess = 0;
            JSONObject jsonMsg = JSON.parseObject(message);
            //hash表的name
            String key = jsonMsg.getString("type");

            delSuccess = redisClient.hDel(key,field);

            if(delSuccess > 0){

                ResourceQpsMessage msg = new ResourceQpsMessage();
                msg.setResourceName(field);
                msg.setType(key);
                msg.setOperate("del");//删除通知
                redisClient.publish(MessageConstant.MESSAGECHANEL, JSON.toJSONString(msg));
            }

        } catch (Exception e) {
            e.printStackTrace();
            value = -1;
        }

        return value;
    }

    /**
     * 通过Hash表的name, 获取所有数据
     *
     * @param key
     * @return
     */
    public Map<String, String> getAll(String key) {

        try {
            return redisClient.hGetAll(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<String, String>();
    }

    /**
     * 通过 和 域获取value
     *
     * @param key
     * @param field
     * @return
     */
    public String get(String key, String field) {

        try {
            return redisClient.hGet(key, field);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
