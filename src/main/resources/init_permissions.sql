-- 权限初始化脚本
-- 执行此脚本前请确保 sys_role、sys_menu、sys_role_menu、sys_admin_role 表已创建

-- ============================================
-- 1. 初始化角色
-- ============================================

-- 普通用户角色
INSERT INTO sys_role (id, role_name, role_code, create_time, update_time) VALUES
(2, '普通用户', 'user', NOW(), NOW());

-- 管理员角色
INSERT INTO sys_role (id, role_name, role_code, create_time, update_time) VALUES
(1, '超级管理员', 'admin', NOW(), NOW());

-- ============================================
-- 2. 初始化权限菜单（用户权限）
-- ============================================

INSERT INTO sys_menu (id, parent_id, menu_name, permission, type, sort, create_time, update_time) VALUES
-- 用户管理
(100, 0, '用户管理', NULL, 0, 1, NOW(), NOW()),
(101, 100, '获取用户列表', 'user:user:list', 2, 1, NOW(), NOW()),
(102, 100, '获取用户详情', 'user:user:detail', 2, 2, NOW(), NOW()),
(103, 100, '更新用户信息', 'user:user:update', 2, 3, NOW(), NOW()),
(104, 100, '获取当前用户', 'user:user:me', 2, 4, NOW(), NOW()),
(105, 100, '刷新会话', 'user:user:refresh', 2, 5, NOW(), NOW()),

-- 商品管理
(200, 0, '商品管理', NULL, 0, 2, NOW(), NOW()),
(201, 200, '发布商品', 'user:product:create', 2, 1, NOW(), NOW()),
(202, 200, '获取我的商品', 'user:product:my', 2, 2, NOW(), NOW()),
(203, 200, '下架商品', 'user:product:offline', 2, 3, NOW(), NOW()),
(204, 200, '上架商品', 'user:product:online', 2, 4, NOW(), NOW()),
(205, 200, '删除商品', 'user:product:delete', 2, 5, NOW(), NOW()),
(206, 200, '修改商品', 'user:product:update', 2, 6, NOW(), NOW()),
(207, 200, '检查库存', 'user:product:check-stock', 2, 7, NOW(), NOW()),
(208, 200, '锁定库存', 'user:product:lock-stock', 2, 8, NOW(), NOW()),

-- 购物车管理
(300, 0, '购物车管理', NULL, 0, 3, NOW(), NOW()),
(301, 300, '购物车所有操作', 'user:cart:*', 2, 1, NOW(), NOW()),

-- 订单管理
(400, 0, '订单管理', NULL, 0, 4, NOW(), NOW()),
(401, 400, '订单所有操作', 'user:order:*', 2, 1, NOW(), NOW()),

-- 支付管理
(500, 0, '支付管理', NULL, 0, 5, NOW(), NOW()),
(501, 500, '创建支付', 'user:payment:create', 2, 1, NOW(), NOW()),

-- 地址管理
(600, 0, '地址管理', NULL, 0, 6, NOW(), NOW()),
(601, 600, '地址所有操作', 'user:address:*', 2, 1, NOW(), NOW()),

-- 收藏管理
(700, 0, '收藏管理', NULL, 0, 7, NOW(), NOW()),
(701, 700, '收藏所有操作', 'user:favorite:*', 2, 1, NOW(), NOW()),

-- 评价管理
(800, 0, '评价管理', NULL, 0, 8, NOW(), NOW()),
(801, 800, '提交评价', 'user:evaluate:create', 2, 1, NOW(), NOW()),
(802, 800, '我的评价', 'user:evaluate:my', 2, 2, NOW(), NOW()),
(803, 800, '待评价订单', 'user:evaluate:pending', 2, 3, NOW(), NOW()),

-- 文件管理
(900, 0, '文件管理', NULL, 0, 9, NOW(), NOW()),
(901, 900, '文件所有操作', 'user:file:*', 2, 1, NOW(), NOW()),

-- 信用积分
(1000, 0, '信用积分', NULL, 0, 10, NOW(), NOW()),
(1001, 1000, '积分所有操作', 'user:credit:*', 2, 1, NOW(), NOW()),

-- 消息推送
(1100, 0, '消息推送', NULL, 0, 11, NOW(), NOW()),
(1101, 1100, '消息推送用户操作', 'user:message:*', 2, 1, NOW(), NOW()),

