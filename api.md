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