## 需求场景
1. 在一些高并发场景下，我们可以使用分布式锁来**降低并发量**（当然还有很多其他方式来实现），防止大量请求同时打到数据库上。
2. 对于一些涉及资金或其他出现误差就会产生很大影响的业务，我们必须用分布式锁来进行有效的**资源控制**，防止某一资源同时被不同线程修改，出现不一致的情况。

## 功能点
1. 支持 SpEL 表达式，动态生成 key。
2. 支持可重入。
3. 支持简单的自定义规则加锁，能有效防止死锁。
4. 注解形式，添加到方法上。锁随着方法结束而释放。

## 使用示例
[实现代码](https://github.com/jiangzhe1155/demo/tree/master/src/main/java/org/jz/demo/spring/distributedLock)

[测试用例](https://github.com/jiangzhe1155/demo/tree/master/src/test/java/org/jz/demo/spring/distributedLock)
