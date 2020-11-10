package org.jz.demo.spring.couponBus;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * (CouponAccount)
 *
 * @author jz
 * @since 2020-11-06
 */
@Data
@Accessors(chain = true)
@TableName("t_coupon_account")
public class CouponAccount {

    @TableField("`id`")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("`user_id`")
    private Integer userId;

    @TableField("`coupon_cfg`")
    private String couponCfg;

    //使用状态，0-未使用，1-使用中，2-已使用。
    @TableField("`status`")
    private Integer status;

}