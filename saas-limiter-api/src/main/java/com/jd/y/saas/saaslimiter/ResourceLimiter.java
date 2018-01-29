package com.jd.y.saas.saaslimiter;

/**
 * 限流接口定义
 * @author gaofei12
 * @Desc
 * @date 2018/1/16
 */
public interface ResourceLimiter {


    /**
     * 初始化资源的访问量
     */
    public void init();

    /**
     * 销毁资源的访问量
     */
    public void destory();

    /**
     * 更新资源的单位时间内的访问量
     *
     * @param resource URI
     * @param qps
     */
    public void updateResourceQps(String resource, long qps);


    /**
     * 移除对资源的限流
     * @param resource
     */
    public void removeResource(String resource);

    /**
     *
     * @param resource 请求URI
     * @return 返回值 1 表示可正常执行; 0 表示请求被限流
     */
    public  int tryAcquire(String resource);

}
