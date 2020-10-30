package org.jz.demo.spring.distributedLock;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisTimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Stack;

/**
 * @author jz
 * @date 2020/03/14
 */
@Slf4j
@Aspect
@Component
public class DistributedLockAspect {

    @Autowired
    RedissonClient redissonClient;

    private LocalVariableTableParameterNameDiscoverer nameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

    //SpEL上下文
    private StandardEvaluationContext evaluationContext = new StandardEvaluationContext();

    //使用SpEL进行key的解析
    private ExpressionParser parser = new SpelExpressionParser();

    @Pointcut("@annotation(DistributedLock)")
    public void paramsLog() {
    }

    @Around("paramsLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {

        Method targetMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        DistributedLock commonLog = targetMethod.getAnnotation(DistributedLock.class);
        if (commonLog.spELs().length == 0) {
            return joinPoint.proceed();
        }

        Object[] args = joinPoint.getArgs();
        //获取被拦截方法参数名列表(使用Spring支持类库)
        String[] paraNameArr = nameDiscoverer.getParameterNames(targetMethod);

        //把方法参数放入SPEL上下文中
        if (paraNameArr != null && paraNameArr.length > 0) {
            for (int i = 0; i < paraNameArr.length; i++) {
                evaluationContext.setVariable(paraNameArr[i], args[i]);
            }
        }

        Stack<RLock> locks = new Stack<>();
        try {
            for (String key : commonLog.spELs()) {
                String name = parser.parseExpression(key).getValue(evaluationContext, String.class);
                RLock lock = commonLog.isFair() ? redissonClient.getLock(name) : redissonClient.getFairLock(name);
                if (lock.isHeldByCurrentThread()) {
                    log.info("分布式锁 key {} 已经被当前线程占有，跳过", lock.getName());
                    continue;
                }

                if (!lock.tryLock(commonLog.waitTime(), commonLog.leaseTime(), commonLog.timeUnit())) {
                    log.info("分布式锁 key {} 加锁失败", lock.getName());
                    throw new RedisTimeoutException();
                }

                locks.push(lock);
                log.info("分布式锁 key {} 加锁成功", lock.getName());
            }
            return joinPoint.proceed();
        } finally {
            while (!locks.empty()) {
                RLock lock = locks.pop();
                lock.unlock();
                log.info("解除分布式锁 key {}", lock.getName());
            }
        }
    }
}
