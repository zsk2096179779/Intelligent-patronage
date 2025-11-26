-- 插入测试用户数据
-- 注意：密码使用明文存储（不加密）

-- 删除已存在的测试用户（如果存在）
DELETE FROM users WHERE username IN ('user001', 'staff001', 'auditor001', 'admin');

-- 插入测试用户
-- 密码都是 '123456'（明文）
INSERT INTO users (username, password, email, role, status, created_at, updated_at) VALUES
('user001', '123456', 'user001@example.com', 'USER', 1, NOW(), NOW()),
('staff001', '123456', 'staff001@example.com', 'STAFF', 1, NOW(), NOW()),
('auditor001', '123456', 'auditor001@example.com', 'AUDITOR', 1, NOW(), NOW()),
('admin', '123456', 'admin@example.com', 'AUDITOR', 1, NOW(), NOW());

-- 验证插入结果
SELECT id, username, email, role, status FROM users WHERE username IN ('user001', 'staff001', 'auditor001', 'admin');

