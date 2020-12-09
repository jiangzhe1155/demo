package org.jz.demo.spring.shardingJdbc;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * (CouponCfg)
 *
 * @author jz
 * @since 2020-11-06
 */
@Data
@TableName("t_coupon_cfg")
public class CouponCfg {

    @TableField("`id`")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("`name`")
    private String name;

    //抵扣金额
    @TableField("`deduce_amount`")
    private Integer deduceAmount;

}