-- 通知管理
(1200, 0, '通知管理', NULL, 0, 12, NOW(), NOW()),
(1201, 1200, '获取通知列表', 'user:notice:list', 2, 1, NOW(), NOW()),
(1202, 1200, '获取未读通知', 'user:notice:unread', 2, 2, NOW(), NOW()),
(1203, 1200, '获取通知统计', 'user:notice:statistics', 2, 3, NOW(), NOW()),
(1204, 1200, '标记已读', 'user:notice:read', 2, 4, NOW(), NOW()),
(1205, 1200, '全部已读', 'user:notice:read-all', 2, 5, NOW(), NOW()),
(1206, 1200, '删除通知', 'user:notice:delete', 2, 6, NOW(), NOW()),
(1207, 1200, '获取系统通知会话', 'user:notice:session', 2, 7, NOW(), NOW()),
(1208, 1200, '游标分页获取通知', 'user:notice:messages', 2, 8, NOW(), NOW()),
(1209, 1200, '标记系统通知已读', 'user:notice:session-read', 2, 9, NOW(), NOW()),

-- AI服务
(1300, 0, 'AI服务', NULL, 0, 13, NOW(), NOW()),
(1301, 1300, '生成商品描述', 'user:ai:generate', 2, 1, NOW(), NOW()),

-- 即时通讯
(1400, 0, '即时通讯', NULL, 0, 14, NOW(), NOW()),
(1401, 1400, '聊天所有操作', 'user:chat:*', 2, 1, NOW(), NOW()),

-- 分类管理（用户）
(1500, 0, '分类查询', NULL, 0, 15, NOW(), NOW()),
(1501, 1500, '分类详情', 'user:category:detail', 2, 1, NOW(), NOW()),
(1502, 1500, '分类列表', 'user:category:list', 2, 2, NOW(), NOW()),
(1503, 1500, '子分类', 'user:category:children', 2, 3, NOW(), NOW()),
(1504, 1500, '检查分类名', 'user:category:check', 2, 4, NOW(), NOW());

-- ============================================
-- 3. 初始化权限菜单（管理员权限）
-- ============================================

INSERT INTO sys_menu (id, parent_id, menu_name, permission, type, sort, create_time, update_time) VALUES
-- 用户管理（管理员）
(2000, 0, '用户管理（管理员）', NULL, 0, 100, NOW(), NOW()),
(2001, 2000, '删除用户', 'admin:user:delete', 2, 1, NOW(), NOW()),
(2002, 2000, '用户列表', 'admin:user:list', 2, 2, NOW(), NOW()),
(2003, 2000, '用户详情', 'admin:user:detail', 2, 3, NOW(), NOW()),
(2004, 2000, '启用用户', 'admin:user:enable', 2, 4, NOW(), NOW()),
(2005, 2000, '禁用用户', 'admin:user:disable', 2, 5, NOW(), NOW()),
(2006, 2000, '更新状态', 'admin:user:status', 2, 6, NOW(), NOW()),
(2007, 2000, '所有用户', 'admin:user:all', 2, 7, NOW(), NOW()),

-- 商品管理（管理员）
(2100, 0, '商品管理（管理员）', NULL, 0, 101, NOW(), NOW()),
(2101, 2100, '商品列表', 'admin:product:list', 2, 1, NOW(), NOW()),
(2102, 2100, '待审核商品', 'admin:product:pending', 2, 2, NOW(), NOW()),
(2103, 2100, '审核商品', 'admin:product:audit', 2, 3, NOW(), NOW()),
(2104, 2100, '强制下架', 'admin:product:force-offline', 2, 4, NOW(), NOW()),
(2105, 2100, '商品详情', 'admin:product:detail', 2, 5, NOW(), NOW()),

-- 分类管理（管理员）
(2200, 0, '分类管理（管理员）', NULL, 0, 102, NOW(), NOW()),
(2201, 2200, '创建分类', 'admin:category:create', 2, 1, NOW(), NOW()),
(2202, 2200, '更新分类', 'admin:category:update', 2, 2, NOW(), NOW()),
(2203, 2200, '删除分类', 'admin:category:delete', 2, 3, NOW(), NOW()),

