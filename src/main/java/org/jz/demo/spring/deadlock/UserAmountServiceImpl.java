package org.jz.demo.spring.deadlock;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jz
 * @date 2020/10/23
 */
@Slf4j
@Service
public class UserAmountServiceImpl extends ServiceImpl<UserAmountMapper, UserAmount> {

    @Async
    @Transactional(rollbackFor = Exception.class)
    public void updateAmountAndInsert(Integer inviter, Integer invitee) throws InterruptedException {
        log.info("当前线程 {}", Thread.currentThread().getName());

        //给邀请人发放金币奖励
        update(new LambdaUpdateWrapper<UserAmount>()
                .eq(UserAmount::getUserId, inviter)
                .setSql("`amount` = `amount` + 1"));

        //休眠十秒，强制触发死锁
        Thread.sleep(10000);

        // 同时给被邀请人发放奖励
        UserAmount inviteeAmount = getOne(new LambdaQueryWrapper<UserAmount>().eq(UserAmount::getUserId, invitee));
        if (inviteeAmount == null) {
            save(new UserAmount().setUserId(invitee).setAmount(1));
        } else {
            update(new LambdaUpdateWrapper<UserAmount>()
                    .eq(UserAmount::getUserId, invitee)
                    .setSql("`amount` = `amount` + 1"));
        }
    }
}





