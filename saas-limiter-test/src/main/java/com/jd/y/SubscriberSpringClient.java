package com.jd.y;

import com.alibaba.fastjson.JSON;
import com.jd.y.saas.saaslimiter.ResourceLimiter;
import com.jd.y.saas.saaslimiter.bean.ResourceQpsMessage;
import com.jd.y.saas.saaslimiter.constant.MessageConstant;
import com.jd.y.saas.saaslimiter.impl.SinglePointAtomicLimiter;
import com.jd.y.saas.saaslimiter.mgt.ResourceMessageService;
import com.jd.y.saas.saaslimiter.mgt.handler.MessageSubHandler;
import com.jd.y.saas.saaslimiter.util.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;


/**
 *
 * @author gaofei12
 * @Desc
 * @date 2018/1/19
 */
@Service
public class SubscriberSpringClient {

    static RedisClient redisClient = new RedisClient();

    @Autowired(required = true)
    @Qualifier("singlePointAtomicLimiter")
    public ResourceLimiter limiterr;

    @Autowired
    public ResourceMessageService service;



    public void exec(){
        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
                "/applicationContext.xml");

        limiterr = (SinglePointAtomicLimiter) appContext.getBean(SinglePointAtomicLimiter.class);

        //1.订阅  同步数据
        final MessageSubHandler mh = new MessageSubHandler();
//        mh.setSinglePointAtomicLimiter(limiterr);
        new Thread(){
            public void run(){
                redisClient.subscribe(mh, MessageConstant.MESSAGECHANEL);
            }
        }.start();



        //2.访问  第一次：默认设置10 qps
        new Thread(){
            public void run(){
                testAutomicLimiter(limiterr);
            }
        }.start();

        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        service = (ResourceMessageService) appContext.getBean(ResourceMessageService.class);

        //3.更新资源QPS
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

     public static void main(String []args) throws Exception{

         new SubscriberSpringClient().exec();

    }

    public static  void testAutomicLimiter(ResourceLimiter limiter){
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
