# UniSport 数据库设计方案

## 数据库概述

本文档为 UniSport 校园体育社交应用设计数据库表结构，采用关系型数据库（推荐使用 MySQL 8.0+ 或 PostgreSQL 13+）。

## 核心设计原则

1. **数据规范化**：遵循第三范式（3NF），减少数据冗余
2. **可扩展性**：预留扩展字段，支持未来功能迭代
3. **性能优化**：合理使用索引，支持高并发查询
4. **数据完整性**：使用外键约束和数据验证
5. **逻辑删除**：核心表（如 users）使用 `deleted` 字段实现软删除，配合 MyBatis Plus 实现自动过滤

---

## 表结构设计

### 1. 用户系统模块

#### 1.0 学校学院管理

##### 1.0.1 schools（学校表）

学校基础信息表，存储所有支持的学校数据。

**使用场景：**
> 🏫 **学校基础数据** - 系统支持的学校信息管理
> 
> **应用场景：**
> 1. **注册选择**：用户注册时选择所在学校
> 2. **学生验证**：学生身份验证时关联学校信息
> 3. **学校管理**：管理端维护学校基础数据
> 4. **数据统计**：按学校统计用户、学生数据
> 5. **学院关联**：作为学院表的外键关联
> 
> **典型操作：**
> - 查询所有学校：SELECT * WHERE status = 1 ORDER BY sort_order
> - 按地区查询：SELECT * WHERE province = ? AND city = ?
> - 学校详情：SELECT * WHERE id = ?

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 学校ID |
| name | VARCHAR(100) | UNIQUE, NOT NULL | 学校名称 |
| code | VARCHAR(20) | UNIQUE, NOT NULL | 学校代码 |
| province | VARCHAR(50) | NULL | 所在省份 |
| city | VARCHAR(50) | NULL | 所在城市 |
| address | VARCHAR(200) | NULL | 详细地址 |
| website | VARCHAR(200) | NULL | 官网 |
| sort_order | INT | DEFAULT 0 | 排序 |
| status | TINYINT | DEFAULT 1 | 状态（1:启用 0:禁用） |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引：**
- PRIMARY KEY (id)
- UNIQUE KEY (name)
- UNIQUE KEY (code)
- INDEX idx_province_city (province, city)
- INDEX idx_status (status)

---

##### 1.0.2 departments（学院/院系表）

学院院系信息表，隶属于具体学校。

**使用场景：**
> 🏛️ **学院基础数据** - 各学校的学院/院系信息管理
> 
> **应用场景：**
> 1. **注册选择**：用户注册时选择所在学院
> 2. **学生验证**：学生身份验证时关联学院信息
> 3. **层级查询**：根据学校查询其下属学院
> 4. **数据统计**：按学院统计用户、学生数据
> 5. **院系管理**：管理端维护学院基础数据
> 
> **典型操作：**
> - 查询学校学院：SELECT * WHERE school_id = ? AND status = 1 ORDER BY sort_order
> - 学院详情：SELECT * WHERE id = ?
> - 按代码查询：SELECT * WHERE school_id = ? AND code = ?

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 学院ID |
| school_id | BIGINT | NOT NULL | 学校ID |
| name | VARCHAR(100) | NOT NULL | 学院名称 |
| code | VARCHAR(20) | NULL | 学院代码 |
| description | TEXT | NULL | 学院简介 |
| sort_order | INT | DEFAULT 0 | 排序 |
| status | TINYINT | DEFAULT 1 | 状态（1:启用 0:禁用） |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引：**
- PRIMARY KEY (id)
- UNIQUE KEY uk_school_name (school_id, name)
- INDEX idx_school (school_id)
- INDEX idx_status (status)

**外键：**
- FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE

---

##### 1.0.3 students（学生信息表）

学生基础信息表，用于学生身份验证（软绑定）。

**使用场景：**
> 🎓 **学生身份验证** - 注册时基于学号的学生身份验证
> 
> **应用场景：**
> 1. **注册验证**：用户注册时查询学号是否存在于学生表，验证学生身份
> 2. **软绑定机制**：学号一致即认定为同一人，完成用户与学生的关联
> 3. **学生管理**：管理端维护学生基础数据（由学校管理员录入）
> 4. **身份校验**：防止非本校学生注册，保证用户身份真实性
> 5. **数据查询**：按学校、学院、年级等维度查询学生
> 6. **统计分析**：学生数据统计和分析
> 
> **注册流程说明：**
> - 用户填写注册信息（包括学号、学校、学院）
> - 后端查询 students 表：`SELECT * WHERE student_id = ? AND school_id = ? AND department_id = ?`
> - 如果查询到记录，验证通过，允许注册（用户表的 student_id 字段记录学号）
> - 如果查询不到，注册失败，提示"该学号不存在或学校/学院信息不匹配"
> 
> **设计特点：**
> - **管理员维护**：学生数据由学校管理员通过管理端批量导入或录入
> - **软绑定**：通过学号关联，不建立users表与students表的外键约束
> - **独立管理**：学生表与用户表独立，便于数据管理和维护
> 
> **典型操作：**
> - 验证学号：SELECT * WHERE student_id = ? AND school_id = ? AND department_id = ? AND status = 1
> - 按学校查询：SELECT * WHERE school_id = ? ORDER BY grade DESC, student_id
> - 按学院查询：SELECT * WHERE department_id = ?
> - 按年级查询：SELECT * WHERE grade = ? ORDER BY student_id
> - 批量导入：INSERT INTO students (student_id, name, ...) VALUES (...)
> - 更新状态：UPDATE status = 0 WHERE student_id = ? （学生毕业/离校）

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键ID |
| student_id | VARCHAR(30) | UNIQUE, NOT NULL | 学号（唯一标识） |
| name | VARCHAR(50) | NOT NULL | 学生姓名 |
| school_id | BIGINT | NOT NULL | 学校ID |
| department_id | BIGINT | NOT NULL | 学院ID |
| grade | INT | NULL | 年级（如2024） |
| major | VARCHAR(100) | NULL | 专业 |
| class_name | VARCHAR(50) | NULL | 班级 |
| gender | INT | NULL | 性别（0:女性 1:男性） |
| phone | VARCHAR(20) | NULL | 手机号 |
| email | VARCHAR(100) | NULL | 邮箱 |
| status | TINYINT | DEFAULT 1 | 状态（1:在校 0:毕业/离校） |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引：**
- PRIMARY KEY (id)
- UNIQUE KEY (student_id)
- INDEX idx_school_dept (school_id, department_id)
- INDEX idx_student_id (student_id)
- INDEX idx_name (name)
- INDEX idx_grade (grade)
- INDEX idx_status (status)

