package com.jd.y.saas.saaslimiter.mgt.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.RateLimiter;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * @author gaofei12
 * @Desc
 * @date 2018/1/23
 */
public class TransferMapUtil {

    /**
     * map转化
     * @param resourceMap
     * @param resourceThresholdMap
     */
    public static void transferMap(Map<String,String> resourceMap, ConcurrentMap<String,Long> resourceThresholdMap){

        Set<String> keySet = resourceMap.keySet();
        for (Iterator<String> it = keySet.iterator(); it.hasNext();){
            String uri = it.next();
            String jsonMessage = resourceMap.get(uri);
            if(!"".equals(jsonMessage) && null != jsonMessage ){
                JSONObject jsonObject = JSON.parseObject(jsonMessage);
                int isValid = jsonObject.getIntValue("isValid");
                long qps = jsonObject.getLongValue("qps");
                if(isValid == 1){
                    resourceThresholdMap.put(uri,qps);
                }
            }
        }
    }

    /**
     *
     * @param resourceMap
     * @param rateLimiterMap
     */
    public static void transferRateLimiterMap(Map<String,String> resourceMap, ConcurrentMap<String, RateLimiter> rateLimiterMap){

        Set<String> keySet = resourceMap.keySet();
        for (Iterator<String> it = keySet.iterator(); it.hasNext();){
            String uri = it.next();
            String jsonMessage = resourceMap.get(uri);
            if(!"".equals(jsonMessage) && null != jsonMessage ){
                JSONObject jsonObject = JSON.parseObject(jsonMessage);
                int isValid = jsonObject.getIntValue("isValid");
                long qps = jsonObject.getLongValue("qps");
                if(isValid == 1){
                    rateLimiterMap.put(uri,RateLimiter.create(qps));
                }
            }
        }
    }

}
