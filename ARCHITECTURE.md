# UniSport Backend 架构设计

## 📐 整体架构

UniSport Backend 采用经典的**三层架构**设计模式：

```
┌─────────────────────────────────────────┐
│          Client Layer (前端)             │
│   React + TypeScript + TailwindCSS      │
└────────────────┬────────────────────────┘
                 │ HTTP/REST API
┌────────────────▼────────────────────────┐
│       Controller Layer (控制层)         │
│  - 参数校验                              │
│  - 请求转发                              │
│  - 统一响应封装                          │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│        Service Layer (服务层)           │
│  - 业务逻辑实现                          │
│  - 事务管理                              │
│  - 数据转换                              │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│         Mapper Layer (持久层)           │
│  - MyBatis Plus                         │
│  - SQL 执行                              │
│  - 数据库操作                            │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│      Database Layer (数据库层)          │
│  - MySQL 8.0+                           │
│  - Redis 缓存                            │
└─────────────────────────────────────────┘
```

## 📦 项目结构详解

```
unisport-backend/
├── src/main/java/com/unisport/
│   ├── UnisportApplication.java    # 应用启动类
│   │
│   ├── common/                     # 通用类包
│   │   ├── Result.java            # 统一响应结果
│   │   ├── BusinessException.java # 业务异常
│   │   └── PageResult.java        # 分页结果（待添加）
│   │
│   ├── config/                    # 配置类包
│   │   ├── WebMvcConfig.java     # Web MVC 配置（跨域）
│   │   ├── Knife4jConfig.java    # API 文档配置
│   │   ├── MyMetaObjectHandler.java # MyBatis Plus 自动填充
│   │   ├── GlobalExceptionHandler.java # 全局异常处理
│   │   ├── RedisConfig.java       # Redis 配置（待添加）
│   │   └── JwtConfig.java         # JWT 配置（待添加）
│   │
│   ├── controller/                # 控制层
│   │   ├── SystemController.java # 系统管理
│   │   ├── UserController.java   # 用户管理（待添加）
│   │   ├── PostController.java   # 帖子管理（待添加）
│   │   ├── MatchController.java  # 赛事管理（待添加）
│   │   └── CommentController.java # 评论管理（待添加）
│   │
│   ├── service/                   # 服务层接口
│   │   ├── UserService.java      # 用户服务（待添加）
│   │   ├── PostService.java      # 帖子服务（待添加）
│   │   └── MatchService.java     # 赛事服务（待添加）
│   │
│   ├── service/impl/              # 服务层实现
│   │   ├── UserServiceImpl.java  # 用户服务实现（待添加）
│   │   ├── PostServiceImpl.java  # 帖子服务实现（待添加）
│   │   └── MatchServiceImpl.java # 赛事服务实现（待添加）
│   │
│   ├── mapper/                    # Mapper 接口
│   │   ├── UserMapper.java       # 用户 Mapper（待添加）
│   │   ├── PostMapper.java       # 帖子 Mapper（待添加）
│   │   └── MatchMapper.java      # 赛事 Mapper（待添加）
│   │
│   ├── entity/                    # 实体类
│   │   ├── User.java             # 用户实体
│   │   ├── Category.java         # 分类实体
│   │   ├── Post.java             # 帖子实体
│   │   ├── Comment.java          # 评论实体（待添加）
│   │   ├── Match.java            # 比赛实体（待添加）
│   │   └── Team.java             # 队伍实体（待添加）
│   │
│   ├── dto/                       # 数据传输对象（待添加）
│   │   ├── request/              # 请求 DTO
│   │   │   ├── LoginRequest.java
│   │   │   └── RegisterRequest.java
│   │   └── response/             # 响应 DTO
│   │       ├── UserVO.java
│   │       └── PostVO.java
│   │
│   ├── enums/                     # 枚举类（待添加）
│   │   ├── UserStatus.java       # 用户状态
│   │   ├── MatchStatus.java      # 比赛状态
│   │   └── CategoryType.java     # 分类类型
│   │
│   └── util/                      # 工具类（待添加）
│       ├── JwtUtil.java          # JWT 工具
│       ├── PasswordUtil.java     # 密码工具
│       └── DateUtil.java         # 日期工具
│
└── src/main/resources/
    ├── application.properties     # 应用配置
    ├── mapper/                    # MyBatis XML 文件
    │   ├── UserMapper.xml
    │   ├── PostMapper.xml
    │   └── MatchMapper.xml
    └── sql/
        └── init.sql              # 数据库初始化脚本
```

