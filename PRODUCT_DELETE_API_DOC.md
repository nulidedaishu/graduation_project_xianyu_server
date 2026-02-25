# 商品删除API文档

## 功能概述
实现了商品的软删除功能，用户可以删除自己发布的商品。删除操作采用软删除方式，将商品状态设置为"已删除"(状态码5)，而不是物理删除数据。

## 删除规则
- **权限要求**: 需要登录，且必须是商品的所有者
- **状态限制**: 只有以下状态的商品可以被删除：
  - 待审核 (状态码 0)
  - 审核驳回 (状态码 2)  
  - 已下架 (状态码 3)
  - 已售出 (状态码 4)
- **不可删除状态**: 已上架的商品(状态码 1)不能直接删除，需要先下架

## API接口

### DELETE /api/products/{id}

**功能描述**: 删除指定ID的商品

**权限要求**: 需要用户登录 (@SaCheckLogin(type = "user"))

**请求参数**:
- Path参数:
  - `id` (Long): 商品ID，示例值: 1

**成功响应**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "name": "iPhone 15 Pro",
    "description": "全新未拆封，支持验货",
    "price": 8999.00,
    "categoryId": 1,
    "categoryName": "手机数码",
    "imageUrls": "https://example.com/image1.jpg",
    "contactInfo": "13800138000",
    "status": 5,
    "userId": 1,
    "userNickname": "张三",
    "createTime": "2026-02-26T10:00:00",
    "updateTime": "2026-02-26T10:00:00"
  }
}
```

**错误响应**:

1. 商品不存在 (404):
```json
{
  "code": 404,
  "message": "商品不存在",
  "data": null
}
```

2. 无操作权限 (403):
```json
{
  "code": 403,
  "message": "无权操作此商品",
  "data": null
}
```

3. 状态不允许删除 (400):
```json
{
  "code": 400,
  "message": "当前状态不允许删除商品",
  "data": null
}
```

4. 删除失败 (500):
```json
{
  "code": 500,
  "message": "商品删除失败",
  "data": null
}
```

## 状态码说明

| 状态码 | 状态描述 | 是否可删除 |
|-------|---------|-----------|
| 0 | 待审核 | ✅ 可删除 |
| 1 | 已上架 | ❌ 不可删除 |
| 2 | 审核驳回 | ✅ 可删除 |
| 3 | 已下架 | ✅ 可删除 |
| 4 | 已售出 | ✅ 可删除 |
| 5 | 已删除 | - |

## 使用示例

### 1. 正常删除商品
```bash
curl -X DELETE \
  http://localhost:8080/api/products/1 \
  -H 'Authorization: your_token_here'
```

### 2. 删除已下架商品
```bash
curl -X DELETE \
  http://localhost:8080/api/products/2 \
  -H 'Authorization: your_token_here'
```

## 注意事项

1. **软删除机制**: 删除操作实际上是将商品状态更新为"已删除"，数据仍然保存在数据库中
2. **权限验证**: 系统会验证当前登录用户是否为商品的所有者
3. **状态检查**: 删除前会检查商品当前状态是否允许删除
4. **不可逆操作**: 虽然是软删除，但在前端展示时已删除的商品不会再显示
5. **关联影响**: 删除商品不会影响已完成的订单记录

## 相关接口

- **获取我的商品**: `GET /api/products/my` - 可以查看包括已删除状态在内的所有商品
- **下架商品**: `POST /api/products/{id}/offline` - 将已上架商品下架后才能删除
- **重新上架**: `POST /api/products/{id}/online` - 已删除商品无法重新上架