package com.jd.y.saas.saaslimiter.impl;

import com.jd.y.saas.saaslimiter.ResourceLimiter;
import org.junit.Test;

/**
 * @author gaofei12
 * @Desc
 * @date 2018/1/19
 */
public class SinglePointAtomicLimiterTest {

    @Test
    public void tryAcquire() {

        {
            ResourceLimiter limiter = new SinglePointAtomicLimiter();
            String k = "a";
            limiter.updateResourceQps(k,10);
            int count =0;
            while (true){
                int v = limiter.tryAcquire(k);
                if(v ==1){
                    System.out.println("Success");
                }else{
                    System.out.println("Fail");
                }
                if(count++ == 100){
                    break;
                }
            }

        }
    }
}