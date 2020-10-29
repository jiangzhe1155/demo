package org.jz.demo.spring.distributedLock;


import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DistributedLockDemoTest {

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    DistributedLockDemo distributedLockDemo;

    @Test
    public void distributedLockTest() {
        //锁名称固定
        distributedLockDemo.fixLockTest();

        //锁的名称随着参数改变
        distributedLockDemo.variableLock("variableUser", 6);

        //固定前缀+变量
        distributedLockDemo.fixAndVariableLock("variableUser");

        //顺序加锁
        distributedLockDemo.mutiLock("user1", "user2");

        //对象加锁
        distributedLockDemo.nestedLock(new DistributedLockDemo.User().setId("userNested"));

        //根据 id 大小顺序加锁(支持字典排序和数字排序)
        distributedLockDemo.inOrderLock(
                new DistributedLockDemo.User().setId("orderB"),
                new DistributedLockDemo.User().setId("orderA"));

        //嵌套对象加锁
        distributedLockDemo.mutiNestedLock(new DistributedLockDemo.User().setAnother(new DistributedLockDemo.User().setId("mutiNested")));

        //可重入锁（不会重复释放锁）
        distributedLockDemo.mutiLock("user1", "user1");

        //测试加锁失败场景
        RLock lock = null;
        try {
            lock = redissonClient.getLock("userHasLock");
            lock.lock();
            Thread thread = new Thread(() -> distributedLockDemo.mutiLock("userNotHasLock", "userHasLock"));
            thread.start();
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

}