package org.jz.demo.spring.shardingJdbc;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jz
 * @date 2020/11/04
 * 基础红包服务
 */
@Slf4j
@Service
public class BaseCouponServiceImpl {

    private Map<Integer, Integer> map = new ConcurrentHashMap<>();

    @Autowired
    CouponAccountMapper couponAccountMapper;

    @Autowired
    CouponCfgMapper couponCfgMapper;

    @Autowired
    ObjectMapper objectMapper;

    // 获取用户可用红包
    public List<CouponAccount> listUserValidCoupons(Integer userId) {
        List<CouponAccount> couponAccounts = couponAccountMapper
                .selectList(new LambdaQueryWrapper<CouponAccount>()
                        .eq(userId != null, CouponAccount::getUserId, userId));
        return couponAccounts;
    }

    // 发送红包
    public Integer sendCoupon(Integer userId, Integer couponCfgId) throws JsonProcessingException {
        // 插入一条领取红包记录
        CouponCfg couponCfg = couponCfgMapper.selectById(couponCfgId);
        if (couponCfg == null) {
            return null;
        }
        couponCfg.setDeduceAmount(couponCfg.getDeduceAmount() + 1);
        couponCfgMapper.updateById(couponCfg);

        CouponAccount couponAccount =
                new CouponAccount().setUserId(userId).setStatus(0).setCouponCfg(objectMapper.writeValueAsString(couponCfg));
        int success = couponAccountMapper.insert(couponAccount);
        if (success <= 0) {
            return null;
        }

        return couponAccount.getId();
    }

    // 使用红包
    public boolean useCoupon(Integer userId, Integer couponId) {

        if (userId == null) {
            // 通过缓存找到couponId对应的userId
            userId = map.get(couponId);
        }

        // 此时有两种情况: 1.缓存中查不到userId，只能依次遍历所有分区表进行修改。
        //                2.查到了userId，通过sharding-jdbc路由到对应表进行修改。
        int success = couponAccountMapper.update(null, new LambdaUpdateWrapper<CouponAccount>()
                .set(CouponAccount::getStatus, 2)
                .eq(userId != null, CouponAccount::getUserId, userId)
                .eq(CouponAccount::getId, couponId));

        return success > 0;
    }

}
