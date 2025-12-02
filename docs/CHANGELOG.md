# 更新日志

所有重要的项目变更都会记录在此文件中。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，
并且本项目遵守 [语义化版本](https://semver.org/lang/zh-CN/)。

---

## [1.1.0] - 2025-12-01

### 新增 (Added)

- **User 实体**：添加 `studentId` 学号字段
  - 字段类型：`String`
  - 数据库类型：`VARCHAR(30)`
  - 允许为空：是
  - 位置：`department` 字段之后
  - 说明：用于存储用户的学号信息

### 修改 (Changed)

- **数据库表 users**：
  - 添加 `student_id` 字段（VARCHAR(30), NULL）
  - 添加 `idx_student_id` 索引以优化查询性能

- **数据库初始化脚本** (`init.sql`)：
  - 更新 users 表创建语句，包含 student_id 字段
  - 添加 idx_student_id 索引

### 文档更新 (Documentation)

- 更新 `README.md`，记录学号字段添加
- 创建 `migration_add_student_id.sql` 迁移脚本
- 创建 `CHANGELOG.md` 更新日志

### 技术细节

**影响的文件**：
- `src/main/java/com/unisport/entity/User.java`
- `src/main/resources/sql/init.sql`
- `src/main/resources/sql/migration_add_student_id.sql` (新建)
- `README.md`
- `CHANGELOG.md` (新建)

**数据库迁移**：
```sql
ALTER TABLE `users` 
ADD COLUMN `student_id` VARCHAR(30) COMMENT '学号' AFTER `department`;

ALTER TABLE `users` 
ADD INDEX `idx_student_id` (`student_id`);
```

**实体类变更**：
```java
/**
 * 学号
 */
private String studentId;
```

### 兼容性说明

- ✅ **向后兼容**：学号字段允许为 NULL，不影响已有功能
- ✅ **数据迁移**：已有用户的 student_id 默认为 NULL
- ✅ **前端对接**：需前端同步更新注册和个人信息页面

### 下一步计划

- [ ] 开发用户注册接口（支持学号参数）
- [ ] 开发用户信息更新接口（支持学号修改）
- [ ] 添加学号格式验证逻辑
- [ ] 编写单元测试

---

## [1.0.0] - 2025-11-29

### 新增 (Added)

- 初始化项目基础框架
- 集成 Spring Boot 3.2.0
- 集成 MyBatis Plus 3.5.5
- 集成 Knife4j API 文档
- 统一响应结果封装 (`Result`)
- 全局异常处理 (`GlobalExceptionHandler`)
- 跨域配置 (`WebMvcConfig`)
- 基础实体类定义：
  - `User.java` - 用户实体
  - `Category.java` - 分类实体
  - `Post.java` - 帖子实体
- 系统健康检查接口 (`SystemController`)
- 数据库初始化脚本 (`init.sql`)

### 技术栈

- Spring Boot 3.2.0
- JDK 17
- MyBatis Plus 3.5.5
- MySQL 8.0+
- Redis
- Knife4j 4.3.0
- Hutool 5.8.23
- Lombok

---

**版本说明**：
- **主版本号 (Major)**：不兼容的 API 修改
- **次版本号 (Minor)**：向下兼容的功能性新增
- **修订号 (Patch)**：向下兼容的问题修正
