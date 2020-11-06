package org.jz.demo.spring.deadlock;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jz
 * @date 2020/11/06
 */
@Data
@Accessors(chain = true)
@TableName("t_user_amount")
public class UserAmount {

    @TableId(type = IdType.AUTO)
    @TableField("`id`")
    private Integer id;

    @TableField("`user_id`")
    private Integer userId;

    @TableField("`amount`")
    private Integer amount;
}