**外键：**
- FOREIGN KEY (school_id) REFERENCES schools(id)
- FOREIGN KEY (department_id) REFERENCES departments(id)

---

#### 1.1 users（用户表）

用户基础信息表，存储所有用户的账号和个人资料。

**使用场景：**
> 👤 **用户身份核心** - 所有用户相关功能的基础数据表
> 
> **应用场景：**
> 1. **用户注册**：新用户通过学号或手机号注册，创建账号并设置个人信息
> 2. **用户登录**：验证 account 和 password，生成登录凭证（JWT Token）
> 3. **个人主页**：展示用户的昵称、头像、个人简介等信息，学校和学院信息从 user_educations 表获取
> 4. **编辑资料**：用户在"编辑个人资料"页面修改昵称、头像、简介等
> 5. **用户列表**：管理端查看所有用户
> 6. **账号管理**：管理员可以禁用/启用用户账号（status 字段）
> 7. **社交功能基础**：发帖、评论、点赞等所有操作都需要关联 user_id
> 8. **教育经历关联**：用户的学校、学院信息通过 user_educations 表维护
> 
> **典型操作：**
> - 注册：INSERT 新用户记录，密码使用 bcrypt 加密
> - 登录：SELECT WHERE account = ? 验证密码
> - 查询用户：SELECT WHERE id = ? 获取用户详情
> - 更新资料：UPDATE 昵称、头像、bio等字段
> - 学号查询：SELECT WHERE student_id = ? 用于验证学号唯一性
> - 获取用户学校：JOIN user_educations WHERE is_primary = 1

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 用户ID |
| account | VARCHAR(50) | UNIQUE, NOT NULL | 账号（学号/手机号） |
| password | VARCHAR(255) | NOT NULL | 密码（加密存储） |
| nickname | VARCHAR(50) | NOT NULL | 昵称 |
| avatar | VARCHAR(500) | NULL | 头像URL |
| student_id | VARCHAR(30) | NULL | 学号 |
| bio | VARCHAR(500) | NULL | 个人简介 |
| gender | INT | NULL | 性别（0:女性 1:男性） |
| status | TINYINT | DEFAULT 1 | 账号状态（1:正常 0:禁用） |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引：**
- PRIMARY KEY (id)
- UNIQUE KEY (account)
- INDEX idx_student_id (student_id)

---

#### 1.2 user_follows（关注关系表）

用户之间的关注关系。

**使用场景：**
> 🤝 **社交关系链** - 用户之间的关注与被关注关系管理
> 
> **应用场景：**
> 1. **关注用户**：用户在他人主页点击"关注"按钮，建立关注关系
> 2. **取消关注**：再次点击取消关注，删除关注记录
> 3. **关注列表**：个人中心查看"我的关注"，显示所有关注的用户
> 4. **粉丝列表**：查看"我的粉丝"，显示关注自己的用户
> 5. **关注状态**：判断当前用户是否已关注某人，控制按钮显示
> 6. **关注动态**：优先展示关注用户的帖子和动态（个性化推荐）
> 7. **互相关注**：判断是否互相关注，展示"好友"标识
> 
> **设计特点：**
> - **唯一约束**：uk_follow 确保不能重复关注同一用户
> - **双向索引**：idx_follower 和 idx_following 分别优化关注列表和粉丝列表查询
> - **级联删除**：用户删除时，相关关注关系自动清除
> 
> **典型操作：**
> - 关注：INSERT INTO user_follows (follower_id, following_id)
> - 取消关注：DELETE WHERE follower_id = ? AND following_id = ?
> - 我的关注：SELECT following_id WHERE follower_id = ?
> - 我的粉丝：SELECT follower_id WHERE following_id = ?
> - 判断关注状态：SELECT COUNT(*) WHERE follower_id = ? AND following_id = ?
> - 互关判断：查询双向关注记录

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 记录ID |
| follower_id | BIGINT | NOT NULL | 关注者ID |
| following_id | BIGINT | NOT NULL | 被关注者ID |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 关注时间 |

**索引：**
- PRIMARY KEY (id)
- UNIQUE KEY uk_follow (follower_id, following_id)
- INDEX idx_follower (follower_id)
- INDEX idx_following (following_id)

**外键：**
- FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE
- FOREIGN KEY (following_id) REFERENCES users(id) ON DELETE CASCADE

---

#### 1.3 user_educations（用户教育经历表）

用户教育背景信息表，记录用户的所有教育经历（支持多个学校）。

**使用场景：**
> 🎓 **教育背景管理** - 用户多阶段教育经历的完整记录
> 
> **应用场景：**
> 1. **添加教育经历**：用户在个人信息页面添加新的教育经历（本科、研究生等）
> 2. **升学管理**：支持用户升学后添加新学校信息，保留完整教育轨迹
> 3. **教育背景展示**：个人主页展示用户的完整教育背景
> 4. **学号验证**：添加教育经历时验证学号真实性（基于students表）
> 5. **主教育经历标识**：is_primary字段标识用户当前主要的教育经历
> 6. **验证状态跟踪**：status字段记录学号验证状态（待验证/已验证/验证失败）
> 7. **教育经历管理**：用户可以查看、添加、删除自己的教育经历
> 
> **设计特点：**
> - **多教育经历支持**：一个用户可以有多条教育经历记录
> - **学号验证机制**：类似注册时的验证逻辑，确保教育信息真实性
> - **主教育经历**：通过is_primary标识用户当前就读的学校
> - **验证状态管理**：区分不同验证状态，支持异步验证
> - **软关联设计**：与students表软关联，不建立外键约束
> 
> **验证流程：**
> 1. 用户填写教育经历（学校、院系、学号、起止时间）
> 2. 后端查询students表验证学号：
>    ```sql
>    SELECT * FROM students 
>    WHERE student_id = ? 
>      AND school_id = ? 
>      AND department_id = ? 
>      AND status = 1
>    ```
> 3. 验证通过：status设为"verified"，允许添加
> 4. 验证失败：status设为"failed"或直接拒绝添加
> 
> **典型操作：**
> - 添加教育经历：INSERT 新记录，验证学号后设置status
> - 查询用户教育经历：SELECT WHERE user_id = ? ORDER BY is_primary DESC, created_at DESC
> - 设置主教育经历：UPDATE is_primary = 1，同时将其他记录的is_primary设为0
> - 删除教育经历：DELETE WHERE id = ? AND user_id = ?
> - 验证学号：查询students表确认学号存在且匹配
> - 查询主教育经历：SELECT WHERE user_id = ? AND is_primary = 1

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 教育经历ID |
| user_id | BIGINT | NOT NULL | 用户ID |
| school_id | BIGINT | NOT NULL | 学校ID |
| department_id | BIGINT | NOT NULL | 学院ID |
| student_id | VARCHAR(30) | NOT NULL | 学号 |
| start_date | VARCHAR(10) | NULL | 开始时间（格式：YYYY-MM） |
| end_date | VARCHAR(10) | NULL | 结束时间（NULL表示至今） |
| is_primary | TINYINT | DEFAULT 0 | 是否为主要教育经历（1:是 0:否） |
| status | ENUM('pending','verified','failed') | DEFAULT 'pending' | 验证状态 |
| deleted | TINYINT | DEFAULT 0 | 逻辑删除（0:未删除 1:已删除，MyBatis Plus） |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引：**
- PRIMARY KEY (id)
- INDEX idx_user (user_id, is_primary)
- INDEX idx_school_dept (school_id, department_id)
- INDEX idx_student_id (student_id)
- INDEX idx_status (status)
- INDEX idx_deleted (deleted)
- UNIQUE KEY uk_user_school_student (user_id, school_id, student_id)

