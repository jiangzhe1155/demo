package org.jz.demo.spring.deadlock;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author jz
 * @date 2020/11/06
 */
@Mapper
public interface UserAmountMapper extends BaseMapper<UserAmount> {

}
