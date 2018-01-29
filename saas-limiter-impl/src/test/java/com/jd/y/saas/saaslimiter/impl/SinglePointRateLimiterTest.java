package com.jd.y.saas.saaslimiter.impl;

import com.jd.y.saas.saaslimiter.ResourceLimiter;
import org.junit.Test;

/**
 * @author gaofei12
 * @Desc
 * @date 2018/1/19
 */
public class SinglePointRateLimiterTest {

    @Test
    public void tryAcquire() {
        ResourceLimiter limiter = new SinglePointRateLimiter();
        String k = "a";
        limiter.updateResourceQps(k,1000);
        int count = 0;
        while (true){
            int v = limiter.tryAcquire(k);
            if(v ==1){
                System.out.println("Success "+count++);
            }else{
                System.out.println("Fail "+count++);
            }
            if(count == 100){
                break;
            }
        }

    }
}