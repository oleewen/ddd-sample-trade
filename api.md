# API设计参考规范

# HTTP接口设计参考规范

## 接口设计原则

### 请求设计原则

- 请求 = 动词 + 宾语
    - 动词：采用标准HTTP方法，对应CRUD操作
    - 宾语：必须是名词，优先采用复数，偶有例外
    - 过滤：支持过滤参数，支持分页offset/limit
    - 聚合：第一个名词为聚合根，名词路径代表聚合关系
    - 角色：角色 + 请求 = 业务场景，Customer + /items VS Merchant + /items
- URI = http(s)://{域名}/{业务域}/{上下文}/{聚合根}/({聚合})/{动作}
    - POST [https://www.youzan.com/commodity/goods/items](https://www.youzan.com/commodity/goods/v1/items)
    - POST [https://www.youzan.com/commodity/goods/items/publish](https://www.youzan.com/commodity/goods/v1/items/publish)
    - GET [https://www.youzan.com/commodity/goods/items/skus](https://www.youzan.com/commodity/goods/v1/items/skus)
    - GET [https://www.youzan.com/commodity/category/categories](https://www.youzan.com/commodity/category/v1/categories)

### 响应设计原则

- 采用HTTP状态码
    - 每次请求，都有响应，响应包括HTTP状态码和数据两部分
- 状态码，共100余种，均有约定解释，行业通用
    - 1XX：相关信息（API不需要）
    - 2XX：操作成功
    - 3XX：请求重定向
    - 4XX：客户端错误
    - 5XX：服务器错误
- 响应出错，要有error返回错误信息

## 接口设计规范

### 操作标准
|操作|含义|说明|
|---|---|---|
|POST|创建/Create| 同一请求多次调用产生完全相同的副作用，即幂等性|
|GET|读取/Read | 调用多次不产生副作用（不是每次结果相同）可设置条件或范围过滤|
|PUT|更新/Update| 用作整体更新对象全部信息|
|PATCH|更新/Update| 用作部分更新对象部分信息|
|DELETE|删除/Delete| 删除记录，有立即删除和非立即删除

### API规范
一个商品（item）对有多个标准库存单元（SKU），如下图
![img](https://qima.feishu.cn/space/api/box/stream/download/asynccode/?code=NTAyNjkxYzcwMmU1NDc3MDI2MDZiNTAxM2Y4NzM4ODdfcnU2WGpYQlA5TGFUSEYxdjNvYWZCSUxsTmtYcUQ4cmJfVG9rZW46Ym94Y24yUWRxblI0ejVOQVpVR2Q5U0tOTGtnXzE2NDk0NzQ5NDY6MTY0OTQ3ODU0Nl9WNA)

#### 接口规范
|操作|接口|语义|
|---|---|---|
|POST| /items| 新建一个商品|
|POST| /items/skus?item_id=:id |新建一个SKU
|POST| /items/publish?item_id=:id | 发布一个商品 
|POST| /shop/follow?shop_id=:id | 关注该店铺 |
|GET| /items| 列出所有商品（支持多维度过滤） |
|GET|/items?item_id=:id |获取某个指定的商品|
|GET| /items?item_ids=:id,:id |获取某些指定的商品|
|GET| /items/skus?item_id=:id |获取某个指定商品的sku列表|
|GET| /items/skus?item_id=:id&sku_id=:sku_id |获取某个指定商品的指定sku |
|PUT|/items?item_id=:id | 更新某个指定商品的信息 |
|PATCH|/items?item_id=:id | 更新某个指定商品的部分信息 
|DELETE| /items?item_id=:id |删除某个指定的商品 
|DELETE| /item/disable?item_id=:id |禁用某个指定的商品
|DELETE| /items/skus?item_id=:id&sku_id=:sku_id |删除某个指定商品的指定sku

#### 常见过滤
|参数|含义|举例|
|---|---|---|
|offset |开始位置| offset=10
|limit| 返回记录数 |limit=10
|page| 指定第几页 |page=2 
|sortby| 按哪个属性排序 |sortby=name
|order| 排序顺序（升序/降序） |order=asc/desc
|start_time |开始时间 |start_time=2020-12-27 00:00:00 
|end_time| 结束时间 |start_time=2020-12-31 00:00:00

# RPC接口设计参考规范
## 接口设计原则
### 请求设计原则
- 请求 = 宾语 + 动词
    - 宾语：必须是名词，优先采用单数
- 动词：采用标准方法create/get|list/save/remove，对应CRUD操作
- 过滤：支持过滤参数，支持分页offset/limit
- 聚合：第一个名词为聚合根，名词路径代表聚合关系
- 角色：角色 + 请求 = 业务场景，Customer + ItemService.get VS Merchant + ItemService.get
- 签名 = com.company.{业务域}.{上下文}.{聚合根}.api.{聚合}Service.{动作}
    - com.youzan.commodity.goods.items.api.ItemService.get
    - com.youzan.commodity.goods.items.api.ItemService.publish
    - com.youzan.commodity.goods.items.api.ItemSKUService.get
    - com.youzan.commodity.category.categories.api.CategoryService.get
    - com.youzan.commodity.goodsmanagement.items.api.ItemService.get
    - com.youzan.commodity.goodsmanagement.items.api.ItemSKUService.get
    - com.youzan.commodity.goodsmanagement.categories.api.CategoryService.get
    - com.youzan.commodity.goodsmanagement.categories.api.CategoryAttributeService.get

### 响应设计原则
- 沿用HTTP状态码
    - 每次请求，都有响应，响应包括状态码和数据两部分
- 状态码，共100余种，均有约定解释，行业通用
    - 1XX：相关信息（API不需要）
    - 2XX：操作成功
    - 3XX：请求重定向
    - 4XX：客户端错误
    - 5XX：服务器错误
- 响应出错，要有error返回错误信息

## 接口设计规范
### 操作标准
|操作|含义|说明|
|---|---|---|
|create|创建/Create |同一请求多次调用产生完全相同的副作用，即幂等性|
|get/list|读取/Read| 调用多次不产生副作用（不是每次结果相同）可设置条件或范围过滤|
|save|更新/Update |用作整体更新对象全部信息|
|remove|删除/Delete |删除记录，有立即删除和非立即删除

### API规范
一个商品（item）对有多个标准库存单元（SKU），如下图
![img](https://qima.feishu.cn/space/api/box/stream/download/asynccode/?code=OGE4NGJmODI0YzQwNGFhZjc1ZjljYWMwZmU1OTFmMjFfTFZjOWhlaXUzTmVKdVhqYVhDR0dOYVlnclB1OGJUMEpfVG9rZW46Ym94Y25oc01BeHRyWGh5cmJWRnRvZFRhSlhnXzE2NDk0NzQ5NDY6MTY0OTQ3ODU0Nl9WNA)

#### 接口规范
|操作|接口|语义|
|---|---|---|
|create|ItemService.create(ItemDTO) |新建一个商品 
|create|ItemSKUService.create(SKUDTO)| 新建一个SKU |
|publish|ItemService.publish(:id) |发布一个商品 |
|follow|ShopService.follow(:id) |关注该店铺
|list |ItemService.list |列出所有商品（支持多维度过滤）
|get |ItemService.get(:id) |获取某个指定的商品
|search |ItemService.search(<:id>)| 获取某些指定的商品
|list |ItemSKUService.list |获取某个指定商品的sku列表
|get |ItemSKUService.get(:id)| 获取某个指定商品的指定sku
|save|ItemService.save(:id) |更新某个指定商品的信息
|remove |ItemService.remove(:id)| 删除某个指定的商品
|disable|ItemService.disable(:id) |禁用某个指定的商品
|remove|ItemSKUService.remove(:item_id,:sku_id) |删除某个指定商品的指定sku

#### 常见过滤
|参数|含义|举例|
|---|---|---|
|offset |开始位置| offset=10
|limit| 返回记录数 |limit=10
|page| 指定第几页 |page=2
|sortby| 按哪个属性排序 |sortby=name
|order| 排序顺序（升序/降序） |order=asc/desc
|start_time |开始时间 |start_time=2020-12-27 00:00:00
|end_time| 结束时间 |start_time=2020-12-31 00:00:00

# 参考资料
- https://restfulapi.cn/

# 命名参考规范
## 变量命名参考

| **名称**     | **命名**        | **业务语义**   |
| ------------ | --------------- | -------------- |
| 用户标识     | userId/uid      | 用户id         |
| 用户名       | userName        | 用户名称       |
| 帐号         | account         | 帐号           |
| 手机号       | phoneNumber     | 手机号码       |
| 地址         | address         | 地址           |
| 邮箱         | email           | 邮箱           |
| 位置         | location        | 地理位置       |
| 经纬度       | longAndLatitude | 经纬度         |
| 会员资格     | memberBenefit   | 会员权益       |
| 会员         | member          | 会员           |
| 等级         | memberLevel     | 会员等级       |
| 成长规则     | growthRules     | 成长规则       |
| 店铺标识     | shopId          | 店铺ID         |
| 门店         | offlineShop     | 线下店铺       |
| 网店         | onlineShop      | 线上店铺       |
| 货架         | shelves         | 货架           |
| 组织         | orgId           | 组织ID         |
| 总部         | hqShop          | 连锁总店       |
| 区域         | regionShop      | 连锁区域店铺   |
| 合伙人       | partnerShop     | 连锁合伙人店铺 |
| 员工         | staff           | 员工           |
| 权限         | permission      | 权限           |
| 店铺能力     | shopAbility     | 店铺能力       |
| 销售渠道     | channelId       | 售卖渠道ID     |
| 商品标识     | goodsId         | 商品ID         |
| 商品规格标识 | skuId           | 商品规格ID     |
| 商品分组     | goodsGroup      | 商品分组       |
| 类目         | category        | 商品类目       |
| 属性         | property        | 类目属性       |
| 属性值       | propertyValue   | 类目属性值     |
| 产品标识     | spuId           | 产品ID         |
| 价格         | price           | 商品价格       |
| 库存         | stockQuantity   | 库存数量       |
| 活动         | activityId      | 活动ID         |
| 优惠         | promotions      | 优惠活动       |
| 权益         | benefit         | 权益           |
| 优惠券       | couponId        | 优惠券ID       |
| 人群         | crowd           | 人群           |
| 门槛         | threshold       | 门槛           |
| 订单号       | orderNo         | 交易订单号     |
| 订单         | order           | 交易订单       |
| 物流         | logisticsMode   | 物流方式       |
| 支付单       | payRecord       | 支付单据       |
| 退款单       | refundRecord    | 退款单据       |

## 函数命名参考

| **名称** | **命名** | **业务语义**                                           |
| -------- | -------- | ------------------------------------------------------ |
| 单个查询 | get/find | 单个查询，只返回关键属性                               |
| 多个查询 | mget     | 多个查询                                               |
| 单个详情 | detail   | 单个详情，支持查询选项                                 |
| 列表查询 | list     | 根据简单条件匹配查询列表（查询条件固定），支持查询选项 |
| 搜索     | search   | 搜索匹配查询                                           |
| 计数     | count    | 计数                                                   |

#### 返回真伪值的方法

| **单词** | **意义**                         | **例**        |
| -------- | -------------------------------- | ------------- |
| is       | 对象是否符合期待的状态           | isValid       |
| can      | 对象能否执行所期待的动作         | canRemove     |
| should   | 应不应该调用方执行某个命令或方法 | shouldMigrate |
| has      | 对象是否持有所期待的数据和属性   | hasObservers  |
| needs    | 调用方是否需要执行某个命令或方法 | needsMigrate  |

#### 用来检查的方法

| **单词** | **意义**                                             | **例**         |
| -------- | ---------------------------------------------------- | -------------- |
| ensure   | 检查是否为期待的状态，不是则抛出异常或返回error code | ensureCapacity |
| validate | 检查是否为正确的状态，不是则抛出异常或返回error code | validateInputs |

#### 按需求才执行的方法

| **单词**  | **意义**                                  | **例**                 |
| --------- | ----------------------------------------- | ---------------------- |
| IfNeeded  | 需要的时候执行，不需要的时候什么都不做    | drawIfNeeded           |
| might     | 同上                                      | mightCreate            |
| try       | 尝试执行，失败时抛出异常或是返回errorcode | tryCreate              |
| OrDefault | 尝试执行，失败时返回默认值                | getOrDefault           |
| OrElse    | 尝试执行、失败时返回实际参数中指定的值    | getOrElse              |
| force     | 强制尝试执行。error抛出异常或是返回值     | forceCreate, forceStop |

#### 异步相关方法

| **单词**     | **意义**                                     | **例**                |
| ------------ | -------------------------------------------- | --------------------- |
| blocking     | 线程阻塞方法                                 | blockingGetUser       |
| InBackground | 执行在后台的线程                             | doInBackground        |
| Async        | 异步方法                                     | sendAsync             |
| Sync         | 对应已有异步方法的同步方法                   | sendSync              |
| schedule     | Job和Task放入队列                            | schedule, scheduleJob |
| post         | 同上                                         | postJob               |
| execute      | 执行异步方法（注：我一般拿这个做同步方法名） | execute, executeTask  |
| start        | 同上                                         | start, startJob       |
| cancel       | 停止异步方法                                 | cancel, cancelJob     |
| stop         | 同上                                         | stop, stopJob         |

#### 回调方法

| **单词** | **意义**                   | **例**       |
| -------- | -------------------------- | ------------ |
| on       | 事件发生时执行             | onCompleted  |
| before   | 事件发生前执行             | beforeUpdate |
| pre      | 同上                       | preUpdate    |
| will     | 同上                       | willUpdate   |
| after    | 事件发生后执行             | afterUpdate  |
| post     | 同上                       | postUpdate   |
| did      | 同上                       | didUpdate    |
| should   | 确认事件是否可以发生时执行 | shouldUpdate |

#### 操作对象生命周期的方法

| **单词**   | **意义**                       | **例**          |
| ---------- | ------------------------------ | --------------- |
| initialize | 初始化。也可作为延迟初始化使用 | initialize      |
| pause      | 暂停                           | onPause ，pause |
| stop       | 停止                           | onStop，stop    |
| abandon    | 销毁的替代                     | abandon         |
| destroy    | 同上                           | destroy         |
| dispose    | 同上                           | dispose         |

####  与集合操作相关的方法

| **单词** | **意义**                     | **例**     |
| -------- | ---------------------------- | ---------- |
| contains | 是否持有与指定对象相同的对象 | contains   |
| add      | 添加                         | addJob     |
| append   | 添加                         | appendJob  |
| insert   | 插入到下标n                  | insertJob  |
| put      | 添加与key对应的元素          | putJob     |
| remove   | 移除元素                     | removeJob  |
| enqueue  | 添加到队列的最末位           | enqueueJob |
| dequeue  | 从队列中头部取出并移除       | dequeueJob |
| push     | 添加到栈头                   | pushJob    |
| pop      | 从栈头取出并移除             | popJob     |
| peek     | 从栈头取出但不移除           | peekJob    |
| find     | 寻找符合条件的某物           | findById   |

#### 与数据相关的方法

| **单词** | **意义**                               | **例**        |
| -------- | -------------------------------------- | ------------- |
| create   | 新创建                                 | createAccount |
| new      | 新创建                                 | newAccount    |
| from     | 从既有的某物新建，或是从其他的数据新建 | fromConfig    |
| to       | 转换                                   | toString      |
| update   | 更新既有某物                           | updateAccount |
| load     | 读取                                   | loadAccount   |
| fetch    | 远程读取                               | fetchAccount  |
| delete   | 删除                                   | deleteAccount |
| remove   | 删除                                   | removeAccount |
| save     | 保存                                   | saveAccount   |
| store    | 保存                                   | storeAccount  |
| commit   | 保存                                   | commitChange  |
| apply    | 保存或应用                             | applyChange   |
| clear    | 清除数据或是恢复到初始状态             | clearAll      |
| reset    | 清除数据或是恢复到初始状态             | resetAll      |

#### 成对出现的动词

| **单词** **意义** |                   |
| ----------------- | ----------------- |
| get获取           | set 设置          |
| add 增加          | remove 删除       |
| create 创建       | destory 移除      |
| start 启动        | stop 停止         |
| open 打开         | close 关闭        |
| read 读取         | write 写入        |
| load 载入         | save 保存         |
| create 创建       | destroy 销毁      |
| begin 开始        | end 结束          |
| backup 备份       | restore 恢复      |
| import 导入       | export 导出       |
| split 分割        | merge 合并        |
| inject 注入       | extract 提取      |
| attach 附着       | detach 脱离       |
| bind 绑定         | separate 分离     |
| view 查看         | browse 浏览       |
| edit 编辑         | modify 修改       |
| select 选取       | mark 标记         |
| copy 复制         | paste 粘贴        |
| undo 撤销         | redo 重做         |
| insert 插入       | delete 移除       |
| add 加入          | append 添加       |
| clean 清理        | clear 清除        |
| index 索引        | sort 排序         |
| find 查找         | search 搜索       |
| increase 增加     | decrease 减少     |
| play 播放         | pause 暂停        |
| launch 启动       | run 运行          |
| compile 编译      | execute 执行      |
| debug 调试        | trace 跟踪        |
| observe 观察      | listen 监听       |
| build 构建        | publish 发布      |
| input 输入        | output 输出       |
| encode 编码       | decode 解码       |
| encrypt 加密      | decrypt 解密      |
| compress 压缩     | decompress 解压缩 |
| pack 打包         | unpack 解包       |
| parse 解析        | emit 生成         |
| connect 连接      | disconnect 断开   |
| send 发送         | receive 接收      |
| download 下载     | upload 上传       |
| refresh 刷新      | synchronize 同步  |
| update 更新       | revert 复原       |
| lock 锁定         | unlock 解锁       |
| check out 签出    | check in 签入     |
| submit 提交       | commit 交付       |
| push 推           | pull 拉           |
| expand 展开       | collapse 折叠     |
| begin 起始        | end 结束          |
| start 开始        | finish 完成       |
| enter 进入        | exit 退出         |
| abort 放弃        | quit 离开         |
| obsolete 废弃     | depreciate 废旧   |
| collect 收集      | aggregate 聚集    |

## 类命名参考

### 领域设计相关

| **名称**               | **命名**                                                     | **业务语义**                                                 |                                                   |
| ---------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------- |
| 领域层                 | 实体，聚合根                                                 | Entity为后缀，xxxEntity？直接用XXX命名，xxx？                | OrderEntiy?ShoppingCartItem?                      |
| 值对象                 | Value为后缀，xxxValue?直接用XXX命名，xxx?                    |                                                              |                                                   |
| 领域事件               | xxxEvent                                                     | OrderCreateEvent                                             |                                                   |
| 领域服务               | DomainService为后缀，xxxDomainService？直接用XXXService命名，依靠包路径区分，xxxService? |                                                              |                                                   |
| 仓库                   | xxxRepository                                                |                                                              |                                                   |
| 工厂                   | xxxFactory                                                   |                                                              |                                                   |
| 应用层                 | 应用服务                                                     | ApplicationService为后缀，xxxApplicationService？AppService为后缀，xxxAppService？直接用XXXService命名，依靠包路径区分，xxxService? |                                                   |
| 服务入参               | Command为后缀：xxxCommand                                    |                                                              |                                                   |
| 服务出参               | 不统一规范？                                                 |                                                              |                                                   |
| 适配层-持久化          | 持久化接口                                                   | DAO为后缀：xxxDAOMapper为后缀：xxxMapper?                    |                                                   |
| 持久化对象             | DO为后缀：xxxDO?PO为后缀：xxxPO?                             |                                                              |                                                   |
| 适配层-对外服务        | 对外PRC服务                                                  | xxxService富客户端：xxxClient                                |                                                   |
| 入参                   | 写操作：xxxRequest读操作：xxxRequest？读操作：xxxQueryParam? |                                                              |                                                   |
| 出参                   | xxxDTO对前端的服务：xxxVO？对前端的服务：展示对象:xxxActivityVO，Activity为业务活动名？ |                                                              |                                                   |
| 对外服务出参封装result | 直接follow有赞api定义，不要自己定义？：通用：com.youzan.api.common.response.PlainResult分页：PageResult（有赞api也有定义，看了下感觉实现得不好） |                                                              |                                                   |
|                        |                                                              |                                                              |                                                   |
| 适配层-调用外部服务    | 适配外部服务                                                 | xxxFacade？xxxService？xxxProxyService?                      | 交易适配商品服务：GoodsService?GoodsProxyService? |
| 转换器                 | 服务层转换类                                                 | DTO<->实体：xxxDTOConvertorVO<->实体：xxxVOConvertor         |                                                   |
| 基础设施层转换类       | PO/DO<->实体：xxxDOConvertor？PO/DO<->实体：xxxPOConvertor？ |                                                              |                                                   |

### 通用设计

| **名称**       | **命名**                                   | **业务语义**                                                 |                                                              |
| -------------- | ------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 设计模式相关类 | Builder，Factory等                         | 当使用到设计模式时，需要使用对应的设计模式作为后缀，如ThreadFactory |                                                              |
| 测试类         | Test结尾                                   | UserServiceTest， 表示用来测试UserService类的                |                                                              |
| 枚举类         | Enum 作为后缀                              | GenderEnum                                                   |                                                              |
| 接口实现类     | 接口名+ Impl                               | UserServiceImpl                                              |                                                              |
| 默认接口实现类 | Default+接口名+ Impl?General+接口名+ Impl? |                                                              |                                                              |
| 抽象类         | Abstract 或者 Base 开头                    | BaseUserService                                              |                                                              |
| 异常类         | Exception结尾                              | RuntimeException                                             |                                                              |
| 工具类         | Utils作为后缀                              | StringUtils                                                  |                                                              |
| 助手类         | Helper作为后缀                             |                                                              |                                                              |
| 配置类         | Config作为后缀                             |                                                              |                                                              |
| 特定功能       | 处理器                                     | Handler为后缀Processor为后缀                                 | 表示处理器，校验器，断言，这些类工厂还有配套的方法名如handle，predicate，validate |
|                |                                            |                                                              |                                                              |
| 校验器         | Validator为后缀                            |                                                              |                                                              |
| 装配器         | Assembler为后缀                            |                                                              |                                                              |
| 转换器         | Convertor为后缀                            |                                                              |                                                              |
| 断言           | Predicate为后缀                            |                                                              |                                                              |
| 路由           | Rounter为后缀                              |                                                              |                                                              |
| 切面           | Aspect为后缀                               |                                                              |                                                              |
| 消息监听       | Listener为后缀                             |                                                              |                                                              |
| RPC返回结果    | Result/PageResult                          | 建议follow zan api包定义                                     |                                                              |

## 包命名参考

| **名称**                                           | **命名**                                                     |
| -------------------------------------------------- | ------------------------------------------------------------ |
| Domain领域服务层                                   | `- com.${company}.${businessdomain}.${context}.${aggregateroot} \- domain   |- service //领域服务   |- facade //查询门面   |- model //领域对象   |- event //事件   \- repository //资源库` |
| ApplicationService应用服务层                       | `- com.${company}.${businessdomain}.${context}.${aggregateroot} \- application   |- service //应用服务，面向用例或用户故事   |- command   |- query   \- result ` |
| Infrastructure基础设施层                           | `- com.${company}.${businessdomain}.${context}.${aggregateroot} \- infrastructure   |- dao //数据访问对象   |- config //数据库配置   |- entity //数据对象   |- mapper //数据映射   \- factory ` |
| Dependency资源层（可选，可以合并到infrastructure） | `- com.${company}.${businessdomain}.${context}.${aggregateroot} \- dependency   |- dal   |- call   |- entity   \- factory ` |
| API用户接口层                                      | `- com.${company}.${businessdomain}.${context} |- common | |- consts | |- enums | |- util | \_ exception - com.${company}.${businessdomain}.${context}.${aggregateroot} \- api   |- module   | |- request   | |- dto   | \- response   \_ ${Aggregate}Service` |
| UI接口适配层                                       | `- com.${company}.${businessdomain}.${context}.${aggregateroot}  |- message | |- consumer   | \- listener |- job | |- task | \- handle |- service   |- rpc   | \- ${Aggregate}ServiveImpl   \- web     |- controller     | \- ${Aggregate}Controller     |- request     |- vo     |- response     |- config     \- filter   ` |

## 模块命名参考

| **名称**   | **命名**           | **业务语义**                                                 |
| ---------- | ------------------ | ------------------------------------------------------------ |
| service层  | xxx-service        | 用户接口层，即表现层，实现表现层逻辑（协议、输入&输出转换，如RPC、HTTP） |
| API层      | xxx-api            | 公共常量，服务接口定义                                       |
| 应用服务层 | xxx-application    | 业务流程组装                                                 |
| 领域服务层 | xxx-domain         | 领域对象model、领域服务service、资源库repository接口、事件event |
| 基础设施层 | xxx-infrastructure | 资源库实现（包括数据仓库服务、三方远程服务）                 |