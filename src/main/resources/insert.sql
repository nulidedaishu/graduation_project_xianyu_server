-- 插入商品分类（三层结构：一级分类 5 个，二级分类 20 个，三级分类 80 个）
-- 一级分类 (parent_id = 0)
INSERT INTO bus_category (parent_id, name)
VALUES (0, '电子产品'),
       (0, '服装鞋帽'),
       (0, '家居用品'),
       (0, '图书音像'),
       (0, '运动户外');

-- 二级分类 - 电子产品 (parent_id = 1)
INSERT INTO bus_category (parent_id, name)
VALUES (1, '手机通讯'),
       (1, '电脑办公'),
       (1, '数码配件'),
       (1, '智能设备');

-- 二级分类 - 服装鞋帽 (parent_id = 2)
INSERT INTO bus_category (parent_id, name)
VALUES (2, '男装'),
       (2, '女装'),
       (2, '童装'),
       (2, '鞋靴');

-- 二级分类 - 家居用品 (parent_id = 3)
INSERT INTO bus_category (parent_id, name)
VALUES (3, '厨房用具'),
       (3, '家用电器'),
       (3, '家具家装'),
       (3, '生活日用');

-- 二级分类 - 图书音像 (parent_id = 4)
INSERT INTO bus_category (parent_id, name)
VALUES (4, '计算机书籍'),
       (4, '人文社科'),
       (4, '教育教辅'),
       (4, '音像制品');

-- 二级分类 - 运动户外 (parent_id = 5)
INSERT INTO bus_category (parent_id, name)
VALUES (5, '健身器材'),
       (5, '户外运动'),
       (5, '体育用品'),
       (5, '骑行装备');

-- 三级分类 - 手机通讯 (parent_id = 6)
INSERT INTO bus_category (parent_id, name)
VALUES (6, '智能手机'),
       (6, '老人机'),
       (6, '手机配件'),
       (6, '对讲机');

-- 三级分类 - 电脑办公 (parent_id = 7)
INSERT INTO bus_category (parent_id, name)
VALUES (7, '笔记本电脑'),
       (7, '台式机'),
       (7, '平板电脑'),
       (7, '打印机');

-- 三级分类 - 数码配件 (parent_id = 8)
INSERT INTO bus_category (parent_id, name)
VALUES (8, '充电器'),
       (8, '数据线'),
       (8, '保护壳'),
       (8, '存储卡');

-- 三级分类 - 智能设备 (parent_id = 9)
INSERT INTO bus_category (parent_id, name)
VALUES (9, '智能手表'),
       (9, '智能手环'),
       (9, '智能音箱'),
       (9, '智能家居');

-- 三级分类 - 男装 (parent_id = 10)
INSERT INTO bus_category (parent_id, name)
VALUES (10, '男士 T 恤'),
       (10, '男士衬衫'),
       (10, '男士外套'),
       (10, '男士裤子');

-- 三级分类 - 女装 (parent_id = 11)
INSERT INTO bus_category (parent_id, name)
VALUES (11, '女士连衣裙'),
       (11, '女士上衣'),
       (11, '女士裤子'),
       (11, '女士外套');

-- 三级分类 - 童装 (parent_id = 12)
INSERT INTO bus_category (parent_id, name)
VALUES (12, '婴儿装'),
       (12, '幼儿装'),
       (12, '儿童上衣'),
       (12, '儿童裤子');

-- 三级分类 - 鞋靴 (parent_id = 13)
INSERT INTO bus_category (parent_id, name)
VALUES (13, '运动鞋'),
       (13, '休闲鞋'),
       (13, '皮鞋'),
       (13, '靴子');

-- 三级分类 - 厨房用具 (parent_id = 14)
INSERT INTO bus_category (parent_id, name)
VALUES (14, '炒锅'),
       (14, '刀具菜板'),
       (14, '餐具套装'),
       (14, '保鲜盒');

-- 三级分类 - 家用电器 (parent_id = 15)
INSERT INTO bus_category (parent_id, name)
VALUES (15, '空调'),
       (15, '冰箱'),
       (15, '洗衣机'),
       (15, '电视机');

-- 三级分类 - 家具家装 (parent_id = 16)
INSERT INTO bus_category (parent_id, name)
VALUES (16, '沙发'),
       (16, '床'),
       (16, '衣柜'),
       (16, '书桌');

-- 三级分类 - 生活日用 (parent_id = 17)
INSERT INTO bus_category (parent_id, name)
VALUES (17, '清洁用品'),
       (17, '收纳用品'),
       (17, '纸品湿巾'),
       (17, '宠物用品');

-- 三级分类 - 计算机书籍 (parent_id = 18)
INSERT INTO bus_category (parent_id, name)
VALUES (18, '编程语言'),
       (18, '数据库'),
       (18, '网络技术'),
       (18, '人工智能');

