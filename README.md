# sc 介绍 #

`它是一个基于SpringCloud的分布式服务示例项目`

<pre>
使用组件列表：
注册中心：nacos，替代方案eureka、consul、zookeeper
配置中心: nacos ，替代方案sc config、consul config
调用:feign，替代方案：resttempate
熔断：sentinel、，替代方案：Resilience4j
熔断监控：sentinel dashboard
负载均衡:sc loadbalancer
网关：spring cloud gateway
链路：spring cloud sleuth+zipkin，替代方案：skywalking等
</pre>

#### 包含工程有：
* sc-consumer：client端，即consumer服务消费者。
* sc-openfeign：SpringCloud OpenFeign，声明的接口，采用的是Nacos作为注册中心。
* sc-provider：server端，即provider服务提供端，端口是随机分配的，可以启动多个服务对ribbon验证服务负载

#### 示例启动

* 按顺序依次启动项目工程： sc-provider、sc-consumer

* 访问 sc-consumer 测试验证
* 浏览器访问 [http://localhost:9999/diff/2](http://localhost:9999/diff/2)

[GitHub](https://github.com/tobeflyboy) [issues](https://github.com/tobeflyboy/sc/issues)
