package redis.connection;

import com.sun.xml.internal.ws.policy.AssertionSet;
import org.junit.Assert;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import static org.junit.Assert.*;

/**
 * Created by DEll on 2019-10-19.
 */
public class RedisFactoryTest {

    @Test
    public void testGetJedis() throws Exception {
        Jedis jedis = RedisFactory.getJedis();
        //获取的对象不为空
        Assert.assertNotNull(jedis);
        //获取的值相等
        Assert.assertEquals("PONG",jedis.ping());
        //存活链接数量判断
        Assert.assertEquals(1, RedisFactory.getNumActive());
        /*--------------------------String start--------------------------*/
        //获取不存在的key，返回null
        Assert.assertNull(jedis.get("testFlushDBKey"));
        //每次测试前移除测试用键
        if(jedis.exists("key1")){
            jedis.del("key1");
        }
        //新增String类型数据成功
        Assert.assertEquals("OK", jedis.set("key1", "value1"));
        //获取String类型的结果
        Assert.assertEquals("value1", jedis.get("key1"));
        //重置value后结果应为重置后的结果
        jedis.set("key1", "value1_reset");
        Assert.assertEquals("value1_reset", jedis.get("key1"));
        //给特定key的String值执行append操作
        jedis.append("key1", "_append");
        Assert.assertEquals("value1_reset_append", jedis.get("key1"));
        //方法返回当前连接到服务器的所有客户端信息
        System.out.println(jedis.clientList());
        /*--------------------------String end--------------------------*/
        jedis.close();
        //关闭连接后判断存活连接数量
        Assert.assertEquals(0, RedisFactory.getNumActive());

        RedisFactory.closePool();
    }
}