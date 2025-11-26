-- 将现有用户的密码更新为明文（如果之前使用了 BCrypt 加密）
-- 注意：此脚本会将所有用户的密码重置为 '123456'（明文）

-- 更新所有测试用户的密码为明文 '123456'
UPDATE users 
SET password = '123456' 
WHERE username IN ('user001', 'staff001', 'auditor001', 'admin');

-- 验证更新结果
SELECT id, username, password, role, status FROM users WHERE username IN ('user001', 'staff001', 'auditor001', 'admin');

