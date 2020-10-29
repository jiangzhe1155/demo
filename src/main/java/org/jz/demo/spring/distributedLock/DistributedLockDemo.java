package org.jz.demo.spring.distributedLock;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Service;

/**
 * @author jz
 * @date 2020/10/28
 */
@Service
public class DistributedLockDemo {

    @DistributedLock(spELs = {"'fix:lock'"})
    public void fixLockTest() {

    }

    @DistributedLock(spELs = {"#userId+':'+#type"})
    public void variableLock(String userId, Integer type) {

    }

    @DistributedLock(spELs = {"'fix:lock:'+#userId"})
    public void fixAndVariableLock(String userId) {

    }

    @DistributedLock(spELs = {"'nested:'+#user.id"})
    public void nestedLock(User user) {

    }

    @DistributedLock(spELs = {"#user1", "#user2"})
    public void mutiLock(String user1, String user2) {


    }

    @DistributedLock(spELs = {"#user1.id < #user2.id ? #user1.id : #user2.id", "#user2.id >= #user1.id ? #user2.id : #user1.id"})
    public void inOrderLock(User user1, User user2) {

    }

    @DistributedLock(spELs = {"'mutiNested:'+#user.another.id"})
    public void mutiNestedLock(User user) {

    }

    @Data
    @Accessors(chain = true)
    public static class User {
        String id;
        User another;
    }

}