-- 通知管理（管理员）
(2300, 0, '通知管理（管理员）', NULL, 0, 103, NOW(), NOW()),
(2301, 2300, '发送通知', 'admin:notice:send', 2, 1, NOW(), NOW()),

-- 后台首页
(2400, 0, '后台首页', NULL, 0, 104, NOW(), NOW()),
(2401, 2400, '统计数据', 'admin:dashboard:statistics', 2, 1, NOW(), NOW()),

-- AI配置
(2500, 0, 'AI配置', NULL, 0, 105, NOW(), NOW()),
(2501, 2500, '获取所有AI配置', 'admin:ai-config:list', 2, 1, NOW(), NOW()),
(2502, 2500, '获取配置详情', 'admin:ai-config:detail', 2, 2, NOW(), NOW()),
(2503, 2500, '创建配置', 'admin:ai-config:create', 2, 3, NOW(), NOW()),
(2504, 2500, '更新配置', 'admin:ai-config:update', 2, 4, NOW(), NOW()),
(2505, 2500, '删除配置', 'admin:ai-config:delete', 2, 5, NOW(), NOW()),
(2506, 2500, '设置默认配置', 'admin:ai-config:set-default', 2, 6, NOW(), NOW()),
(2507, 2500, '启用/禁用配置', 'admin:ai-config:toggle', 2, 7, NOW(), NOW()),
(2508, 2500, '测试已保存的配置', 'admin:ai-config:test', 2, 8, NOW(), NOW()),
(2509, 2500, '测试新配置', 'admin:ai-config:test-new', 2, 9, NOW(), NOW()),

-- 连接统计
(2600, 0, '连接统计', NULL, 0, 106, NOW(), NOW()),
(2601, 2600, '获取连接统计', 'admin:connection:stats', 2, 1, NOW(), NOW());

-- ============================================
-- 4. 角色-权限关联（给角色分配权限）
-- ============================================

-- 普通用户角色（id=2）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 101), (2, 102), (2, 103), (2, 104), (2, 105),
(2, 201), (2, 202), (2, 203), (2, 204), (2, 205), (2, 206), (2, 207), (2, 208),
(2, 301),
(2, 401),
(2, 501),
(2, 601),
(2, 701),
(2, 801), (2, 802), (2, 803),
(2, 901),
(2, 1001),
(2, 1101),
(2, 1201), (2, 1202), (2, 1203), (2, 1204), (2, 1205), (2, 1206), (2, 1207), (2, 1208), (2, 1209),
(2, 1301),
(2, 1401),
(2, 1501), (2, 1502), (2, 1503), (2, 1504);

-- 管理员角色（id=1）拥有所有权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
-- 用户权限
(1, 101), (1, 102), (1, 103), (1, 104), (1, 105),
(1, 201), (1, 202), (1, 203), (1, 204), (1, 205), (1, 206), (1, 207), (1, 208),
(1, 301),
(1, 401),
(1, 501),
(1, 601),
(1, 701),
(1, 801), (1, 802), (1, 803),
(1, 901),
(1, 1001),
(1, 1101),
(1, 1201), (1, 1202), (1, 1203), (1, 1204), (1, 1205), (1, 1206), (1, 1207), (1, 1208), (1, 1209),
(1, 1301),
(1, 1401),
(1, 1501), (1, 1502), (1, 1503), (1, 1504),
-- 管理员权限
(1, 2001), (1, 2002), (1, 2003), (1, 2004), (1, 2005), (1, 2006), (1, 2007),
(1, 2101), (1, 2102), (1, 2103), (1, 2104), (1, 2105),
(1, 2201), (1, 2202), (1, 2203),
(1, 2301),
(1, 2401),
(1, 2501), (1, 2502), (1, 2503), (1, 2504), (1, 2505), (1, 2506), (1, 2507), (1, 2508), (1, 2509),
(1, 2601);

-- ============================================
-- 5. 给管理员账号关联角色（假设管理员ID=1）
-- ============================================
-- 注意：执行前请确保 sys_admin 表中有 id=1 的管理员记录
-- 如果有多个管理员，请为每个管理员添加关联
-- INSERT INTO sys_admin_role (admin_id, role_id) VALUES (1, 2);

-- 给普通用户关联角色（如果有现有用户需要绑定角色）
-- INSERT INTO sys_user_role (user_id, role_id) VALUES (用户ID, 1);