**外键：**
- FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
- FOREIGN KEY (school_id) REFERENCES schools(id)
- FOREIGN KEY (department_id) REFERENCES departments(id)

**业务规则：**
1. 每个用户只能有一个is_primary=1的教育经历
2. 同一用户不能添加相同的学校+学号组合（唯一约束）
3. 添加教育经历时必须通过students表验证学号
4. 删除教育经历时，如果是主教育经历，需提示用户设置新的主教育经历
5. status字段用于标识验证状态，可用于后台异步验证场景

---

### 2. 赛事模块

#### 2.1 categories（运动分类表）

运动项目分类。

**使用场景：**
> 🏆 **分类基础数据** - 所有运动项目的分类字典表
> 
> **应用场景：**
> 1. **导航栏分类**：首页顶部导航栏展示所有运动分类（足球/篮球/羽毛球/乒乓球/健身）
> 2. **内容筛选**：用户点击分类，过滤显示该分类下的赛事和帖子
> 3. **发帖选择**：用户发帖时选择运动分类标签
> 4. **赛事组织**：创建联赛、队伍时选择所属运动分类
> 5. **图标展示**：各分类配有 emoji 图标，增强视觉识别
> 6. **分类管理**：管理员可以启用/禁用分类，调整排序顺序
> 
> **设计特点：**
> - **基础数据**：系统初始化时预置 5 个分类
> - **代码唯一**：code 字段用于程序内部识别，不可重复
> - **排序控制**：sort_order 控制分类在界面上的显示顺序
> 
> **典型操作：**
> - 查询全部：SELECT * WHERE status = 1 ORDER BY sort_order
> - 按code查询：SELECT * WHERE code = 'football'
> - 初始化数据：INSERT 5个预置分类

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | 分类ID |
| code | VARCHAR(20) | UNIQUE, NOT NULL | 分类代码（football/basketball等） |
| name | VARCHAR(50) | NOT NULL | 分类名称（足球/篮球等） |
| icon | VARCHAR(10) | NULL | 图标 emoji |
| sort_order | INT | DEFAULT 0 | 排序顺序 |
| status | TINYINT | DEFAULT 1 | 状态（1:启用 0:禁用） |

**索引：**
- PRIMARY KEY (id)
- UNIQUE KEY (code)

---

#### 2.2 leagues（联赛表）

赛事联赛信息。

**使用场景：**
> 🏆 **赛事组织管理** - 各类联赛和赛事的基础信息
> 
> **应用场景：**
> 1. **联赛列表**：展示当前进行中的各类联赛（新生杯、院系联赛等）
> 2. **赛事筛选**：按运动分类、赛季年份、学校筛选联赛
> 3. **联赛详情**：查看联赛的名称、描述、起止日期等信息
> 4. **比赛关联**：所有比赛都必须关联到某个联赛
> 5. **积分榜组织**：每个联赛都有对应的积分榜和球员统计
> 6. **历史赛事**：查询往年的联赛信息和成绩
> 7. **赛事状态**：区分进行中和已结束的联赛
> 8. **学校联赛**：每个联赛归属于一个学校，不支持跨校联赛
> 9. **年度区分**：同一学校每年举办的联赛（如2024新生杯、2025新生杯）视为不同联赛实体
> 
> **典型操作：**
> - 创建联赛：INSERT 新联赛记录（必须指定学校）
> - 当前赛季：SELECT WHERE category_id = ? AND year = 2025 AND status = 1
> - 学校联赛：SELECT WHERE school_id = ? AND year = 2025
> - 历史赛事：SELECT WHERE category_id = ? ORDER BY year DESC
> - 更新状态：UPDATE status = 0 当赛事结束

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 联赛ID |
| category_id | INT | NOT NULL | 运动分类ID |
| school_id | BIGINT | NOT NULL | 学校ID（联赛所属学校，软关联） |
| name | VARCHAR(100) | NOT NULL | 联赛名称（如"新生杯"） |
| year | INT | NOT NULL | 赛季年份 |
| description | TEXT | NULL | 联赛描述 |
| start_date | DATE | NULL | 开始日期 |
| end_date | DATE | NULL | 结束日期 |
| status | TINYINT | DEFAULT 1 | 状态（1:进行中 0:已结束） |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引：**
- PRIMARY KEY (id)
- INDEX idx_category_year (category_id, year)
- INDEX idx_school_year (school_id, year)

**外键：**
- FOREIGN KEY (category_id) REFERENCES categories(id)

**设计说明：**
> ⚠️ **软连接设计**：school_id 字段不设置外键约束，仅通过业务逻辑保证数据一致性。
> 
> **原因：**
> - 避免因物理外键导致的数据插入失败
> - 提高数据操作的灵活性
> - 业务层负责维护 school_id 的有效性

---

#### 2.3 teams（队伍表）

参赛队伍信息（适用于团队项目）。