-- 三级分类 - 人文社科 (parent_id = 19)
INSERT INTO bus_category (parent_id, name)
VALUES (19, '历史书籍'),
       (19, '哲学书籍'),
       (19, '心理学'),
       (19, '经济学');

-- 三级分类 - 教育教辅 (parent_id = 20)
INSERT INTO bus_category (parent_id, name)
VALUES (20, '小学教辅'),
       (20, '初中教辅'),
       (20, '高中教辅'),
       (20, '考研资料');

-- 三级分类 - 音像制品 (parent_id = 21)
INSERT INTO bus_category (parent_id, name)
VALUES (21, '音乐 CD'),
       (21, '电影 DVD'),
       (21, '教育视频'),
       (21, '游戏光盘');

-- 三级分类 - 健身器材 (parent_id = 22)
INSERT INTO bus_category (parent_id, name)
VALUES (22, '跑步机'),
       (22, '健身车'),
       (22, '哑铃杠铃'),
       (22, '瑜伽用品');

-- 三级分类 - 户外运动 (parent_id = 23)
INSERT INTO bus_category (parent_id, name)
VALUES (23, '帐篷睡袋'),
       (23, '登山装备'),
       (23, '野炊用品'),
       (23, '照明工具');

-- 三级分类 - 体育用品 (parent_id = 24)
INSERT INTO bus_category (parent_id, name)
VALUES (24, '篮球'),
       (24, '足球'),
       (24, '羽毛球'),
       (24, '乒乓球');

-- 三级分类 - 骑行装备 (parent_id = 25)
INSERT INTO bus_category (parent_id, name)
VALUES (25, '山地自行车'),
       (25, '公路自行车'),
       (25, '骑行服饰'),
       (25, '安全装备');


-- 插入30个商品
INSERT INTO bus_product (user_id, category_id, title, description, price, status)
VALUES (12, 1, 'iPhone 15 Pro', '全新未拆封，支持验货，性能强劲', 8999.00, 1),
       (12, 1, 'MacBook Air M2', '轻薄便携，续航强劲，适合办公学习', 9999.00, 1),
       (12, 6, '小米14手机', '拍照出色，性价比高', 3999.00, 1),
       (12, 6, '华为Mate60', '国产旗舰，性能优秀', 5999.00, 1),
       (12, 7, '联想ThinkPad X1', '商务本，品质可靠', 8888.00, 1),
       (12, 7, '戴尔游匣G15', '游戏本，配置强劲', 7999.00, 1),
       (12, 2, '男士休闲衬衫', '纯棉材质，舒适透气', 199.00, 1),
       (12, 2, '女士连衣裙', '春夏新款，优雅时尚', 299.00, 1),
       (12, 8, '男士牛仔裤', '经典款式，百搭耐穿', 159.00, 1),
       (12, 8, '男士POLO衫', '商务休闲，质感优良', 129.00, 1),
       (12, 9, '女士针织开衫', '柔软舒适，春秋必备', 189.00, 1),
       (12, 9, '女士半身裙', '显瘦A字版型，优雅大方', 169.00, 1),
       (12, 3, '空气净化器', '除甲醛除异味，智能操控', 1299.00, 1),
       (12, 3, '扫地机器人', '激光导航，自动回充', 1999.00, 1),
       (12, 10, '不粘锅套装', '三层复合锅体，健康烹饪', 299.00, 1),
       (12, 10, '电饭煲', 'IH电磁加热，米饭更香', 399.00, 1),
       (12, 4, 'Java编程思想', '经典编程书籍，深入理解面向对象', 89.00, 1),
       (12, 4, 'Python数据分析', '实战教程，从入门到精通', 69.00, 1),
       (12, 4, '算法导论', '计算机科学经典教材', 129.00, 1),
       (12, 4, '设计模式', '软件设计最佳实践指南', 79.00, 1),
       (12, 5, '瑜伽垫', '防滑加厚，环保材质', 89.00, 1),
       (12, 5, '跑步机', '家用静音，多种运动模式', 1999.00, 1),
       (12, 5, '哑铃套装', '可调节重量，健身必备', 299.00, 1),
       (12, 5, '篮球', '室内外通用，手感舒适', 129.00, 1),
       (12, 1, 'iPad Air', '平板电脑，娱乐办公两不误', 4399.00, 1),
       (12, 6, 'OPPO Reno', '拍照手机，颜值担当', 2999.00, 1),
       (12, 3, '加湿器', '静音运行，智能恒湿', 199.00, 1),
       (12, 10, '保温杯', '316不锈钢，长效保温', 129.00, 1),
       (12, 4, '机器学习实战', 'AI入门必读，理论与实践结合', 99.00, 1),
       (12, 5, '羽毛球拍', '碳纤维材质，轻量耐用', 159.00, 1);

