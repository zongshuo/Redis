package redis.connection;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by DEll on 2019-10-19.
 * 用于以链接池的方式配置redis资源，并获取有效的redis连接。
 * 类采用单例模式，资源池为单例，返回有效的非单例的redis连接
 */
public class RedisFactory {
    //redis资源池
    private static JedisPool pool = null;
    private RedisFactory(){};

    /**
     * 获取redis资源的方法，方法线程安全
     * @return Jedis
     */
    public static synchronized Jedis getJedis(){
        if(pool == null){
            buildRedisPool();
        }
        return pool.getResource();
    }

    public static int getNumActive(){
        return pool.getNumActive();
    }

    public static void closePool(){
        pool.close();
    }

    /**
     * 设置JedisPoolConfig参数值
     */
    private static void buildRedisPool(){
        JedisPoolConfig config = new JedisPoolConfig();

        //连接耗尽时是否阻塞，false报异常，true阻塞直到超时。默认true
        // TODO: 2019-10-19 超时时间如何设置？
        //config.setBlockWhenExhausted(true);
        
        //设置的逐出策略类名，默认DefaultEvictionPolicy(当连接超过最大空闲时间，或连接数超过最大空闲连接数)
        // TODO: 2019-10-19 其他还有什么类，类的作用是什么？
        //config.setEvictionPolicyClassName("org.apache.commons.pool2.impl.DefaultEvictionPolicy");
        
        //是否启用pool的jmx管理功能，默认是true
        // TODO: 2019-10-19 jmx是什么？
        //config.setJmxEnabled(true);
//        config.setJmxNamePrefix("pool");
        
        //是否启用后进先出，默认true
        // TODO: 2019-10-19 什么是后进先出？
//        config.setLifo(true);

        //最大空闲数,默认8个
        config.setMaxIdle(20);

        //最大连接数，默认8个
        config.setMaxTotal(100);

        //最大等待毫秒数
        config.setMaxWaitMillis(30000);

        //资源池中资源最小空闲时间（单位毫秒），到达此值后空闲资源将被移除，默认1800000毫秒（30分钟）
        config.setMinEvictableIdleTimeMillis(60000);

        //最小空闲连接数
        config.setMinIdle(0);

        //每次逐出检查时，逐出的最大数目，如果为负数就是1/abs（n），默认3
        // TODO: 2019-10-19 负数时如何计算？
//        config.setNumTestsPerEvictionRun(3);

        //对象空闲多久后逐出，当空暇时间>该值，且空闲连接>最大空闲数时直接逐出，不在根据MinEvictableIdleTimeMillis判断（默认逐出策略）
        config.setSoftMinEvictableIdleTimeMillis(1800000);

        //在获取连接时检查有效性，默认false
        config.setTestOnBorrow(false);

        //是否开启空闲资源检测，默认false
        // TODO: 2019-10-19 如何检测，如何返回结果？
        config.setTestWhileIdle(true);

        //空闲资源的检测周期（单位毫秒）,如果为负数，则不运行逐出线程，默认-1
//        config.setTimeBetweenEvictionRunsMillis(-1);

        //使用配置创建连接池
        pool = new JedisPool(config, "localhost");
    }
}