**使用场景：**
> 👥 **队伍管理** - 团队项目的队伍基础信息
> 
> **应用场景：**
> 1. **队伍列表**：展示某运动分类下的所有参赛队伍（如足球队、篮球队）
> 2. **队伍详情**：查看队伍的名称、队徽、院系、介绍等信息
> 3. **比赛对阵**：团队比赛中的双方队伍信息
> 4. **队伍成员**：通过 team_members 表查看队伍的球员名单
> 5. **院系代表队**：按 department 字段组织院系之间的比赛
> 6. **队长信息**：关联 captain_id 展示队长信息（可选）
> 7. **联赛队伍**：每个队伍只能参加一个联赛，通过 league_id 关联
> 8. **年度区分**：每年的队伍需要重新创建（即使名称相同），如2024计算机队、2025计算机队是两个不同队伍
> 
> **设计特点：**
> - **适用范围**：仅用于团队项目（足球、篮球），个人项目不使用
> - **队长可选**：captain_id 为 NULL，不强制关联用户（软连接设计）
> - **联赛绑定**：每个队伍必须关联到一个联赛，不同年份的联赛创建不同队伍
> - **数据冗余**：保留 category_id 字段，避免多表关联，提高查询性能
> 
> **典型操作：**
> - 创建队伍：INSERT 新队伍记录（必须指定联赛）
> - 按分类查询：SELECT WHERE category_id = ?
> - 按联赛查询：SELECT WHERE league_id = ?
> - 按院系查询：SELECT WHERE department = '计算机系'
> - 更新队伍信息：UPDATE 队名、队徽、介绍等

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 队伍ID |
| category_id | INT | NOT NULL | 运动分类ID（冗余字段，便于查询） |
| league_id | BIGINT | NOT NULL | 联赛ID（队伍参加的联赛，软关联） |
| name | VARCHAR(100) | NOT NULL | 队伍名称 |
| logo | VARCHAR(500) | NULL | 队徽URL |
| captain_id | BIGINT | NULL | 队长用户ID（软关联） |
| department | VARCHAR(100) | NULL | 所属院系 |
| description | TEXT | NULL | 队伍介绍 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引：**
- PRIMARY KEY (id)
- INDEX idx_category (category_id)
- INDEX idx_league (league_id)
- INDEX idx_captain (captain_id)

**外键：**
- FOREIGN KEY (category_id) REFERENCES categories(id)

**设计说明：**
> ⚠️ **软连接设计**：league_id 和 captain_id 字段不设置外键约束，仅通过业务逻辑保证数据一致性。
> 
> **原因：**
> - 避免因物理外键导致的数据插入失败
> - 提高数据操作的灵活性
> - 业务层负责维护 league_id 和 captain_id 的有效性
> 
> **业务规则：**
> - category_id 必须与 league_id 关联的联赛的 category_id 保持一致（业务层校验）
> - 每年创建新联赛时，需为该联赛创建新的队伍记录（即使队伍名称相同）

---

#### 2.4 team_members（队伍成员表）

队伍成员关系。

**使用场景：**
> 🎽 **球员名单管理** - 团队项目中的球员信息存储
> 
> **应用场景：**
> 1. **队伍阵容**：展示某支队伍的完整球员名单
> 2. **球员信息**：显示球员姓名、球衣号码、场上位置
> 3. **比赛阵容**：比赛详情页展示双方队伍的首发阵容和替补名单
> 4. **比赛事件**：进球、黄牌等事件关联到具体球员
> 5. **球员统计**：统计球员的进球数、出场次数等数据
> 6. **管理员维护**：管理员通过管理端录入和更新球员信息
> 7. **可选用户关联**：如果球员也是系统用户，可以关联 user_id（仅供数据关联）
> 
> **设计特点：**
> - **独立数据**：球员与用户是独立概念，由管理员统一录入
> - **用户角色一致**：应用中不区分球员和普通用户
> - **只读展示**：用户端仅能查看球员信息，不能修改
> 
> **典型操作：**
> - 添加球员：INSERT 新球员记录（管理员操作）
> - 队伍名单：SELECT WHERE team_id = ? ORDER BY jersey_number
> - 球员详情：SELECT WHERE id = ?
> - 按位置查询：SELECT WHERE team_id = ? AND position = 'FW'
> - 更新信息：UPDATE 球衣号、位置等（管理员操作）

**设计说明：**

> ⚠️ **重要**: 队伍成员（球员）与应用用户是相互独立的概念。
> 
> - **数据来源**: 队伍成员信息由学校管理人员通过管理端直接录入数据库，不是由用户自主注册加入。
> - **用户角色**: 应用中所有用户角色相同，没有"球员"和"普通用户"的区别。
> - **关联关系**: 虽然 `user_id` 字段可以关联到 users 表（用于某个用户恰好是某队球员的情况），但这仅用于数据关联，并不影响用户在应用中的权限和功能。
> - **数据管理**: 球队阵容、成员信息由管理员统一维护，用户端仅能查看，不能修改。

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 记录ID |
| team_id | BIGINT | NOT NULL | 队伍ID |
| user_id | BIGINT | NULL | 用户ID（可选关联，仅用于数据关联） |
| player_name | VARCHAR(50) | NOT NULL | 球员姓名（管理员录入） |
| jersey_number | INT | NULL | 球衣号码 |
| position | VARCHAR(20) | NULL | 位置（FW/MF/GK等） |
| joined_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 加入时间 |

**索引：**
- PRIMARY KEY (id)
- UNIQUE KEY uk_team_user (team_id, user_id)
- INDEX idx_user (user_id)
- INDEX idx_team (team_id)

**外键：**
- FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE
- FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL

---

#### 2.5 matches（比赛表）

赛事比赛信息。

**使用场景：**
> ⚽ **比赛数据中心** - 所有比赛的详细信息和结果
> 
> **应用场景：**
> 1. **赛事列表**：首页展示最近的比赛（未开始/进行中/已结束）
> 2. **比赛详情**：查看比赛的双方队伍、比分、时间、地点等
> 3. **实时比分**：进行中的比赛实时更新 score_a 和 score_b
> 4. **比赛筛选**：按运动分类、联赛、状态筛选比赛
> 5. **团队比赛**：使用 team_a_id 和 team_b_id 关联队伍表
> 6. **个人比赛**：使用 player_a_id 和 player_b_id 关联 team_members 表
> 7. **比赛日程**：按 match_time 排序展示比赛日程表
> 8. **积分计算**：根据比赛结果更新积分榜
> 9. **学校比赛**：每场比赛归属于一个学校，不支持跨校比赛
> 10. **按学校筛选**：用户可以查看特定学校的比赛列表
> 
> **设计特点：**
> - **团体/个人兼容**：支持团队项目和个人项目两种比赛形式
> - **名称冗余**：保留 team_a_name 和 team_b_name 方便显示
> - **状态跟踪**：实时更新比赛状态（upcoming/live/finished）
> - **学校关联**：每场比赛必须关联到一个学校
> 
> **典型操作：**
> - 创建比赛：INSERT 新比赛记录（必须指定学校）
> - 最近比赛：SELECT WHERE category_id = ? ORDER BY match_time DESC LIMIT 10
> - 进行中比赛：SELECT WHERE status = 'live'
> - 更新比分：UPDATE score_a = ?, score_b = ?
> - 更新状态：UPDATE status = 'finished'
> - 按联赛查询：SELECT WHERE league_id = ? ORDER BY match_time
> - 按学校查询：SELECT WHERE school_id = ? AND category_id = ? ORDER BY match_time

