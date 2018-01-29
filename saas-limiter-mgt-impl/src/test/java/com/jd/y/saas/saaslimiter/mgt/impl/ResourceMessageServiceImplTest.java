package com.jd.y.saas.saaslimiter.mgt.impl;

import com.alibaba.fastjson.JSON;
import com.jd.y.saas.saaslimiter.bean.ResourceQpsMessage;
import com.jd.y.saas.saaslimiter.mgt.ResourceMessageService;
import com.jd.y.saas.saaslimiter.constant.MessageConstant;


/**
 * @author gaofei12
 * @Desc
 * @date 2018/1/19
 */
public class ResourceMessageServiceImplTest {

    @org.junit.Test
    public void put() {

        ResourceMessageService service = new ResourceMessageServiceImpl();
        ResourceQpsMessage msg = new ResourceQpsMessage();
        msg.setIsValid(1);
        msg.setQps(1);
        msg.setResourceName("/refreshRoute");
        msg.setType(MessageConstant.SINGLE);
        String json = JSON.toJSONString(msg);
        service.put("/refreshRoute",json);

    }

    @org.junit.Test
    public void del() {

        ResourceMessageService service = new ResourceMessageServiceImpl();
        ResourceQpsMessage msg = new ResourceQpsMessage();
        msg.setResourceName("a");
        msg.setType(MessageConstant.SINGLE);
        String json = JSON.toJSONString(msg);
        service.del("a",json);

    }
}