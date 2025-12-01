# UniSport Backend 快速启动指南

## 📋 前置条件检查

在启动项目之前，请确保已安装以下软件：

### 必需软件

- ✅ **JDK 17+**
  ```bash
  java -version
  # 应输出: java version "17.x.x" 或更高
  ```

- ✅ **Maven 3.6+**
  ```bash
  mvn -version
  # 应输出: Apache Maven 3.6.x 或更高
  ```

- ✅ **MySQL 8.0+**
  ```bash
  mysql --version
  # 应输出: mysql Ver 8.0.x
  ```

- ✅ **Redis 5.0+** (可选，用于缓存)
  ```bash
  redis-server --version
  # 应输出: Redis server v=5.x.x 或更高
  ```

## 🚀 快速启动步骤

### 步骤 1: 初始化数据库

1. 启动 MySQL 服务

2. 执行初始化脚本：

```bash
# Windows
mysql -u root -p < src\main\resources\sql\init.sql

# Linux/Mac
mysql -u root -p < src/main/resources/sql/init.sql
```

或者使用 MySQL 客户端工具（Navicat、DBeaver等）导入 `src/main/resources/sql/init.sql` 文件。

### 步骤 2: 配置数据库连接

编辑 `src/main/resources/application.properties` 文件：

```properties
# 修改为你的数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/unisport?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=你的数据库密码
```

### 步骤 3: 启动 Redis（可选）

```bash
# Windows
redis-server

# Linux/Mac
redis-server
```

如果不使用 Redis，可以在 `pom.xml` 中注释掉 Redis 依赖。

### 步骤 4: 启动项目

#### 方式一：使用 Maven 命令行

```bash
# 清理并编译
mvn clean install

# 启动项目
mvn spring-boot:run
```

#### 方式二：使用 IDE

1. 用 IntelliJ IDEA 打开项目
2. 等待 Maven 依赖下载完成
3. 找到 `UnisportApplication.java`
4. 右键 -> Run 'UnisportApplication'

### 步骤 5: 验证启动

启动成功后，控制台会显示：

```
===================================
UniSport Backend Started Successfully!
API Documentation: http://localhost:8080/doc.html
===================================
```

## 🧪 测试接口

### 1. 健康检查

```bash
curl http://localhost:8080/api/system/health
```

预期响应：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "status": "UP",
    "application": "UniSport Backend",
    "version": "1.0.0",
    "timestamp": 1701234567890
  },
  "timestamp": 1701234567890
}
```

### 2. 系统信息

```bash
curl http://localhost:8080/api/system/info
```

### 3. 访问 API 文档

在浏览器中打开：

- **Knife4j 文档**: http://localhost:8080/doc.html
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html

## 🔧 常见问题

### 问题 1: 数据库连接失败

**错误信息**: `Communications link failure`

**解决方案**:
1. 检查 MySQL 是否启动
2. 检查数据库配置（用户名、密码、端口）
3. 确认数据库 `unisport` 是否已创建

### 问题 2: Redis 连接失败

**错误信息**: `Unable to connect to Redis`

**解决方案**:
1. 检查 Redis 是否启动
2. 如果不使用 Redis，可以临时注释掉相关依赖

### 问题 3: 端口被占用

**错误信息**: `Port 8080 was already in use`

**解决方案**:
修改 `application.properties` 中的端口：
```properties
server.port=8081
```

### 问题 4: Maven 依赖下载慢

**解决方案**:
配置国内 Maven 镜像（阿里云）：

在 `~/.m2/settings.xml` 中添加：
```xml
<mirror>
  <id>aliyun</id>
  <mirrorOf>central</mirrorOf>
  <name>Aliyun Maven</name>
  <url>https://maven.aliyun.com/repository/public</url>
</mirror>
```

## 📚 下一步

- 📖 查看 [README.md](README.md) 了解项目详细信息
- 🗄️ 查看 [数据库设计文档](../unisport/docs/database-design.md)
- 🔌 开始开发业务接口（用户、赛事、帖子等）
- 🎨 配合前端项目进行联调

## 🆘 获取帮助

如果遇到问题，可以：

1. 查看控制台错误日志
2. 检查 `logs/` 目录下的日志文件
3. 访问项目 Issues 页面
4. 联系开发团队

---

**祝你开发顺利！** 🎉
