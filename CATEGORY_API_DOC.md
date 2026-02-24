# 商品分类管理API文档

## 功能概述
实现了商品分类的完整增删改查功能，包括：
- 分类的创建、更新、删除
- 分类树形结构查询
- 同级分类名称唯一性校验
- 分类使用情况检查（防止删除正在使用的分类）

## API接口列表

### 1. 创建商品分类
**POST** `/api/categories`

**权限要求**: 需要管理员角色

**请求参数**:
```json
{
  "parentId": 1,        // 父分类ID（可为空，表示顶级分类）
  "name": "电子产品",    // 分类名称（必填，不超过50字符）
  "icon": "icon-url",   // 图标URL（可选，不超过255字符）
  "sort": 1            // 排序值（必填）
}
```

**成功响应**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "parentId": null,
    "name": "电子产品",
    "icon": "icon-url",
    "sort": 1,
    "createTime": "2026-02-12T17:00:00"
  }
}
```

### 2. 更新商品分类
**PUT** `/api/categories`

**权限要求**: 需要管理员角色

**请求参数**:
```json
{
  "id": 1,              // 分类ID（必填）
  "parentId": 2,        // 父分类ID（可为空）
  "name": "数码产品",    // 分类名称（必填）
  "icon": "new-icon",   // 图标URL（可选）
  "sort": 2            // 排序值（必填）
}
```

### 3. 删除商品分类
**DELETE** `/api/categories/{id}`

**权限要求**: 需要管理员角色

**路径参数**:
- `id`: 分类ID

**注意**: 
- 如果分类下有子分类，无法删除
- 如果分类正在被商品使用，无法删除

### 4. 获取分类详情
**GET** `/api/categories/{id}`

**权限要求**: 需要登录

**路径参数**:
- `id`: 分类ID

### 5. 获取所有分类（平铺结构）
**GET** `/api/categories`

**权限要求**: 需要登录

**响应示例**:
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "parentId": null,
      "name": "电子产品",
      "icon": "icon-url",
      "sort": 1,
      "createTime": "2026-02-12T17:00:00"
    }
  ]
}
```

### 6. 获取分类树形结构
**GET** `/api/categories/tree`

**权限要求**: 需要登录

**响应示例**:
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "parentId": null,
      "name": "电子产品",
      "icon": "icon-url",
      "sort": 1,
      "createTime": "2026-02-12T17:00:00",
      "children": [
        {
          "id": 2,
          "parentId": 1,
          "name": "手机",
          "icon": "phone-icon",
          "sort": 1,
          "createTime": "2026-02-12T17:00:00",
          "children": []
        }
      ]
    }
  ]
}
```

### 7. 获取指定分类的子分类
**GET** `/api/categories/{parentId}/children`

**权限要求**: 需要登录

**路径参数**:
- `parentId`: 父分类ID

### 8. 检查分类名称是否可用
**GET** `/api/categories/check-name`

**权限要求**: 需要登录

**查询参数**:
- `parentId`: 父分类ID（可选）
- `name`: 分类名称（必填）
- `excludeId`: 排除的分类ID（用于更新时检查，可选）

**响应示例**:
```json
{
  "code": 200,
  "message": "Success",
  "data": true  // true表示可用，false表示已存在
}
```

## 错误码说明

| 错误码 | 说明 |
|-------|------|
| 40501 | 分类不存在 |
| 40502 | 父分类不存在 |
| 40503 | 该分类下存在子分类，无法删除 |
| 40504 | 该分类正在被商品使用，无法删除 |
| 40505 | 同级分类名称已存在 |

## 业务规则

1. **层级限制**: 不允许将分类设置为自己的子分类或后代分类
2. **名称唯一性**: 同一级别的分类名称必须唯一
3. **删除保护**: 有子分类或正在被商品使用的分类不能删除
4. **排序机制**: 分类按sort字段升序排列
5. **权限控制**: 增删改操作需要管理员权限，查询操作需要登录

## 数据库设计

### 分类表结构 (bus_category)
```sql
CREATE TABLE bus_category (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  parent_id BIGINT NULL COMMENT '父分类ID',
  name VARCHAR(50) NOT NULL COMMENT '分类名称',
  icon VARCHAR(255) NULL COMMENT '图标URL',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);
```

## 技术实现要点

1. **事务管理**: 增删改操作使用`@Transactional`注解保证数据一致性
2. **参数校验**: 使用Jakarta Validation进行请求参数校验
3. **日志记录**: 关键业务操作都有详细的日志记录
4. **异常处理**: 自定义业务异常配合全局异常处理器
5. **安全控制**: 使用Sa-Token进行权限控制
6. **树形结构**: 递归算法构建分类树形结构

## 测试覆盖

- DTO类的基本功能测试 ✓
- 控制器接口的模拟测试
- 业务逻辑的单元测试
- 完整流程的集成测试