**设计说明：**
> - **团队项目**：使用 `team_a_id` 和 `team_b_id` 关联到 teams 表
> - **个人项目**：使用 `player_a_id` 和 `player_b_id` 关联到 `team_members` 表（球员由管理员录入）
> - 为了显示方便，同时保留 `team_a_name` 和 `team_b_name` 字段存储名称
> 
> ⚠️ **软连接设计**：school_id 字段不设置外键约束，仅通过业务逻辑保证数据一致性。
> 
> **原因：**
> - 避免因物理外键导致的数据插入失败
> - 提高数据操作的灵活性
> - 业务层负责维护 school_id 的有效性
> 
> **业务规则：**
> 1. school_id 必须与 league_id 关联的联赛的 school_id 保持一致（业务层校验）
> 2. 创建比赛时，school_id 从关联的联赛自动获取
> 3. 不同学校的联赛不能创建跨校比赛

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 比赛ID |
| league_id | BIGINT | NOT NULL | 联赛ID |
| category_id | INT | NOT NULL | 运动分类ID |
| school_id | BIGINT | NOT NULL | 学校ID（比赛所属学校，软关联） |
| team_a_id | BIGINT | NULL | A队ID（团队项目） |
| team_b_id | BIGINT | NULL | B队ID（团队项目） |
| player_a_id | BIGINT | NULL | A选手ID（个人项目，关联team_members表） |
| player_b_id | BIGINT | NULL | B选手ID（个人项目，关联team_members表） |
| team_a_name | VARCHAR(100) | NOT NULL | A方名称 |
| team_b_name | VARCHAR(100) | NOT NULL | B方名称 |
| score_a | INT | NULL | A方得分 |
| score_b | INT | NULL | B方得分 |
| status | ENUM('upcoming','live','finished') | DEFAULT 'upcoming' | 比赛状态 |
| match_time | DATETIME | NOT NULL | 比赛时间 |
| location | VARCHAR(200) | NOT NULL | 比赛地点 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引：**
- PRIMARY KEY (id)
- INDEX idx_league (league_id)
- INDEX idx_category_time (category_id, match_time)
- INDEX idx_school_time (school_id, match_time)
- INDEX idx_status (status)

**外键：**
- FOREIGN KEY (league_id) REFERENCES leagues(id)
- FOREIGN KEY (category_id) REFERENCES categories(id)
- FOREIGN KEY (team_a_id) REFERENCES teams(id) ON DELETE SET NULL
- FOREIGN KEY (team_b_id) REFERENCES teams(id) ON DELETE SET NULL
- FOREIGN KEY (player_a_id) REFERENCES team_members(id) ON DELETE SET NULL
- FOREIGN KEY (player_b_id) REFERENCES team_members(id) ON DELETE SET NULL

---

#### 2.6 match_events（比赛事件表）

比赛进程中的事件记录（进球、黄牌等）。

**使用场景：**
> 📝 **比赛实况记录** - 比赛过程中的关键事件记录
> 
> **应用场景：**
> 1. **比赛直播**：实时记录和展示比赛事件（进球、黄牌、红牌等）
> 2. **比赛回放**：比赛结束后查看完整的比赛过程
> 3. **事件时间轴**：按 minute 字段排序展示比赛时间轴
> 4. **球员事件**：记录哪个球员进球、被罚牌、被换下等
> 5. **比赛统计**：统计比赛的进球数、牌数等数据
> 6. **球员数据**：统计球员的进球数、黄牌数等
> 7. **比赛高光**：筛选进球事件生成比赛高光集锦
> 
> **事件类型：**
> - **goal**：进球事件
> - **yellow_card**：黄牌警告
> - **red_card**：红牌罚下
> - **substitution**：球员替换
> - **whistle**：哨声（开始/结束）
> - **other**：其他事件
> 
> **典型操作：**
> - 记录事件：INSERT 新事件（实时记录）
> - 比赛事件列表：SELECT WHERE match_id = ? ORDER BY minute
> - 进球记录：SELECT WHERE match_id = ? AND event_type = 'goal'
> - 球员事件：SELECT WHERE player_id = ? AND event_type = 'goal'
> - 删除事件：DELETE WHERE id = ?（记录错误时）

**设计说明：**
> `player_id` 字段关联到 `team_members` 表，指向实际参赛的球员（由管理员录入的队伍成员），而非应用用户。

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 事件ID |
| match_id | BIGINT | NOT NULL | 比赛ID |
| event_type | ENUM('goal','yellow_card','red_card','substitution','whistle','other') | NOT NULL | 事件类型 |
| minute | INT | NOT NULL | 发生时间（分钟） |
| team_side | ENUM('A','B','neutral') | NOT NULL | 相关队伍 |
| player_id | BIGINT | NULL | 相关球员ID（关联team_members表） |
| description | VARCHAR(500) | NOT NULL | 事件描述 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引：**
- PRIMARY KEY (id)
- INDEX idx_match (match_id)
- INDEX idx_player (player_id)

**外键：**
- FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE CASCADE
- FOREIGN KEY (player_id) REFERENCES team_members(id) ON DELETE SET NULL

---

#### 2.7 standings（积分榜表）

联赛积分榜数据。

