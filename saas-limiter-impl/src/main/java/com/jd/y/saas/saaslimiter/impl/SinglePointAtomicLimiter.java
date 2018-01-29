package com.jd.y.saas.saaslimiter.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.jd.y.saas.saaslimiter.ResourceLimiter;
import com.jd.y.saas.saaslimiter.constant.MessageConstant;
import com.jd.y.saas.saaslimiter.mgt.ResourceMessageService;
import com.jd.y.saas.saaslimiter.mgt.handler.TransferMapUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 *  单点限流
 *  基于 AtomicLong 的原子操作,失效机制：每次进行缓存操作判断是否失效。实时计数CAS。
 *  特点：非平滑限流.允许1秒中的第N毫秒并发qps个请求。
 *  缺点：不能很好地应对突发请求，即瞬间请求可能都被允许从而导致一些问题；
 *
 * @author gaofei12
 * @Desc
 * @date 2018/1/16
 */
@Service
public class SinglePointAtomicLimiter implements ResourceLimiter {

    /**
     * 最大容量
     */
    private static final int DEFAULT_MAX_CAPACITY = 10000;

    /**
     * 缓存项在给定时间内没有被写访问（创建或覆盖），则回收。如果认为缓存数据总是在固定时候后变得陈旧不可用，这种回收方式是可取的。
     */
    private static final LoadingCache<String, AtomicLong> counter  = CacheBuilder.newBuilder().maximumSize(DEFAULT_MAX_CAPACITY).expireAfterWrite(1, TimeUnit.SECONDS).build(new CacheLoader<String, AtomicLong>() {
        @Override
        public AtomicLong load(String key) throws Exception {
            return new AtomicLong(0);
        }
    });

    /**
     * 保存接口的访问量
     */
    private static ConcurrentMap<String,Long> resourceThresholdMap = Maps.newConcurrentMap();

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
        Map<String,String> resourceMap = resourceMessageService.getAll(MessageConstant.SINGLE);
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

        Long threshold = resourceThresholdMap.get(resource);
        if (threshold == null) {
            return defaultValue;
        }

        long currentValue = 0;
        try {
            currentValue = counter.get(resource).incrementAndGet();
        } catch (ExecutionException e) {
            e.printStackTrace();
            return defaultValue;
        }

        if(currentValue > threshold){//超阈值
            // 测试时放开注释
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            defaultValue = 0;
        }
        return defaultValue;
    }
}
