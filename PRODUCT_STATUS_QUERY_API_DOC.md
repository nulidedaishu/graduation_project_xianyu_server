# 商品状态查询API文档

## 概述
提供按不同状态查询商品的6个接口，每个接口对应一个商品状态码。

## 接口列表

### 1. 查询待审核商品 (状态0)
```
GET /api/products/status/pending
```
**权限要求**: 管理员角色  
**描述**: 查询所有待审核的商品  

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "name": "iPhone 15 Pro",
      "description": "全新未拆封，支持验货",
      "price": 8999.00,
      "categoryId": 1,
      "categoryName": "手机数码",
      "imageUrls": "https://example.com/image1.jpg",
      "status": 0,
      "userId": 1,
      "userNickname": "张三",
      "createTime": "2026-02-12T10:00:00",
      "updateTime": "2026-02-12T10:00:00"
    }
  ]
}
```

### 2. 查询已上架商品 (状态1)
```
GET /api/products/status/listed
```
**权限要求**: 无需特殊权限  
**描述**: 查询所有已上架的商品  

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 2,
      "name": "MacBook Air M2",
      "description": "轻薄便携，性能强劲",
      "price": 9999.00,
      "categoryId": 2,
      "categoryName": "电脑办公",
      "imageUrls": "https://example.com/macbook.jpg",
      "status": 1,
      "userId": 2,
      "userNickname": "李四",
      "createTime": "2026-02-12T11:00:00",
      "updateTime": "2026-02-12T11:00:00"
    }
  ]
}
```

### 3. 查询审核驳回商品 (状态2)
```
GET /api/products/status/rejected
```
**权限要求**: 管理员角色  
**描述**: 查询所有审核驳回的商品  

### 4. 查询已下架商品 (状态3)
```
GET /api/products/status/offline
```
**权限要求**: 无需特殊权限  
**描述**: 查询所有已下架的商品  

### 5. 查询已售出商品 (状态4)
```
GET /api/products/status/sold
```
**权限要求**: 无需特殊权限  
**描述**: 查询所有已售出的商品  

### 6. 查询已删除商品 (状态5)
```
GET /api/products/status/deleted
```
**权限要求**: 管理员角色  
**描述**: 查询所有已删除的商品  

## 商品状态码说明
- **0**: 待审核 - 商品刚发布，等待管理员审核
- **1**: 已上架 - 商品审核通过，正在展示销售
- **2**: 审核驳回 - 商品审核未通过，需要修改后重新提交
- **3**: 已下架 - 商品主动下架或超时下架
- **4**: 已售出 - 商品已完成交易
- **5**: 已删除 - 商品已被删除

## 错误响应
```json
{
  "code": 401,
  "message": "未授权访问",
  "data": null
}
```

## 注意事项
1. 需要管理员权限的接口会验证用户角色
2. 所有查询结果按创建时间倒序排列
3. 返回的数据包含完整的商品信息和关联的分类、用户信息
4. 空结果集也会返回200状态码，data为空数组