**使用场景：**
> 🏆 **排名榜单** - 各联赛的积分排名和成绩统计
> 
> **应用场景：**
> 1. **积分榜展示**：在"积分榜"页面展示联赛排名
> 2. **团队积分榜**：团队项目使用 team_id 关联队伍
> 3. **个人积分榜**：个人项目使用 user_id 关联用户
> 4. **排名更新**：比赛结束后自动更新积分和排名
> 5. **成绩统计**：显示已赛场次、胜/平/负场次、积分
> 6. **历史排名**：查询历史联赛的积分榜数据
> 7. **实时排名**：每场比赛后重新计算排名
> 
> **计分规则：**
> - 胜场：3分
> - 平局：1分
> - 负场：0分
> 
> **典型操作：**
> - 查询榜单：SELECT WHERE league_id = ? ORDER BY team_rank
> - 更新成绩：UPDATE played += 1, won += 1, points += 3
> - 重算排名：UPDATE team_rank 根据 points 重新排序
> - 团队排名：SELECT WHERE league_id = ? AND team_id IS NOT NULL
> - 个人排名：SELECT WHERE league_id = ? AND user_id IS NOT NULL

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 记录ID |
| league_id | BIGINT | NOT NULL | 联赛ID |
| team_id | BIGINT | NULL | 队伍ID（团队项目） |
| user_id | BIGINT | NULL | 用户ID（个人项目） |
| team_rank | INT | NOT NULL | 排名 |
| played | INT | DEFAULT 0 | 已赛场次 |
| won | INT | DEFAULT 0 | 胜场 |
| drawn | INT | DEFAULT 0 | 平局 |
| lost | INT | DEFAULT 0 | 负场 |
| points | INT | DEFAULT 0 | 积分 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引：**
- PRIMARY KEY (id)
- UNIQUE KEY uk_league_team (league_id, team_id)
- UNIQUE KEY uk_league_user (league_id, user_id)
- INDEX idx_rank (league_id, team_rank)

**外键：**
- FOREIGN KEY (league_id) REFERENCES leagues(id) ON DELETE CASCADE
- FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE
- FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE

---

#### 2.8 player_stats（球员统计表）

球员个人数据统计。

**使用场景：**
> 🎯 **球员数据榜** - 球员个人技术统计和排名
> 
> **应用场景：**
> 1. **射手榜**：展示联赛进球数最多的球员（足球/篮球等）
> 2. **球员排名**：按 value（进球/得分）排序展示排行榜
> 3. **个人统计**：查看球员的出场次数、进球数、助攻数
> 4. **数据更新**：比赛结束后根据比赛事件更新统计数据
> 5. **队伍统计**：查询某队伍所有球员的统计数据
> 6. **跨联赛比较**：比较同一球员在不同联赛的表现
> 7. **MVP 评选**：根据综合数据评选最佳球员
> 
> **统计数据：**
> - **played**：出场次数
> - **stat_value**：主要统计值（足球进球/篮球得分）
> - **assists**：助攻数
> - **player_rank**：在联赛中的排名
> 
> **典型操作：**
> - 射手榜：SELECT WHERE league_id = ? ORDER BY stat_value DESC LIMIT 10
> - 更新数据：UPDATE played += 1, stat_value += 1 比赛后自动更新
> - 球员详情：SELECT WHERE team_member_id = ? AND league_id = ?
> - 队伍统计：SELECT WHERE team_id = ? AND league_id = ?
> - 重算排名：UPDATE player_rank 根据 stat_value 重新排序

**设计说明：**
> 统计数据基于 `team_members` 表中的球员，而非应用用户。`team_member_id` 关联到实际参赛球员。

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 记录ID |
| league_id | BIGINT | NOT NULL | 联赛ID |
| team_member_id | BIGINT | NOT NULL | 队伍成员ID（关联team_members表） |
| team_id | BIGINT | NULL | 所属队伍ID |
| played | INT | DEFAULT 0 | 出场次数 |
| stat_value | INT | DEFAULT 0 | 统计值（进球/得分等） |
| assists | INT | DEFAULT 0 | 助攻数 |
| player_rank | INT | NULL | 排名 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引：**
- PRIMARY KEY (id)
- UNIQUE KEY uk_league_member (league_id, team_member_id)
- INDEX idx_rank (league_id, player_rank)
- INDEX idx_value (league_id, stat_value DESC)

**外键：**
- FOREIGN KEY (league_id) REFERENCES leagues(id) ON DELETE CASCADE
- FOREIGN KEY (team_member_id) REFERENCES team_members(id) ON DELETE CASCADE
- FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE SET NULL

---

### 3. 社交模块

#### 3.1 posts（帖子表）

用户发布的动态帖子。

**使用场景：**
> 📱 **核心社交功能** - 用户分享运动动态的主要载体
> 
> **应用场景：**
> 1. **发布动态**：用户在首页点击"发帖"按钮，分享运动心得、比赛感想、训练打卡等内容，发帖时通过user_educations关联学校
> 2. **动态流展示**：首页按运动分类（足球/篮球/羽毛球等）展示相关帖子列表，仅显示当前用户所在学校的帖子
> 3. **个人主页**：在"我的发帖"页面查看自己发布的所有动态
> 4. **内容分类**：根据 category_id 筛选特定运动类型的帖子
> 5. **热门内容**：根据 likes_count 和 comments_count 推荐热门动态
> 6. **时间排序**：按 created_at 倒序显示最新动态
> 7. **学校隔离**：用户只能查看当前就读学校的帖子，通过user_educations的主教育经历(is_primary=1)判断
> 
> **典型操作：**
> - 创建帖子：INSERT 新记录，支持文字 + 多图，school_id从user_educations的主教育经历获取
> - 查询动态流：SELECT WHERE category_id = ? AND school_id = ? AND deleted = 0 ORDER BY created_at DESC
> - 按学校查询：SELECT WHERE school_id = ? AND deleted = 0 ORDER BY created_at DESC
> - 更新互动数：UPDATE likes_count/comments_count（触发器自动更新）
> - 删除帖子：UPDATE deleted = 1（逻辑删除，MyBatis Plus自动过滤）

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 帖子ID |
| user_id | BIGINT | NOT NULL | 发布用户ID |
| category_id | INT | NOT NULL | 运动分类ID |
| school_id | BIGINT | NOT NULL | 学校ID（发帖时从users.school_id获取，软关联） |
| content | TEXT | NOT NULL | 帖子内容 |
| images | JSON | NULL | 图片URL数组（支持多图） |
| likes_count | INT | DEFAULT 0 | 点赞数 |
| comments_count | INT | DEFAULT 0 | 评论数 |
| deleted | TINYINT | DEFAULT 0 | 逻辑删除（0:未删除 1:已删除，MyBatis Plus） |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引：**
- PRIMARY KEY (id)
- INDEX idx_user (user_id)
- INDEX idx_category_time (category_id, created_at DESC)
- INDEX idx_school_time (school_id, created_at DESC)
- INDEX idx_created (created_at DESC)
- INDEX idx_deleted (deleted)

**外键：**
- FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
- FOREIGN KEY (category_id) REFERENCES categories(id)