## 🔧 核心技术选型

### 框架层

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.0 | 核心框架 |
| Spring Web | 6.1.x | Web MVC |
| Spring AOP | 6.1.x | 面向切面编程 |

### 持久层

| 技术 | 版本 | 说明 |
|------|------|------|
| MyBatis Plus | 3.5.5 | ORM 框架增强 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 5.0+ | 缓存数据库 |

### 工具库

| 技术 | 版本 | 说明 |
|------|------|------|
| Lombok | Latest | 简化代码 |
| Hutool | 5.8.23 | Java 工具库 |
| JWT | 0.12.3 | 身份认证 |
| Knife4j | 4.3.0 | API 文档 |

## 🎯 设计模式

### 1. 单例模式

- Spring Bean 默认为单例
- 各 Service、Controller 均为单例

### 2. 工厂模式

- MyBatis Mapper 通过工厂模式创建

### 3. 代理模式

- Spring AOP 动态代理
- MyBatis Mapper 接口代理

### 4. 模板方法模式

- MyBatis Plus BaseMapper
- Spring Boot 启动流程

### 5. 策略模式

- 多种支付方式（待实现）
- 不同登录方式（待实现）

## 🔐 安全设计

### 认证机制

```
用户登录
   ↓
验证账号密码
   ↓
生成 JWT Token
   ↓
返回 Token 给客户端
   ↓
客户端请求携带 Token
   ↓
拦截器验证 Token
   ↓
放行/拒绝
```

### 密码加密

- 使用 BCrypt 算法加密密码
- 每次加密结果不同（加盐）
- 不可逆加密

### 接口权限

- 公开接口：登录、注册
- 需认证接口：发帖、评论、点赞
- 管理员接口：用户管理、内容审核

## 📊 数据流转

### 请求流程

```
1. 前端发起请求
   ↓
2. CORS 跨域处理
   ↓
3. JWT 拦截器验证（可选）
   ↓
4. Controller 接收请求
   ↓
5. 参数校验
   ↓
6. Service 处理业务逻辑
   ↓
7. Mapper 操作数据库
   ↓
8. 数据返回 Service
   ↓
9. 封装统一响应结果
   ↓
10. 返回给前端
```

### 异常处理流程

```
业务异常发生
   ↓
抛出 BusinessException
   ↓
GlobalExceptionHandler 捕获
   ↓
封装统一错误响应
   ↓
返回给前端
```

## 🚀 性能优化

### 数据库优化

- 合理使用索引
- 分页查询
- 批量操作
- 读写分离（待实现）

### 缓存策略

- Redis 缓存热点数据
- 用户信息缓存
- 帖子列表缓存
- 积分榜缓存

### 接口优化

- 分页查询避免全表扫描
- 懒加载关联数据
- 减少数据库查询次数
- 异步处理耗时操作

## 📈 扩展性设计

### 水平扩展

- 无状态设计
- JWT Token 替代 Session
- 可部署多实例

### 功能扩展

- 预留 AI 接口（Gemini）
- 预留支付接口
- 预留消息推送接口
- 预留直播功能接口

## 🧪 测试策略

### 单元测试

- Service 层业务逻辑测试
- Util 工具类测试

### 集成测试

- Controller 接口测试
- Mapper 数据库操作测试

### 性能测试

- JMeter 压力测试
- 数据库慢查询分析

## 📚 最佳实践

### 代码规范

- 遵循阿里巴巴 Java 开发规范
- 使用 Lombok 简化代码
- 统一注释规范
- 合理使用设计模式

### 日志规范

- INFO：业务流程日志
- WARN：警告信息
- ERROR：异常错误
- DEBUG：调试信息

### Git 规范

- feat: 新功能
- fix: 修复 Bug
- docs: 文档更新
- refactor: 代码重构
- test: 测试相关

---

**此架构设计将随项目开发不断完善和优化。**
