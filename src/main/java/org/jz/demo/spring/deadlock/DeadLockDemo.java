package org.jz.demo.spring.deadlock;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author jz
 * @date 2020/10/23
 */
public class DeadLockDemo {

    @Data
    @Accessors(chain = true)
    @TableName("t_user_amount")
    public static class UserAmount {

        @TableId(type = IdType.AUTO)
        @TableField("`id`")
        private Integer id;

        @TableField("`user_id`")
        private Integer userId;

        @TableField("`amount`")
        private Integer amount;
    }

    @Slf4j
    @Service
    public static class UserAmountServiceImpl extends ServiceImpl<UserAmountMapper, UserAmount> {

        @Async
        @Transactional(rollbackFor = Exception.class)
        public void updateAmountAndInsert(Integer inviter, Integer invitee) throws InterruptedException {
            log.info("当前线程 {}\t事务id {}", Thread.currentThread().getName(),
                    TransactionSynchronizationManager.getCurrentTransactionName());

            //给邀请人发放金币奖励
            update(new LambdaUpdateWrapper<UserAmount>()
                    .eq(UserAmount::getUserId, inviter)
                    .setSql("`amount` = `amount` + 1"));

            //休眠五秒，强制触发死锁
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

    @Mapper
    private interface UserAmountMapper extends BaseMapper<UserAmount> {

    }


}