**设计说明：**
> ⚠️ **软连接设计**：school_id 字段不设置外键约束，仅通过业务逻辑保证数据一致性。
> 
> **业务规则：**
> 1. 发帖时，school_id 自动从用户主教育经历（user_educations.is_primary=1）的 school_id 获取，用户不可见也不可修改
> 2. 用户切换学校后（设置新的主教育经历），旧帖子的 school_id 保持不变
> 3. 用户只能查看当前主教育经历学校匹配的帖子，切换学校后无法看到旧学校的帖子（包括自己的）
> 4. 如果用户没有主教育经历（user_educations.is_primary=1不存在），禁止发帖
> 5. 已毕业用户的帖子不删除，同校在读学生仍可查看
> 6. 应用层在发帖时强制验证用户有主教育经历且学校存在于 schools 表中

---

#### 3.2 comments（评论表）

帖子评论。

**使用场景：**
> 💬 **互动交流功能** - 用户对帖子进行评论和回复
> 
> **应用场景：**
> 1. **评论帖子**：用户在帖子详情页发表评论，表达观点或互动交流
> 2. **回复评论**：通过 parent_id 实现评论的回复功能（楼中楼）
> 3. **评论列表**：帖子详情页展示所有评论，按时间正序或倒序排列
> 4. **我的评论**：个人中心查看自己在所有帖子下的评论记录
> 5. **评论互动**：支持对评论点赞，通过 likes_count 展示热门评论
> 6. **通知触发**：新评论触发通知，提醒帖子作者或被回复者
> 
> **典型操作：**
> - 发表评论：INSERT 新评论，同时更新 posts.comments_count += 1
> - 回复评论：INSERT 新评论，设置 parent_id 指向被回复的评论
> - 查询评论：SELECT WHERE post_id = ? ORDER BY created_at
> - 查看回复：SELECT WHERE parent_id = ? 获取某条评论的所有回复
> - 删除评论：UPDATE status = 0，同时更新 posts.comments_count -= 1

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 评论ID |
| post_id | BIGINT | NOT NULL | 帖子ID |
| user_id | BIGINT | NOT NULL | 评论用户ID |
| parent_id | BIGINT | NULL | 父评论ID（回复功能） |
| content | TEXT | NOT NULL | 评论内容 |
| likes_count | INT | DEFAULT 0 | 点赞数 |
| status | TINYINT | DEFAULT 1 | 状态（1:正常 0:删除） |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引：**
- PRIMARY KEY (id)
- INDEX idx_post (post_id, created_at)
- INDEX idx_user (user_id)
- INDEX idx_parent (parent_id)

**外键：**
- FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
- FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
- FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE

---

#### 3.3 post_likes（帖子点赞表）

帖子点赞记录。

**使用场景：**
> 👍 **点赞互动功能** - 记录用户对帖子的点赞行为
> 
> **应用场景：**
> 1. **点赞帖子**：用户在动态流或帖子详情页点击"点赞"按钮
> 2. **取消点赞**：再次点击取消点赞，删除点赞记录
> 3. **点赞状态**：查询当前用户是否已对某帖子点赞，控制按钮显示状态
> 4. **点赞列表**：查看某个帖子的所有点赞用户（可选功能）
> 5. **我的点赞**：个人中心查看自己点赞过的所有帖子
> 6. **通知触发**：点赞操作触发通知，提醒帖子作者
> 
> **设计特点：**
> - **唯一约束**：uk_post_user 确保每个用户对同一帖子只能点赞一次
> - **级联删除**：帖子删除时，相关点赞记录自动清除
> - **计数优化**：点赞/取消时同步更新 posts.likes_count
> 
> **典型操作：**
> - 点赞：INSERT INTO post_likes，同时 UPDATE posts.likes_count += 1
> - 取消点赞：DELETE FROM post_likes，同时 UPDATE posts.likes_count -= 1
> - 查询状态：SELECT COUNT(*) WHERE post_id = ? AND user_id = ?
> - 防重复点赞：利用 UNIQUE KEY 约束，INSERT 失败即已点赞

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 记录ID |
| post_id | BIGINT | NOT NULL | 帖子ID |
| user_id | BIGINT | NOT NULL | 用户ID |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 点赞时间 |

**索引：**
- PRIMARY KEY (id)
- UNIQUE KEY uk_post_user (post_id, user_id)
- INDEX idx_user (user_id)

**外键：**
- FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
- FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE

---

#### 3.4 comment_likes（评论点赞表）

评论点赞记录。

**使用场景：**
> 👍 **评论点赞功能** - 记录用户对评论的点赞行为
> 
> **应用场景：**
> 1. **点赞评论**：用户在评论列表中对某条评论点赞，认可其观点
> 2. **取消点赞**：再次点击取消对评论的点赞
> 3. **热门评论**：根据 comments.likes_count 排序，优先展示高赞评论
> 4. **点赞状态**：判断当前用户是否已对某评论点赞，控制按钮样式
> 5. **互动数据**：统计评论的受欢迎程度，用于内容质量评估
> 6. **通知触发**：点赞评论时通知评论作者
> 
> **设计特点：**
> - **唯一约束**：uk_comment_user 确保每个用户对同一评论只能点赞一次
> - **级联删除**：评论删除时，相关点赞记录自动清除
> - **计数同步**：点赞/取消时同步更新 comments.likes_count
> 
> **典型操作：**
> - 点赞评论：INSERT INTO comment_likes，同时 UPDATE comments.likes_count += 1
> - 取消点赞：DELETE FROM comment_likes，同时 UPDATE comments.likes_count -= 1
> - 批量查询状态：SELECT comment_id WHERE user_id = ? AND comment_id IN (...)
> - 热门评论排序：ORDER BY likes_count DESC LIMIT 10

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 记录ID |
| comment_id | BIGINT | NOT NULL | 评论ID |
| user_id | BIGINT | NOT NULL | 用户ID |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 点赞时间 |

**索引：**
- PRIMARY KEY (id)
- UNIQUE KEY uk_comment_user (comment_id, user_id)
- INDEX idx_user (user_id)

**外键：**
- FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE
- FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE

---

### 4. 消息通知模块

#### 4.1 notifications（通知表）

用户消息通知。

