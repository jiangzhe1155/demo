package org.jz.demo.spring.couponBus;


import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest
class CouponBusDemoTest {

    @Autowired
    BaseCouponServiceImpl couponService;

    @Test
    public void couponBusTest() throws JsonProcessingException {

        Integer userId = 2;
        Integer couponCfgId = 1;

        // 发送红包
        Integer couponId = couponService.sendCoupon(userId, couponCfgId);

        //使用红包
        couponService.useCoupon(userId, couponId);

        //获取用户可用红包列表
        List<CouponAccount> couponAccounts = couponService.listUserValidCoupons(userId);

        log.info("当前用户可用红包列表 {}", couponAccounts);
    }

}