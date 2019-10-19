package redis.connection;

import com.sun.xml.internal.ws.policy.AssertionSet;
import org.junit.Assert;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Created by DEll on 2019-10-19.
 */
public class RedisFactoryTest {

    @Test
    public void testGetJedis() throws Exception {
        Jedis jedisString = RedisFactory.getJedis();
        jedisString.flushDB();
        //获取的对象不为空
        Assert.assertNotNull(jedisString);
        //获取的值相等
        Assert.assertEquals("PONG",jedisString.ping());
        //存活链接数量判断
        Assert.assertEquals(1, RedisFactory.getNumActive());
        testStringType(jedisString);

        Jedis jedisHash = RedisFactory.getJedis();
//        testHashType(jedisHash);

        Jedis jedisList = RedisFactory.getJedis();
        testListType(jedisList);

        Jedis jedisSet = RedisFactory.getJedis();
        testSetType(jedisSet);

        Jedis jedisSortedSet = RedisFactory.getJedis();
        testSortedSet(jedisSortedSet);

        //关闭连接后判断存活连接数量
        Assert.assertEquals(5, RedisFactory.getNumActive());

        jedisSortedSet.close();
        jedisSet.close();
        jedisList.close();
        jedisHash.close();
        jedisString.close();
        RedisFactory.closePool();
    }

    private void testSortedSet(Jedis jedis){

    }

    private void testSetType(Jedis jedis){

    }

    private void testListType(Jedis jedis){
        jedis.lpush("listKey1", "firs", "second", "third");
        jedis.lset("listKey1", 0, "first");
        //左侧出栈
        Assert.assertEquals("first", jedis.lpop("listKey1"));
        //队列长度
        Assert.assertEquals(new Long(2), jedis.llen("listKey1"));
        //右侧入栈
        jedis.rpush("listKey1","forth");
        List<String> list = new ArrayList<>(1);
        list.add("forth");
        Assert.assertEquals(list, jedis.lrange("listKey1", -1, -1));


    }

    private void testHashType(Jedis jedis){
        //判断增加和获取
        jedis.hset("mapKey1", "key1", "value1");
        jedis.hset("mapKey1", "key2", "value2");
        Assert.assertEquals("value1", jedis.hget("mapKey1","key1"));
        Assert.assertEquals("value2", jedis.hget("mapKey1", "key2"));

        //map只能有一个key、value对，否则报hset的wrong number of arguments 错误
        Map<String, String> map = new HashMap<>(2);
        map.put("num1", "1");
//        map.put("num2", "2");
        jedis.hset("mapKey1", map);
        Assert.assertNotNull(jedis.hget("mapKey1", "num1"));

        //hgetAll返回的是某一个key下的所有key、value组成的map
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("key1", "value1");
        resultMap.put("key2", "value2");
        resultMap.put("num1", "1");
        Assert.assertEquals(resultMap, jedis.hgetAll("mapKey1"));

        //获取某一个key下的map长度
        jedis.hdel("mapKey1", "key2");
        Assert.assertTrue(!jedis.hexists("mapKey1", "key2"));
        Assert.assertEquals(new Long(2), jedis.hlen("mapKey1"));

        //设置存活时间
        jedis.expire("mapKey1", 10);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(!jedis.exists("mapKey1"));
    }

    private void testStringType(Jedis jedis){
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
        //判断批量获取结果是否正确
        jedis.set("key2", "value2");
        List<String> stringList = new ArrayList<>(2);
        stringList.add("value1_reset_append");
        stringList.add("value2");
        Assert.assertEquals(stringList, jedis.mget("key1", "key2"));
        //判断整数加减
        jedis.set("number", "0");
        Assert.assertEquals("1", String.valueOf(jedis.incr("number")));
        Assert.assertEquals("0", String.valueOf(jedis.decr("number")));

        //判断批量设置key
        if(jedis.exists("mKey1")) jedis.del("mKey1");
        if (jedis.exists("mKey2")) jedis.del("mKey2");
        Assert.assertEquals("OK", jedis.mset("mKey1", "mValue1", "mKey2", "mValue2"));
        Assert.assertEquals("OK", jedis.mset("mKey1", "mValue", "mKey2", "mValue2"));
        /*--------------------------String end--------------------------*/
    }
}