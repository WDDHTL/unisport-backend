# UniSport Backend

> UniSport 校园体育社交应用后端服务

## 项目简介

UniSport Backend 是一个基于 Spring Boot 3.2 构建的校园体育社交应用后端服务，为前端应用提供完整的 RESTful API 接口。

## 技术栈

- **核心框架**: Spring Boot 3.2.0
- **Java 版本**: JDK 17+
- **持久层框架**: MyBatis Plus 3.5.5
- **数据库**: MySQL 8.0+
- **缓存**: Redis
- **安全认证**: JWT (JSON Web Token)
- **API 文档**: Knife4j 4.3.0 (Swagger 增强版)
- **工具类库**: Hutool 5.8.23

## 项目结构

```
unisport-backend/
├── src/main/java/com/unisport/
│   ├── UnisportApplication.java    # 应用启动类
│   ├── common/                     # 通用类
│   │   ├── Result.java            # 统一响应结果
│   │   └── BusinessException.java # 业务异常
│   ├── config/                    # 配置类
│   │   ├── WebMvcConfig.java     # Web MVC 配置（跨域等）
│   │   ├── Knife4jConfig.java    # API 文档配置
│   │   ├── MyMetaObjectHandler.java # MyBatis Plus 自动填充
│   │   └── GlobalExceptionHandler.java # 全局异常处理
│   ├── controller/                # 控制层
│   │   └── SystemController.java # 系统控制器
│   ├── entity/                    # 实体类
│   │   ├── User.java             # 用户实体
│   │   ├── Category.java         # 分类实体
│   │   └── Post.java             # 帖子实体
│   ├── mapper/                    # Mapper 接口
│   ├── service/                   # 服务层接口
│   └── service/impl/              # 服务层实现
├── src/main/resources/
│   ├── application.properties     # 应用配置
│   └── mapper/                    # MyBatis XML 文件
├── pom.xml                        # Maven 配置
└── README.md                      # 项目说明
```

## 快速开始

### 环境要求

- JDK 17 或更高版本
- Maven 3.6+
- MySQL 8.0+
- Redis 5.0+

### 数据库初始化

1. 创建数据库：

```sql
CREATE DATABASE unisport DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行数据库初始化脚本（参考前端项目 `docs/database-design.md`）

### 配置文件

修改 `src/main/resources/application.properties` 中的数据库和 Redis 配置：

```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/unisport?...
spring.datasource.username=root
spring.datasource.password=your_password

# Redis 配置
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

### 运行项目

#### 方式一：使用 Maven

```bash
# 安装依赖
mvn clean install

# 启动项目
mvn spring-boot:run
```

#### 方式二：使用 IDE

1. 导入项目到 IDEA 或 Eclipse
2. 等待 Maven 依赖下载完成
3. 运行 `UnisportApplication.java`

### 访问接口文档

启动成功后，访问以下地址查看 API 文档：

- **Knife4j 文档**: http://localhost:8080/doc.html
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html

### 测试接口

启动后可以访问健康检查接口：

```bash
# 健康检查
curl http://localhost:8080/api/system/health

# 系统信息
curl http://localhost:8080/api/system/info
```

## 核心功能模块

### 已完成

- ✅ 项目框架搭建
- ✅ 统一响应结果封装
- ✅ 全局异常处理
- ✅ 跨域配置
- ✅ API 文档集成
- ✅ 基础实体类定义
- ✅ User 实体添加 studentId 学号字段
- ✅ 数据库初始化脚本更新（支持学号字段）

### 待开发

- ⏳ 用户认证与授权（登录、注册、JWT）
- ⏳ 用户模块（个人信息、关注、粉丝）
- ⏳ 赛事模块（比赛、积分榜、球员统计）
- ⏳ 社交模块（帖子、评论、点赞）
- ⏳ 消息通知模块
- ⏳ 文件上传功能

## API 接口规范

### 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1701234567890
}
```

### 状态码说明

- **200**: 操作成功
- **400**: 参数错误
- **401**: 未认证
- **403**: 无权限
- **404**: 资源不存在
- **500**: 服务器错误

## 开发规范

### 代码规范

- 遵循阿里巴巴 Java 开发规范
- 使用 Lombok 简化实体类代码
- Controller 层只做参数校验和转发
- Service 层实现业务逻辑
- 统一使用 RESTful API 设计

### 命名规范

- 包名：全小写，例如 `com.unisport.controller`
- 类名：大驼峰，例如 `UserController`
- 方法名：小驼峰，例如 `getUserById`
- 常量名：全大写下划线分隔，例如 `MAX_SIZE`

## 配合前端项目

本后端项目与前端项目（位于 `../unisport`）配套使用：

- 前端地址: `http://localhost:5173` (Vite 默认端口)
- 后端地址: `http://localhost:8080/api`
- 已配置 CORS 跨域支持

## 参考文档

- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [MyBatis Plus 官方文档](https://baomidou.com/)
- [Knife4j 官方文档](https://doc.xiaominfo.com/)
- [数据库设计文档](../unisport/docs/database-design.md)

## License

Apache License 2.0

## 联系方式

- 项目地址: https://github.com/unisport/unisport-backend
- 问题反馈: [Issues](https://github.com/unisport/unisport-backend/issues)
