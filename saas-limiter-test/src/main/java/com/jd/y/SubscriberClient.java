package com.jd.y;

import com.alibaba.fastjson.JSON;
import com.jd.y.saas.saaslimiter.ResourceLimiter;
import com.jd.y.saas.saaslimiter.bean.ResourceQpsMessage;
import com.jd.y.saas.saaslimiter.constant.MessageConstant;
import com.jd.y.saas.saaslimiter.impl.SinglePointAtomicLimiter;
import com.jd.y.saas.saaslimiter.mgt.ResourceMessageService;
import com.jd.y.saas.saaslimiter.mgt.handler.MessageSubHandler;
import com.jd.y.saas.saaslimiter.mgt.impl.ResourceMessageServiceImpl;
import com.jd.y.saas.saaslimiter.util.RedisClient;


/**
 * 非基于spring容器测试
 * @author gaofei12
 * @Desc
 * @date 2018/1/19
 */
public class SubscriberClient {

    static RedisClient redisClient = new RedisClient();

    static ResourceLimiter limiter = new SinglePointAtomicLimiter();

    public static void main(String []args){

        //1.订阅  同步数据
        new Thread(){
            public void run(){
                redisClient.subscribe(new MessageSubHandler(), MessageConstant.MESSAGECHANEL);
            }
        }.start();


        //2.访问  第一次：默认设置10 qps
        new Thread(){
            public void run(){
                testAutomicLimiter();
            }
        }.start();

        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //3.更新资源QPS
        ResourceMessageService service = new ResourceMessageServiceImpl();
        ResourceQpsMessage msg = new ResourceQpsMessage();
        msg.setIsValid(1);
        msg.setQps(2);
        msg.setResourceName("a");
        msg.setType(MessageConstant.SINGLE);
        String json = JSON.toJSONString(msg);

        //第二次：设置2 qps
        service.put("a",json);
        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //第三次：设置10 qps
        msg.setQps(10);
        service.put("a",JSON.toJSONString(msg));

        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //第四次：删除
        service.del("a",JSON.toJSONString(msg));



    }

    public static  void testAutomicLimiter(){
        String k = "a";
        limiter.updateResourceQps(k,10);

        while (true){
            int v = limiter.tryAcquire(k);
            if(v ==1){
                System.out.println("Success  " +System.currentTimeMillis()/1000);
            }else{
                System.out.println("Fail  " +System.currentTimeMillis()/1000);
            }
        }
    }
}
