package com.jd.y.saas.saaslimiter.mgt;

import java.util.Map;

/**
 * 资源数据管理接口
 * @author gaofei12
 * @Desc
 * @date 2018/1/17
 */
public interface ResourceMessageService {

    /**
     * 创建或更新 资源URI的QPS
     *

     * @param field uri
     * @param message 资源uri的qps信息
     * @return 1: 表示执行成功;  -1: 表示执行失败
     */
    public int put(String field, String message);

    /**
     * 删除资源URI的QPS
     * @param field uri
     * @param message 资源uri的qps信息 & 删除资源的type
     * @return 1: 表示执行成功;  -1: 表示执行失败
     */
    public int del(String field,String message);

    /**
     * 通过Hash表的name, 获取所有数据
     * @param key
     * @return
     */
    public Map<String,String> getAll(String key);

    /**
     * 通过 和 域获取value
     * @param key
     * @param field
     * @return
     */
    public String get(String key,String field);








}
