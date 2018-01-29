package com.jd.y.saas.saaslimiter.impl;

import com.google.common.collect.Maps;
import com.jd.y.saas.saaslimiter.ResourceLimiter;
import com.jd.y.saas.saaslimiter.constant.MessageConstant;
import com.jd.y.saas.saaslimiter.mgt.ResourceMessageService;
import com.jd.y.saas.saaslimiter.mgt.handler.TransferMapUtil;
import com.jd.y.saas.saaslimiter.util.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 *  分布式限流
 *  基于redis 的incr 原子操作 & expire 过期操作 实现的 针对资源限流
 *  特点：非平滑限流.允许1秒中的第N毫秒并发qps个请求。
 *  缺点：不能很好地应对突发请求，即瞬间请求可能都被允许从而导致一些问题；
 * @author gaofei12
 * @Desc
 * @date 2018/1/16
 */
@Service
public class DistributeRedisLimiter implements ResourceLimiter {

    /**
     * 保存接口的访问量
     */
    private static ConcurrentMap<String,Long> resourceThresholdMap = Maps.newConcurrentMap();

    static RedisClient redisClient = new RedisClient();

    @Autowired
    private ResourceMessageService resourceMessageService;

    /**
     * 初始化资源的访问量
     */
    public void init() {
        /**
         * 1.调用远程服务获取资源数据
         * 2.cache到 本地内存：resourceThresholdMap
         */
        Map<String,String> resourceMap = resourceMessageService.getAll(MessageConstant.DISTRIBUTE);
        TransferMapUtil.transferMap(resourceMap,resourceThresholdMap);
    }



    /**
     * 销毁资源的访问量
     */
    public void destory() {
        resourceThresholdMap.clear();
    }

    /**
     * 更新资源的单位时间内的访问量
     *
     * @param resource URI
     * @param qps
     */
    public void updateResourceQps(String resource, long qps) {
        resourceThresholdMap.put(resource,qps);
    }

    /**
     * 移除对资源的限流
     *
     * @param resource
     */
    public void removeResource(String resource) {
        resourceThresholdMap.remove(resource);
    }

    /**
     * @param resource 请求URI
     * @return 返回值 1 表示可正常执行; 0 表示请求被限流
     */
    public int tryAcquire(String resource) {

        int defaultValue = 1;

        String time = String.valueOf(new Date().getTime() / 1000);

        //key上加时间戳,规避redis中key 清除失败而导致resource不可访问的隐患.带来的问题是key翻倍--1.key sharding；2.补偿机制清除无效key
        String key = resource + ":" + time;

        Long threshold = resourceThresholdMap.get(resource);

        if (threshold == null) {
            return defaultValue;
        }

        long currentValue = 0;
        currentValue = redisClient.incr(key);

        if (currentValue <= threshold) {
            if(currentValue == 1) {
                long t = redisClient.expire(key, 3);
                /**
                 * TODO 执行失败时、需要补偿机制放入队列循环进行清除无效Key. 清的逻辑：判断key是否存在,然后再进行expire
                 */
//              System.out.println(t);
            }
            return defaultValue;

        } else {
            defaultValue = -1;
        }
        return defaultValue;
    }
}
