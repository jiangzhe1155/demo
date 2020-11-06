package org.jz.demo.spring.deadlock;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;

@ActiveProfiles("dev")
@SpringBootTest
class DeadLockDemoTest {

    @Autowired
    UserAmountServiceImpl userAmountService;

    @Test
    public void deadLockTest() throws InterruptedException {

        //初始化
        userAmountService.remove(null);
        userAmountService.saveBatch(Arrays.asList(
                new UserAmount().setUserId(5).setAmount(5),
                new UserAmount().setUserId(10).setAmount(5),
                new UserAmount().setUserId(15).setAmount(5),
                new UserAmount().setUserId(20).setAmount(5),
                new UserAmount().setUserId(25).setAmount(5)));


        // 邀请人10 通过 被邀请人7 得到奖励
        userAmountService.updateAmountAndInsert(10, 7);
        Thread.sleep(2000);
        // 邀请人10 通过 被邀请人23 得到奖励
        userAmountService.updateAmountAndInsert(10, 23);
        Thread.sleep(20000);
    }
}