package com.jd.y.saas.saaslimiter.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.Map;

/**
 * @author gaofei12
 * @Desc
 * @date 2018/1/9
 */
public class RedisClient {

    //Redis服务器IP
    private static String ADDR = "192.168.156.96";

    //Redis的端口号
    private static int PORT = 6379;



    //可用连接实例的最大数目，默认值为8；
    private static int MAX_TOTAL = 512;

    //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值也是8。
    private static int MAX_IDLE = 50;

    //等待可用连接的最大时间，单位毫秒，默认值为-1，表示永不超时。
    private static int MAX_WAIT = 10000;

    private static int TIMEOUT = 10000;

    //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
    private static boolean TEST_ON_BORROW = true;

    private static JedisPool jedisPool = null;


    static
    {
        initialPool();
    }

    /**
     * 初始化Redis连接池
     */
    private static void initialPool()
    {
        // 池基本配置
        JedisPoolConfig config = new JedisPoolConfig();
//        config.setMaxActive(MAX_TOTAL);
        config.setMaxIdle(MAX_IDLE);
//        config.setMaxWait(MAX_WAIT);


        config.setTestOnBorrow(TEST_ON_BORROW);

        jedisPool = new JedisPool(config, ADDR, PORT, TIMEOUT);
    }


    /**
     * 获取Jedis实例
     * @return
     */
    public synchronized static Jedis getJedis() {
        try {
            if (jedisPool != null) {
                Jedis jedis = jedisPool.getResource();
                return jedis;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 释放jedis资源
     * @param jedis
     */
    public static void returnResource(final Jedis jedis) {
        try {
            if (jedis != null) {
                jedisPool.returnResource(jedis);
            }
        }catch (Exception e){
            e.printStackTrace();
            if(jedis.isConnected()){
                jedis.quit();
                jedis.disconnect();
            }
        }
    }

    public long incrBy(String key,int num) {
        Jedis jedis = null;
        try{
             jedis = this.getJedis();
             return jedis.incrBy(key, num);
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            RedisClient.returnResource(jedis);
        }
        return 0;
    }

    public long incr(String key) {
        Jedis jedis = null;
        try{
            jedis = this.getJedis();
            return jedis.incr(key);
        }catch(Exception e){
            e.printStackTrace();
            //释放redis对象
            jedisPool.returnBrokenResource(jedis);
        }finally{
            RedisClient.returnResource(jedis);
        }
        /**
         *
         * 默认值设置返回0,用于当redis服务异常时,不会影响到用户的正常访问
         */
        return 0;
    }

    /**
     *
     * @param key
     * @param num
     * @return 返回值 1表示执行成功; 0 表示执行失败
     */
    public long expire(String key,int num){

        Jedis jedis = null;
        try{
            jedis = this.getJedis();
            return jedis.expire(key,num);

        }catch(Exception e){
            e.printStackTrace();
            //释放redis对象
            jedisPool.returnBrokenResource(jedis);
        }finally {

            RedisClient.returnResource(jedis);
        }
        return 0;
    }

    /**********一对多 订阅发布操作 begin****************/
    /**
     * 发布消息
     * @param channel
     * @param message
     * @return 表示有N个订阅者订阅了这个频道的消息
     */
    public long publish(String channel,String message){

        Jedis jedis = null;
        try{
            jedis = this.getJedis();
            //返回值 1表示执行成功; 0 表示执行失败
            return jedis.publish(channel,message);
        }catch(Exception e){
            e.printStackTrace();
            //释放redis对象
            jedisPool.returnBrokenResource(jedis);
        }finally {

            RedisClient.returnResource(jedis);
        }
        return 0;
    }


    /**
     * 订阅消息
     * @param jedisPubSub
     * @param channels
     * @return
     */
    public void subscribe(JedisPubSub jedisPubSub, String... channels){

        Jedis jedis = null;
        try{
            jedis = this.getJedis();
            //返回值 1表示执行成功; 0 表示执行失败
            jedis.subscribe(jedisPubSub,channels);
        }catch(Exception e){
            e.printStackTrace();
            //释放redis对象
            jedisPool.returnBrokenResource(jedis);
        }finally {

            RedisClient.returnResource(jedis);
        }
    }
    /**********一对多 订阅发布操作 end****************/

    /**********Hash数据结构操作 begin****************/

    /**
     * 将哈希表 key 中的域 field 的值设为 value 。
     * 如果 key 不存在，一个新的哈希表被创建并进行 HSET 操作。
     * 如果域 field 已经存在于哈希表中，旧值将被覆盖。
     * @param key
     * @param field
     * @param value
     * @return
     *  如果 field 是哈希表中的一个新建域，并且值设置成功，返回 1 。
     *  如果哈希表中域 field 已经存在且旧值已被新值覆盖，返回 0 。
     */
    public long hSet(String key,String field,String value) throws Exception{

        Jedis jedis = null;
        try{
            jedis = this.getJedis();
            //返回值 1表示执行成功; 0 表示执行失败
            return jedis.hset(key,field,value);
        }catch(Exception e){
            e.printStackTrace();
            //释放redis对象
            jedisPool.returnBrokenResource(jedis);
            throw e;
        }finally {

            RedisClient.returnResource(jedis);
        }
    }

    /**
     * 获取哈希表 key 中的域 field 的值 。
     * 如果给定域不存在或是给定 key 不存在时，返回 null
     * @param key
     * @param field
     * @return
     */
    public String hGet(String key,String field) throws Exception{

        Jedis jedis = null;
        try{
            jedis = this.getJedis();
            return jedis.hget(key,field);
        }catch(Exception e){
            e.printStackTrace();
            //释放redis对象
            jedisPool.returnBrokenResource(jedis);
            throw e;
        }finally {

            RedisClient.returnResource(jedis);
        }
    }

    /**
     *
     * @param key
     * @return 若 key 不存在，返回空列表 。
     * @throws Exception
     */
    public Map<String, String> hGetAll(String key) throws Exception{

        Jedis jedis = null;
        try{
            jedis = this.getJedis();
            return jedis.hgetAll(key);
        }catch(Exception e){
            e.printStackTrace();
            //释放redis对象
            jedisPool.returnBrokenResource(jedis);
            throw e;
        }finally {

            RedisClient.returnResource(jedis);
        }
    }
    /**
     * 删除哈希表 key 中的一个或多个指定域，不存在的域将被忽略。
     * @param key
     * @param fields
     * @return 删除域的个数  返回值 >=1表示执行成功;
     */
    public long hDel(String key,String... fields) throws Exception{

        Jedis jedis = null;
        try{
            jedis = this.getJedis();

            return jedis.hdel(key,fields);

        }catch(Exception e){
            e.printStackTrace();
            //释放redis对象
            jedisPool.returnBrokenResource(jedis);
            throw e;
        }finally {

            RedisClient.returnResource(jedis);
        }
    }

    /**********Hash数据结构操作 end****************/

    public static void main(String[] args) {

        System.out.println(RedisClient.getJedis().expire("1",10));
        System.out.println(RedisClient.getJedis().hget("a","b"));
        Map<String,String> m = RedisClient.getJedis().hgetAll("a44");
        System.out.println(m.size());
    }
}
