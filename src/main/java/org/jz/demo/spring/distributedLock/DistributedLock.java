package org.jz.demo.spring.distributedLock;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @author jz
 * @date 2020/03/14
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    String prefix() default "";

    @AliasFor("spELs")
    String[] value() default {};

    String[] spELs() default {};

    //等待时间
    int waitTime() default 5;

    //持有锁时间
    int leaseTime() default -1;

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    //是否公平锁
    boolean isFair() default false;

}
