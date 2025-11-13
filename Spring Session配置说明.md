# Spring Session JDBC 配置说明

## 概述

项目已配置使用 Spring Session JDBC，将会话信息存储在 MySQL 数据库中，支持分布式部署和会话持久化。

## 配置步骤

### 1. 数据库表初始化

在启动应用之前，需要先在数据库中创建 Spring Session 所需的表。

**执行 SQL 脚本：**

项目已提供 SQL 脚本文件：`src/main/resources/sql/spring_session_schema.sql`

请在数据库中执行该脚本，创建以下两个表：

- `SPRING_SESSION` - 存储会话基本信息
- `SPRING_SESSION_ATTRIBUTES` - 存储会话属性

**执行方式：**

1. **使用 MySQL 客户端工具（推荐）：**
   - 使用 Navicat、DBeaver、MySQL Workbench 等工具
   - 连接到数据库：`116.62.82.244:3306/wealthadvisor`
   - 执行 `src/main/resources/sql/spring_session_schema.sql` 文件中的 SQL 语句

2. **使用命令行：**
   ```bash
   mysql -h 116.62.82.244 -P 3306 -u remote_admin -p wealthadvisor < src/main/resources/sql/spring_session_schema.sql
   ```

3. **使用数据库管理工具：**
   - 直接在数据库管理界面中复制 SQL 脚本内容并执行

### 2. 配置文件说明

**application.properties 中的相关配置：**

```properties
# Spring Session JDBC 配置（使用数据库存储会话）
spring.session.store-type=jdbc
# 会话超时时间（秒），默认30分钟（1800秒）
spring.session.timeout=1800
# 是否在启动时初始化数据库表（never=不自动创建，需要手动执行SQL）
spring.session.jdbc.initialize-schema=never
```

**配置项说明：**

- `spring.session.store-type=jdbc`：使用 JDBC 存储会话
- `spring.session.timeout=1800`：会话超时时间（30分钟），可根据需要调整
- `spring.session.jdbc.initialize-schema=never`：不自动创建表，需要手动执行 SQL 脚本

### 3. 依赖说明

**pom.xml 中已包含的依赖：**

```xml
<dependency>
    <groupId>org.springframework.session</groupId>
    <artifactId>spring-session-jdbc</artifactId>
</dependency>
```

该依赖会自动引入 Spring Session 的 JDBC 实现。

## 表结构说明

### SPRING_SESSION 表

存储会话的基本信息：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| PRIMARY_ID | CHAR(36) | 主键ID |
| SESSION_ID | CHAR(36) | 会话ID（唯一索引） |
| CREATION_TIME | BIGINT | 创建时间（时间戳） |
| LAST_ACCESS_TIME | BIGINT | 最后访问时间（时间戳） |
| MAX_INACTIVE_INTERVAL | INT | 最大非活动间隔（秒） |
| EXPIRY_TIME | BIGINT | 过期时间（时间戳） |
| PRINCIPAL_NAME | VARCHAR(100) | 主体名称（用户名等） |

### SPRING_SESSION_ATTRIBUTES 表

存储会话的属性（键值对）：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| SESSION_PRIMARY_ID | CHAR(36) | 会话主键ID（外键） |
| ATTRIBUTE_NAME | VARCHAR(200) | 属性名称 |
| ATTRIBUTE_BYTES | BLOB | 属性值（序列化后的字节） |

## 功能特性

1. **会话持久化**：会话信息存储在数据库中，应用重启后会话不会丢失
2. **分布式支持**：多个应用实例可以共享同一个会话存储
3. **自动清理**：Spring Session 会自动清理过期的会话记录
4. **安全性**：会话数据存储在数据库中，比内存存储更安全

## 使用示例

在 Controller 中使用 HttpSession：

```java
@RestController
@RequestMapping("/api")
public class UserController {
    
    @GetMapping("/login")
    public ResponseEntity<?> login(HttpSession session) {
        // 设置会话属性
        session.setAttribute("userId", 123);
        session.setAttribute("username", "admin");
        return ResponseEntity.ok("登录成功");
    }
    
    @GetMapping("/user")
    public ResponseEntity<?> getUser(HttpSession session) {
        // 获取会话属性
        Integer userId = (Integer) session.getAttribute("userId");
        String username = (String) session.getAttribute("username");
        return ResponseEntity.ok(Map.of("userId", userId, "username", username));
    }
    
    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        // 使会话失效
        session.invalidate();
        return ResponseEntity.ok("登出成功");
    }
}
```

## 注意事项

1. **必须执行 SQL 脚本**：在启动应用前，务必先执行 SQL 脚本创建表，否则会出现表不存在的错误
2. **会话超时时间**：根据业务需求调整 `spring.session.timeout` 的值
3. **数据库连接**：确保数据库连接配置正确，Spring Session 会使用相同的数据源
4. **性能考虑**：如果会话数据量很大，建议定期清理过期会话，Spring Session 会自动处理

## 故障排查

### 问题：Table 'SPRING_SESSION' doesn't exist

**解决方案：**
1. 检查是否已执行 SQL 脚本创建表
2. 确认数据库连接配置正确
3. 检查数据库用户是否有创建表的权限

### 问题：会话丢失

**可能原因：**
1. 会话已过期（超过 `spring.session.timeout` 设置的时间）
2. 数据库连接问题
3. 会话被手动删除

### 问题：性能问题

**优化建议：**
1. 定期清理过期会话（Spring Session 会自动处理）
2. 考虑使用 Redis 作为会话存储（性能更好）
3. 优化数据库索引

## 可选配置

如果需要让 Spring Session 自动创建表，可以修改配置：

```properties
# 自动创建表（仅在表不存在时创建）
spring.session.jdbc.initialize-schema=always
```

**注意：** 自动创建表功能在某些情况下可能不工作，建议手动执行 SQL 脚本。

