# transformer
transformer-公共工具集

***

# 版本更新记录
### 1.2.0.RELEASE:
>统一 dubbo-filer、断言工具、业务异常定义、状态码

***
# 主要模块

## transformer-common
基础常量、异常状态码

## transformer-util
常用工具 

提供DateHelper、 JsonHelper、BeanHelper等编码常用工具。
[详细说明]()

## transformer-exception
通用的异常模块

通用的断言工具、异常处理工具。兼容titans的 BusinessException。

提供者：曹楷

## transformer-mq
通用的MQ消模块  

提供公共的 Producer和Consumer模块。 简化业务编码中对 消息体序列化与反序列化、重试、日志 等重复工作量。

提供者：陶威

## transformer-dubbo
通用的dubbo开发辅助模块

目前包含有推荐的dubboFilter 可配置后直接应用

## transformer-download
导出组件

基于导出中心，提供简化的导出中心接入套件。  

提供者：董兵

## transformer-log
通用的Log消模块
 
提供简化的日志和异常监控工具。


## transformer-dao
数据数据库存储层辅助工具

提供数据库访问有关的工具，如 检查慢SQl检查，高危SQL的MyBatis插件



## transformer-es
通用的ES辅助模块

提供简化的日志和异常监控工具。


