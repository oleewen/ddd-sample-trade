# 工程结构介绍

## 模块依赖关系

    --------------              --------------
    |            |              |            |
    |   client   |              |  present   |_ __ __ __ __ __ _
    |            |              |            |                  \
    --------------              --------------                   \
                \                 /        \                      \
                 \               /          \                      \
                  \             /            \                      \
                  _\|         |/_            _\|                     \
                  --------------               --------------         \     --------------
                  |            |               |            |          \_ _\|            |
                  |     api    |               | application|              /|  resource  |
                  |            |               |            |               |            |
                  --------------               --------------               --------------
                                                             \                  /
                                                              \                /
                                                               \              /
                                                               _\|          |/_
                                                                --------------
                                                                |            |
                                                                |   domain   |
                                                                |            |
                                                                --------------

## 各模块职责

- domain：领域服务层
    - 领域模型层：领域对象model、领域服务service、资源库repository、事件event、命令command
    - 查询处理器queryHandler
    - 代码结构如下
        ```
        - com.${company}.${department}.${business}.${appname}
        \- domain
          |- handler
          |- model
          |- service
          |- command
          |- event
          \- repository
        ```
- application：应用服务层
    - 面向用例或用户故事，实现处理流程、处理节点
    - 代码结构如下
        ```
        - com.${company}.${department}.${business}.${appname}
        |- flow
        \- action
        ```
- resource：资源层，实现数据访问
    - 含数据访问层dal、数据访问对象dao、数据库配置config、数据对象entity、数据映射mapper、数据对象&领域对象工厂
    - 代码结构如下
        ```
        - com.${company}.${department}.${business}.${appname}
        \- resource
          |- dal
          |- dao
          |- config
          |- entity
          |- mapper
          \- factory
        ```
- api：公共api包，含公共常量&通用定义，服务接口定义
    - 公共常量const、枚举enum、通用util类、异常类
    - RPC服务接口定义Service
    - 输入输出对象：Request、Response、DTO
    - 代码结构如下
        ```
        - com.${company}.${department}.${business}.${appname}
        |- common
        | |- consts
        | |- enums
        | |- utils
        | \_ exception
        \- api
          |- module
          | |- request
          | |- response
          | \_ dto
          \_ service
        ```
- client：实现富客户端
    - 富客户端
    - 代码结构如下
        ```
        - com.${company}.${department}.${business}.${appname}
        \_ client
        ```
- present：用户接口层，即表现层（present），实现表现层逻辑（协议、输入&输出转换）
    - 定义present层接口（HTTP协议、RPC协议）
    - 代码结构如下
        ```
        - com.${company}.${department}.${business}.${appname}
        |- present
          |- rpc
          | \- impl
          \- web
            |- controller
            |- config
            \- filter
        ```