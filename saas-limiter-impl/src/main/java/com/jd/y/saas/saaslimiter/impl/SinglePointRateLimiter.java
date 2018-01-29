package com.jd.y.saas.saaslimiter.impl;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.RateLimiter;
import com.jd.y.saas.saaslimiter.ResourceLimiter;
import com.jd.y.saas.saaslimiter.constant.MessageConstant;
import com.jd.y.saas.saaslimiter.mgt.ResourceMessageService;
import com.jd.y.saas.saaslimiter.mgt.handler.TransferMapUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * 单点限流
 * 基于guava 的RateLimiter 实现限流 。漏桶原理。特点：同步平滑限流.把1秒切分成qps段,每隔1/qps 秒允许一个请求。
 *
 * @author gaofei12
 * @Desc
 * @date 2018/1/16
 */
@Service
public class SinglePointRateLimiter implements ResourceLimiter {

    private static final ConcurrentMap<String, RateLimiter> resourceLimiterMap = Maps.newConcurrentMap();

    @Autowired
    private ResourceMessageService resourceMessageService;
    /**
     * 初始化资源的访问量
     */
    public void init() {
        /**
         * TODO
         * 1.调用远程服务获取资源数据
         * 2.cache到 本地内存：resourceThresholdMap
         */
        Map<String,String> resourceMap = resourceMessageService.getAll(MessageConstant.DISTRIBUTE);
        TransferMapUtil.transferRateLimiterMap(resourceMap,resourceLimiterMap);
    }

    /**
     * 销毁资源的访问量
     */
    public void destory() {
        resourceLimiterMap.clear();
    }

    /**
     * 更新资源的单位时间内的访问量
     *
     * @param resource URI
     * @param qps
     */
    public void updateResourceQps(String resource, long qps) {

        RateLimiter limiter = resourceLimiterMap.get(resource);
        if (limiter == null) {
            limiter = RateLimiter.create(qps);
            RateLimiter putByOtherThread  = resourceLimiterMap.putIfAbsent(resource, limiter);
            if (putByOtherThread != null) {
                limiter = putByOtherThread;
            }
        }
        limiter.setRate(qps);
    }

    /**
     * 移除对资源的限流
     *
     * @param resource
     */
    public void removeResource(String resource) {

        resourceLimiterMap.remove(resource);
    }

    /**
     * @param resource 请求URI
     * @return 返回值 1 表示可正常执行; 0 表示请求被限流
     */
    public int tryAcquire(String resource) {

        int defaultValue = 1;
        RateLimiter limiter = resourceLimiterMap.get(resource);
        if (limiter == null) {
            return defaultValue;
        }

        if (limiter.tryAcquire()) {
            System.out.println("Success ");
            return defaultValue;
        }else{
            System.out.println("Fail ");
            defaultValue = 0;
        }

        return defaultValue;
    }


}