**使用场景：**
> 🔔 **消息通知中心** - 用户互动和系统通知的消息中心
> 
> **应用场景：**
> 1. **点赞通知**：帖子或评论被点赞时通知作者
> 2. **评论通知**：帖子被评论或评论被回复时通知
> 3. **关注通知**：新增粉丝时通知被关注者
> 4. **系统通知**：管理员发送的系统公告、活动通知等
> 5. **消息列表**：个人中心查看所有通知消息
> 6. **未读消息**：显示未读消息数量的小红点
> 7. **消息分类**：按通知类型筛选查看
> 8. **消息跳转**：点击通知跳转到相关内容（帖子/评论/用户）
> 
> **通知类型：**
> - **like**：点赞通知（帖子或评论被点赞）
> - **comment**：评论通知（帖子被评论或评论被回复）
> - **follow**：关注通知（被他人关注）
> - **system**：系统通知（公告、活动等）
> 
> **关联对象：**
> - **post**：关联到帖子
> - **comment**：关联到评论
> - **user**：关联到用户
> - **match**：关联到比赛
> 
> **典型操作：**
> - 发送通知：INSERT 新通知记录
> - 消息列表：SELECT WHERE user_id = ? ORDER BY created_at DESC
> - 未读数量：SELECT COUNT(*) WHERE user_id = ? AND is_read = 0
> - 标记已读：UPDATE is_read = 1 WHERE id = ?
> - 全部已读：UPDATE is_read = 1 WHERE user_id = ?
> - 删除通知：DELETE WHERE id = ?
> - 按类型筛选：SELECT WHERE user_id = ? AND type = 'like'

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 通知ID |
| user_id | BIGINT | NOT NULL | 接收用户ID |
| sender_id | BIGINT | NULL | 发送者ID |
| type | ENUM('like','comment','follow','system') | NOT NULL | 通知类型 |
| related_type | ENUM('post','comment','user','match') | NULL | 关联对象类型 |
| related_id | BIGINT | NULL | 关联对象ID |
| content | VARCHAR(500) | NOT NULL | 通知内容 |
| is_read | TINYINT | DEFAULT 0 | 是否已读（1:已读 0:未读） |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引：**
- PRIMARY KEY (id)
- INDEX idx_user_read (user_id, is_read, created_at DESC)
- INDEX idx_sender (sender_id)

**外键：**
- FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
- FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE

---

## ER 关系图说明

### 核心关系

1. **学校 ↔ 学院**：一对多关系（一个学校有多个学院）
2. **学校 ↔ 学生**：一对多关系（一个学校有多个学生）
3. **学院 ↔ 学生**：一对多关系（一个学院有多个学生）
4. **学生 ↔ 用户**：软绑定关系（通过 student_id 字段关联，非外键约束）
5. **用户 ↔ 教育经历**：一对多关系（一个用户可以有多条教育经历）
6. **教育经历 ↔ 学校/学院**：多对一关系（通过外键关联）
7. **学校 ↔ 联赛**：一对多关系（一个学校有多个联赛，软关联）
8. **联赛 ↔ 队伍**：一对多关系（一个联赛有多个队伍，软关联）
9. **用户 ↔ 帖子**：一对多关系（一个用户可以发布多个帖子）
10. **帖子 ↔ 评论**：一对多关系（一个帖子可以有多条评论）
11. **球员 ↔ 队伍**：多对多关系（通过 team_members 关联表，一个球员可以加入多个队伍，一个队伍有多个球员）
12. **比赛 ↔ 队伍**：多对一关系（一场比赛有两个队伍）
13. **比赛 ↔ 事件**：一对多关系（一场比赛有多个事件）
14. **用户 ↔ 用户（关注）**：多对多自关联（通过 user_follows）

---

## 数据字典补充

### 状态码说明

#### 用户状态 (users.status)
- `1`: 正常
- `0`: 禁用/封号

#### 比赛状态 (matches.status)
- `upcoming`: 未开始
- `live`: 进行中
- `finished`: 已结束

#### 通知类型 (notifications.type)
- `like`: 点赞通知
- `comment`: 评论通知
- `follow`: 关注通知
- `system`: 系统通知

---

## 性能优化建议

### 1. 索引优化
- 热点查询字段添加索引（user_id, category_id, created_at）
- 复合索引用于常见查询组合
- 定期分析慢查询日志，优化索引

### 2. 数据分区
- posts 表按时间分区（按月或季度）
- 历史比赛数据归档

### 3. 缓存策略
- 热门帖子、积分榜使用 Redis 缓存
- 用户信息缓存（TTL 30分钟）
- 点赞数、评论数采用计数器缓存

### 4. 读写分离
- 主库负责写操作
- 从库负责读操作（帖子列表、排行榜等）

---

## 数据安全建议

1. **密码安全**：使用 bcrypt 或 argon2 加密存储
2. **SQL注入防护**：使用参数化查询/ORM
3. **敏感信息加密**：手机号、身份证等脱敏处理
4. **数据备份**：每日全量备份 + 实时增量备份
5. **访问控制**：数据库账号权限最小化

---

## 未来扩展预留

1. **私信功能**：新增 messages 表
2. **活动报名**：新增 event_registrations 表
3. **积分系统**：新增 user_points 表
4. **勋章成就**：新增 user_achievements 表
5. **直播功能**：新增 live_streams 表

---

## 初始化数据建议

### 必须初始化的数据

1. **categories 表**：插入 5 个运动分类（足球、篮球、羽毛球、乒乓球、健身）
2. **schools 表**：初始化支持的学校列表
3. **departments 表**：初始化各学校的学院/院系数据
4. **students 表**：管理员批量导入学生数据（用于注册验证和教育经历验证）
5. **管理员账号**：创建初始管理员用户

### SQL 示例

```sql
-- 插入运动分类
INSERT INTO categories (code, name, icon, sort_order) VALUES
('football', '足球', '⚽', 1),
('basketball', '篮球', '🏀', 2),
('badminton', '羽毛球', '🏸', 3),
('pingpong', '乒乓球', '🏓', 4),
('fitness', '健身', '💪', 5);

-- 插入示例学校
INSERT INTO schools (name, code, province, city, sort_order) VALUES
('清华大学', 'THU', '北京', '北京市', 1),
('北京大学', 'PKU', '北京', '北京市', 2),
('浙江大学', 'ZJU', '浙江', '杭州市', 3);

-- 插入示例学院（假设 school_id=1 是清华大学）
INSERT INTO departments (school_id, name, code, sort_order) VALUES
(1, '计算机系', 'CS', 1),
(1, '经管学院', 'SEM', 2),
(1, '建筑学院', 'ARCH', 3);

-- 插入示例学生数据（由管理员录入）
INSERT INTO students (student_id, name, school_id, department_id, grade, major, gender) VALUES
('2024001001', '张三', 1, 1, 2024, '计算机科学与技术', 'male'),
('2024001002', '李四', 1, 1, 2024, '计算机科学与技术', 'female');
```

---

## 总结

本数据库设计方案涵盖了 UniSport 项目的所有核心功能，具有良好的扩展性和性能。建议在实际开发中：

1. 根据具体需求调整字段长度和数据类型
2. 开发环境可适当放宽约束，生产环境严格执行
3. 定期监控数据库性能，根据实际情况优化
4. 保持数据库文档与代码同步更新
