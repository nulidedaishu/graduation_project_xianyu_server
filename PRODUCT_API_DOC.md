# 商品管理API文档

## 功能概述
实现了商品发布的完整功能，包括：
- 商品信息的创建和发布
- 商品分类关联验证
- 商品状态管理（默认待审核状态）
- 用户身份验证
- 管理员审核功能

## API接口列表

### 1. 发布商品
**POST** `/api/products`

### 2. 管理员审核商品
**PATCH** `/api/products/{productId}/review`

**权限要求**: 需要登录

**请求参数**:
```json
{
  "name": "iPhone 15 Pro",           // 商品名称（必填）
  "description": "全新未拆封，支持验货",   // 商品描述（可选）
  "price": 8999.00,                 // 商品价格（必填，必须大于0）
  "categoryId": 1,                  // 商品分类ID（必填）
  "imageUrls": "https://example.com/image1.jpg,https://example.com/image2.jpg", // 商品图片URL列表（可选）
  "detail": "详细的产品介绍...",      // 商品详情（可选）
  "contactInfo": "13800138000"      // 联系方式（可选）
}
```

**成功响应**:
```json
{
  "code": 200,
  "message": "商品发布成功，等待审核",
  "data": {
    "id": 1,
    "name": "iPhone 15 Pro",
    "description": "全新未拆封，支持验货",
    "price": 8999.00,
    "categoryId": 1,
    "categoryName": "手机数码",
    "imageUrls": "https://example.com/image1.jpg,https://example.com/image2.jpg",
    "detail": "详细的产品介绍...",
    "contactInfo": "13800138000",
    "status": 0,
    "userId": 1,
    "userNickname": "张三",
    "createTime": "2026-02-12T10:00:00",
    "updateTime": "2026-02-12T10:00:00"
  }
}
```

## 商品状态说明

| 状态值 | 说明 |
|-------|------|
| 0 | 待审核 |
| 1 | 已上架 |
| 2 | 审核驳回 |
| 3 | 已下架 |
| 4 | 已售出 |
| 5 | 已删除 |

## 审核流程说明

1. **用户发布商品**：商品初始状态为0（待审核）
2. **管理员审核**：
   - 审核通过：状态变为1（已上架）
   - 审核拒绝：状态变为2（审核驳回），需填写拒绝理由
3. **后续操作**：
   - 已上架商品可进行下架操作（状态变为3）
   - 审核驳回的商品可重新编辑后再次提交审核

## 错误码说明

| 错误码 | 说明 |
|-------|------|
| 400 | 请求参数错误 |
| 401 | 未授权，请先登录 |
| 403 | 权限不足（需要管理员角色） |
| 404 | 分类不存在/商品不存在 |
| 40101 | 用户不存在 |
| 40301 | 商品不存在 |
| 40601 | 商品创建失败 |
| 40602 | 商品更新失败 |
| 422 | 无效的商品状态（非待审核状态无法审核） |

## 验证规则

1. **商品名称**: 必填，不能为空
2. **商品价格**: 必填，必须大于0
3. **分类ID**: 必填，必须存在对应的分类
4. **用户身份**: 必须登录才能发布商品

## 使用示例

### cURL请求示例
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_token_here" \
  -d '{
    "name": "MacBook Pro 14寸",
    "description": "2023款，M2芯片，全新未拆封",
    "price": 15999.00,
    "categoryId": 2,
    "imageUrls": "https://example.com/macbook1.jpg,https://example.com/macbook2.jpg",
    "detail": "配备M2 Pro芯片，16GB内存，512GB SSD存储",
    "contactInfo": "13800138000"
  }'
```

### JavaScript/Fetch示例
```javascript
const productData = {
  name: "iPad Air 5",
  description: "2022款，全新未拆封",
  price: 4599.00,
  categoryId: 3,
  imageUrls: "https://example.com/ipad1.jpg",
  detail: "10.9英寸 Liquid Retina 显示屏",
  contactInfo: "13800138000"
};

fetch('/api/products', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer ' + token
  },
  body: JSON.stringify(productData)
})
.then(response => response.json())
.then(data => {
  console.log('商品发布成功:', data);
})
.catch(error => {
  console.error('发布失败:', error);
});
```

## 注意事项

1. 商品发布后默认状态为"待审核"(0)
2. 需要管理员审核通过后才能上架展示
3. 商品图片URL支持多个，用逗号分隔
4. 价格单位为人民币元
5. 发布成功后会返回完整的商品信息，包括分类名称和用户昵称