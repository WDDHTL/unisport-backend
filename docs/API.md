# UniSport API 接口文档

> **版本**: v1.1.0  
> **更新时间**: 2025-12-03  
> **基础URL**: `http://localhost:8080/api`  
> **前端地址**: `http://localhost:5173`

---

## 📋 目录

- [1. 接口规范](#1-接口规范)
- [2. 认证授权模块](#2-认证授权模块)
- [3. 用户模块](#3-用户模块)
- [4. 赛事模块](#4-赛事模块)
- [5. 社交模块](#5-社交模块)
- [6. 消息通知模块](#6-消息通知模块)
- [7. 系统功能模块](#7-系统功能模块)
- [8. 错误码说明](#8-错误码说明)
- [9. 前端页面与接口对应关系](#9-前端页面与接口对应关系)
- [10. 实时连接模块](#10-实时连接模块)

---

## 1. 接口规范

### 1.1 统一响应格式

所有接口统一返回以下JSON格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1701234567890
}
```

**字段说明**：
- `code`: 状态码（200成功，其他失败）
- `message`: 返回消息
- `data`: 返回数据（可以是对象、数组或null）
- `timestamp`: 时间戳

### 1.2 分页响应格式

分页接口额外包含分页信息：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [],
    "total": 100,
    "current": 1,
    "size": 10,
    "pages": 10
  },
  "timestamp": 1701234567890
}
```

### 1.3 HTTP 状态码

| 状态码 | 说明 | 使用场景 |
|--------|------|---------|
| 200 | 操作成功 | 请求处理成功 |
| 201 | 创建成功 | POST 请求创建资源成功 |
| 400 | 参数错误 | 请求参数不合法 |
| 401 | 未认证 | Token 缺失或无效 |
| 403 | 无权限 | 没有操作权限 |
| 404 | 资源不存在 | 请求的资源不存在 |
| 500 | 服务器错误 | 服务器内部错误 |

### 1.4 请求头

#### 公开接口（无需认证）
```http
Content-Type: application/json
```

#### 需认证接口
```http
Content-Type: application/json
Authorization: Bearer <JWT Token>
```

---

## 2. 认证授权模块

### 2.1 用户注册

**接口**: `POST /api/auth/register`

**使用场景**:
- 新用户通过注册页面创建账号
- 基于学号验证学生身份（软绑定）
- 注册成功后跳转到登录页面

**前端页面**: `Register.tsx`

**请求参数**:

```json
{
  "account": "2024001",
  "password": "123456",
  "school": "清华大学",
  "schoolId": 1,
  "department": "计算机系",
  "departmentId": 1,
  "studentId": "2024001001"
}
```

**参数说明**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| account | String | 是 | 账号（手机号/学号） |
| password | String | 是 | 密码（6-20位） |
| school | String | 是 | 学校名称 |
| schoolId | Long | 是 | 学校ID（注册时绑定，标识用户当前就读学校） |
| department | String | 是 | 学院名称 |
| departmentId | Long | 是 | 学院ID |
| studentId | String | 是 | 学号（用于身份验证） |

**成功响应**:

```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "id": 1,
    "account": "2024001",
    "nickname": "2024001",
    "studentId": "2024001001",
    "createdAt": "2025-12-01T10:00:00"
  }
}
```

**失败响应示例**:

```json
{
  "code": 40005,
  "message": "学号验证失败：该学号不存在或学校/学院信息不匹配",
  "data": null
}
```

**注册验证逻辑**:

1. ✅ **基础校验**：
   - 账号唯一性校验
   - 密码强度校验
   - 必填字段完整性校验

2. 🎓 **学生身份验证（软绑定）**：
   ```sql
   SELECT * FROM students 
   WHERE student_id = ? 
     AND school_id = ? 
      AND department_id = ? 
      AND status = 1
   ```
   - 如果查询到记录：验证通过，允许注册
   - 如果查询不到：注册失败，返回错误码 40005

3. 💾 **创建用户**：
   - 密码使用 BCrypt 加密存储
   - users.student_id 字段记录学号（与 students.student_id 软关联）
   - users.school_id 字段记录当前就读学校ID
   - 默认昵称为账号，用户可后续修改

4. 🎓 **写入教育经历（主记录）**：
   - 向 `user_educations` 表新增一条记录
   - 字段：`user_id`、`school_id`、`department_id`、`student_id`
   - 状态：`status=verified`，`is_primary=true`
   - 作为当前教育经历（start/end 可为空，后续可补充）

**注意事项**:
1. ⚠️ 密码使用 BCrypt 加密存储
2. ⚠️ 账号唯一性校验
3. ⚠️ 学号必须在 students 表中存在且学校/学院信息匹配
4. ⚠️ 学生状态必须为在校（status=1）
5. ⚠️ schoolId 为必填字段，注册时即绑定用户当前就读学校
6. ⚠️ 注册成功响应仅返回用户基础信息，不再包含 school、schoolId、department、departmentId 等学校/学院字段
7. 💡 前端提供学校学院下拉选择（调用 GET /api/schools 和 GET /api/departments）
8. 💡 学号输入框实时验证格式
9. 💡 注册成功后会自动生成一条“当前教育经历”主记录，可在“教育经历”页继续补充起止时间等信息

---

### 2.2 用户登录

**接口**: `POST /api/auth/login`

**使用场景**:
- 用户登录获取 JWT Token

**前端页面**: `Login.tsx`

**请求参数**:

```json
{
  "account": "2024001",
  "password": "123456"
}
```

**成功响应**:

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "nickname": "张三",
      "avatar": "https://example.com/avatar.jpg"
    }
  }
}
```

**Token 负载说明**:
- `userId`、`account`、`nickname`、`avatar`
- `schoolId`：主要教育经历的学校ID（`user_educations.is_primary=true && status=verified`），若无主教育记录则回退 `users.school_id`

**注意事项**:
1. ⚠️ Token 存储在 localStorage
2. ⚠️ Token 默认7天有效
3. 💡 登录失败3次后增加验证码
4. 💡 Token 已包含当前学校ID，前端无需单独请求 schoolId，可直接从解码后的 Token 中获取

---

## 3. 用户模块

### 3.1 获取用户信息

**接口**: `GET /api/users/{id}`

**使用场景**:
- 查看用户主页
- 查看帖子作者信息

**前端页面**: `UserProfile.tsx`, `Profile.tsx`

**成功响应**:

```json
{
  "code": 200,
  "data": {
    "id": 2,
    "nickname": "李四",
    "avatar": "https://example.com/avatar.jpg",
    "school": "清华大学",
    "schoolId": 1,
    "department": "经管学院",
    "bio": "篮球爱好者",
    "gender": 1,
    "followersCount": 120,
    "followingCount": 50,
    "postsCount": 35,
    "isFollowing": false
  }
}
```

> gender 为整数：1=男性，0=女性。

---

### 3.2 更新用户信息

**接口**: `PUT /api/users/{id}`

**使用场景**:
- 编辑个人资料

**前端页面**: `EditProfile.tsx`

**请求参数**:

```json
{
  "nickname": "张三丰",
  "avatar": "https://example.com/avatar.jpg",
  "bio": "热爱运动",
  "gender": 1
}
```

**字段说明**:
- `gender`：整数类型，1=男性，0=女性。

**注意事项**:
1. ⚠️ 只能修改自己的信息
2. ⚠️ 部分更新，未提交字段保持不变
3. ⚠️ account、school、department、student_id、school_id 不可修改
4. 💡 学校和院系信息通过教育经历表管理
5. 💡 school_id 只能通过添加教育经历更新，不允许用户随意修改

---

### 3.3 关注用户

**接口**: `POST /api/users/{id}/follow`

**使用场景**:
- 关注其他用户

**前端页面**: `UserProfile.tsx`

**注意事项**:
1. ⚠️ 不能关注自己
2. ⚠️ 检查重复关注
3. ⚠️ 发送通知给被关注者

---

### 3.4 取消关注

**接口**: `DELETE /api/users/{id}/follow`

**使用场景**:
- 取消关注用户

**前端页面**: `UserProfile.tsx`, `FollowingList.tsx`

---

### 3.5 获取关注列表

**接口**: `GET /api/users/{id}/following`

**使用场景**:
- 查看关注列表

**前端页面**: `FollowingList.tsx`

**查询参数**:

| 参数 | 类型 | 默认值 | 说明 |
|------|------|-------|------|
| current | Integer | 1 | 页码 |
| size | Integer | 20 | 每页大小 |

---

### 3.6 获取用户教育经历列表

**接口**: `GET /api/users/{id}/educations`

**使用场景**:
- 查看用户的所有教育经历
- 个人信息页面展示教育背景

**前端页面**: `EditProfile.tsx`, `Profile.tsx`

**成功响应**:

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "userId": 1,
      "school": "清华大学",
      "schoolId": 1,
      "department": "计算机系",
      "departmentId": 1,
      "studentId": "2021123456",
      "startDate": "2021-09",
      "endDate": "2025-06",
      "isPrimary": true,
      "status": "verified",
      "createdAt": "2025-12-01T10:00:00"
    },
    {
      "id": 2,
      "userId": 1,
      "school": "北京大学",
      "schoolId": 2,
      "department": "光华管理学院",
      "departmentId": 5,
      "studentId": "2025001234",
      "startDate": "2025-09",
      "endDate": null,
      "isPrimary": false,
      "status": "pending",
      "createdAt": "2025-12-03T14:00:00"
    }
  ]
}
```

**字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 教育经历ID |
| userId | Long | 用户ID |
| school | String | 学校名称 |
| schoolId | Long | 学校ID |
| department | String | 院系名称 |
| departmentId | Long | 院系ID |
| studentId | String | 学号 |
| startDate | String | 开始时间（格式：YYYY-MM） |
| endDate | String | 结束时间（null表示至今） |
| isPrimary | Boolean | 是否为主要教育经历 |
| status | String | 验证状态：pending(待验证)/verified(已验证)/failed(验证失败) |
| createdAt | DateTime | 创建时间 |

**注意事项**:
1. 仅返回 `user_educations` 表中的记录，不再拼接用户表中的初始教育信息。
2. `isPrimary=true` 的记录固定置顶，其余按 `createdAt` 倒序排列。
3. `status` 字段用于标识学号验证状态。

---

### 3.7 添加教育经历

**接口**: `POST /api/users/educations`

**使用场景**:
- 用户添加新的教育经历
- 支持升学后添加新学校信息

**前端页面**: `AddEducation.tsx`

**请求参数**:

```json
{
  "schoolId": 1,
  "departmentId": 1,
  "studentId": "2021123456",
  "startDate": "2021-09",
  "endDate": "2025-06"
}
```

**参数说明**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| schoolId | Long | 是 | 学校ID |
| departmentId | Long | 是 | 院系ID |
| studentId | String | 是 | 学号 |
| startDate | String | 否 | 开始时间（格式：YYYY-MM） |
| endDate | String | 否 | 结束时间（null表示至今） |

**成功响应**:

```json
{
  "code": 200,
  "message": "添加成功",
  "data": {
    "id": 2,
    "userId": 1,
    "school": "清华大学",
    "schoolId": 1,
    "department": "计算机系",
    "departmentId": 1,
    "studentId": "2021123456",
    "startDate": "2021-09",
    "endDate": "2025-06",
    "isPrimary": true,
    "status": "verified",
    "createdAt": "2025-12-03T14:00:00",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

**失败响应示例**:

```json
{
  "code": 40005,
  "message": "学号验证失败：该学号不存在或学校/学院信息不匹配",
  "data": null
}
```

**验证逻辑**:

1. ✅ **学号验证**（必须）：
   ```sql
   SELECT * FROM students 
   WHERE student_id = ? 
     AND school_id = ? 
     AND department_id = ? 
     AND status = 1
   ```
   - 如果查询到记录：验证通过，status设为"verified"
   - 如果查询不到：添加失败，返回错误码 40005

2. ✅ **唯一性校验**：
   - 检查同一用户是否已添加相同学校+学号的教育经历
   - 避免重复添加

3. ✅ **主教育经历处理**：
   - 请求参数不再接收 `isPrimary`，后端固定将新增记录设为主教育经历（isPrimary=true）
   - 添加前会将该用户历史主教育经历的 isPrimary 置为 false，保证仅有一条主教育经历

4. ✅ **Token 同步**：
   - 添加成功后会刷新当前登录 Token，载荷中的 `schoolId` 更新为新的主教育经历 `school_id`
   - 新 Token 通过响应体 `data.token` 返回，前端需用它覆盖本地登录态

**注意事项**:
1. ⚠️ 学号必须在 students 表中存在且学校/学院信息匹配
2. ⚠️ 同一用户不能添加重复的学校+学号组合
3. ⚠️ 只能添加自己的教育经历
4. ⚠️ 添加成功会刷新登录 Token（`data.token`），前端需立即更新 Authorization，确保后续请求使用最新 schoolId
5. 💡 新增记录即为当前主教育经历，历史主记录已自动取消
6. 💡 添加成功后返回完整的教育经历信息（包括学校和院系名称）
7. 💡 前端需调用 GET /api/schools 和 GET /api/departments 接口获取下拉选项

---

### 3.8 删除教育经历

**接口**: `DELETE /api/users/educations/{id}`

**使用场景**:
- 删除错误或过期的教育经历

**前端页面**: `EditProfile.tsx`

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 教育经历ID |

**成功响应**:

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

**注意事项**:
1. ⚠️ 只能删除自己的教育经历
2. ⚠️ 如果删除的是主教育经历（isPrimary=true），需提示用户设置新的主教育经历
3. 💡 建议至少保留一条教育经历

---

## 4. 赛事模块

### 4.1 获取运动分类

**接口**: `GET /api/categories`

**使用场景**:
- 首页导航栏展示分类
- 发帖时选择分类

**前端页面**: `Navbar.tsx`, `CreatePost.tsx`

**成功响应**:

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "code": "football",
      "name": "足球",
      "icon": "⚽",
      "sortOrder": 1
    }
  ]
}
```

**注意事项**:
1. ⚠️ 按 sortOrder 排序
2. 💡 前端启动时获取一次即可

---

### 4.2 获取比赛列表

**接口**: `GET /api/matches`

**使用场景**:
- 首页近期赛事列表
- 全部赛事页面（联赛下拉筛选 + 按当前分类展示）

**前端页面**: `MatchList.tsx`, `AllMatches.tsx`

**查询参数**:

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| categoryId | Long | 无 | 运动分类ID，首页/全部赛事进入时必传（1=足球、2=篮球、3=羽毛球、4=乒乓球、5=健身） |
| leagueId | Long | null | 联赛ID，可选；`AllMatches` 联赛下拉的 value |
| schoolId | Long | null | 学校ID（无需传，后端从登录态 Token 获取；缺失会返回未登录/学校缺失错误） |
| status | String | all | 比赛状态：upcoming / live / finished / all |

**成功响应**:

```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 101,
        "categoryCode": "football",
        "categoryName": "足球",
        "leagueId": 11,
        "leagueName": "2025 新生杯",
        "teamAName": "计算机系",
        "teamBName": "经管学院",
        "scoreA": 2,
        "scoreB": 1,
        "status": "finished",
        "matchTime": "2025-12-12T16:00:00+08:00",
        "location": "北区体育场"
      }
    ],
    "total": 12,
    "current": 1,
    "size": 20,
    "pages": 1
  }
}
```

**字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 比赛ID |
| categoryCode | String | 运动分类code（如 football、basketball），用于前端匹配图标/分类 |
| categoryName | String | 运动分类名称 |
| leagueId | Long | 联赛ID（联赛筛选时回传） |
| leagueName | String | 联赛名称；顶部“联赛筛选”下拉使用 |
| teamAName | String | A队名称 |
| teamBName | String | B队名称 |
| scoreA | Integer | A队得分，未开始可返回0 |
| scoreB | Integer | B队得分，未开始可返回0 |
| status | String | 比赛状态：upcoming / live / finished |
| matchTime | String | 开赛时间，ISO 8601 字符串，前端用于展示“MM-DD HH:mm”并提取年份徽标 |
| location | String | 比赛地点（可选，用于详情或后续展示） |

**注意事项**:
1. 按比赛时间倒序返回；`matchTime` 必须可被 `new Date()` 正确解析（含时区信息最佳），否则无法生成赛季年份徽标。
2. `status=upcoming` 时前端展示 “VS”，`live/finished` 展示比分，请保证状态准确。
3. `leagueName` 请务必返回，用于“联赛筛选”下拉；如缺失前端会退化为 `categoryName + "联赛"`。
4. 若后端支持分页请使用统一分页格式；如果一次性返回数组，前端也已兼容但推荐分页。
5. `schoolId` 由登录态 Token 自动注入，前端不需要也无法切换；`leagueId` 仅在联赛筛选时传入。
6. 如后续提供赛季/年份筛选，可扩展 `GET /api/matches/season-years?categoryId=` 供下拉使用（当前页面未开启该筛选）。
---

#### 4.2.1 获取联赛列表（用于联赛下拉）

**接口**: `GET /api/leagues`

**用途**:
- “全部赛事”页联赛下拉选项（按分类筛选联赛）
- 其他需要按联赛筛选比赛/积分榜的入口

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| categoryId | Long | 是 | 运动分类ID（返回该分类下的联赛列表） |
| schoolId | Long | 否 | 学校ID（无需传，后端从登录态 Token 获取；仅用于自动匹配当前用户学校） |

**成功响应示例**:

```json
{
  "code": 200,
  "data": [
    { "id": 11, "name": "2025 校园杯", "categoryId": 1 },
    { "id": 12, "name": "院系联赛", "categoryId": 1 }
  ]
}
```

**注意事项**:
1. 前端会将 `id` 作为 `leagueId` 传入 `/api/matches` 查询参数
2. 建议返回状态字段（如 active/archived）便于前端隐藏已归档联赛
3. 后端已按登录态自动限定学校，无需传 schoolId；跨校访问需更换登录态
4. 若暂不提供该接口，前端会 fallback 以当前分类下的比赛列表中 `league` 字段去重生成选项

---

#### 4.2.2 获取赛季年份列表（用于年度赛季下拉）

**接口**: `GET /api/matches/season-years`

**用途**:
- “全部赛事”页年度赛季下拉选项（按分类筛选可选年份）
- 其他需要按年份过滤比赛的场景

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| categoryId | Long | 是 | 运动分类ID |

**成功响应示例**:

```json
{
  "code": 200,
  "data": [2025, 2024, 2023]
}
```

**注意事项**:
1. 返回值为年份数组，按从新到旧排序；前端直接渲染下拉。
2. 若无数据可返回空数组，前端会禁用下拉并提示“暂无可用年份”。
3. 该接口优先于前端基于 `matchTime` 的年份解析，可减少解析误差。

---

### 4.3 获取比赛详情

**接口**: `GET /api/matches/{id}`

**使用场景**:
- 查看比赛详细信息
- 查看球员阵容和事件时间轴

**前端页面**: `MatchDetail.tsx`

**成功响应**:

```json
{
  "code": 200,
  "data": {
    "id": 1,
    "teamAName": "计算机系",
    "teamBName": "经管学院",
    "scoreA": 2,
    "scoreB": 1,
    "playersA": [
      {
        "id": 1,
        "playerName": "张三",
        "jerseyNumber": 10,
        "position": "FW"
      }
    ],
    "events": [
      {
        "id": 1,
        "eventType": "goal",
        "minute": 23,
        "description": "张三 劲射破门"
      }
    ]
  }
}
```

---

### 4.4 获取积分榜

**接口**: `GET /api/standings`

**使用场景**:
- 查看联赛积分排名

**前端页面**: `Standings.tsx`

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| leagueId | Long | 是 | 联赛ID（必须指定联赛，默认为下拉框第一个选项） |
| categoryId | Long | 是 | 运动分类ID |
| year | Integer | 否 | 年份，默认当前年份（兼容旧版本） |

**成功响应**:

```json
{
  "code": 200,
  "data": [
    {
      "rank": 1,
      "teamId": 5,
      "teamName": "计算机系",
      "leagueId": 1,
      "leagueName": "2025新生杯",
      "played": 5,
      "won": 4,
      "drawn": 1,
      "lost": 0,
      "points": 13
    }
  ]
}
```

**注意事项**:
1. ⚠️ 计分规则：胜3分、平1分、负0分
2. 💡 缓存积分榜数据
3. 💡 leagueId 和 categoryId 均为必填，未操作时前端默认使用联赛下拉第一项；year 仅用于历史数据

---

### 4.5 获取球员统计

**接口**: `GET /api/player-stats`

**使用场景**:
- 查看射手榜、得分榜

**前端页面**: `PlayerStats.tsx`

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| leagueId | Long | 是 | 联赛ID（必须指定联赛，默认为下拉框第一个选项） |
| categoryId | Long | 是 | 运动分类ID |
| year | Integer | 否 | 年份（兼容旧版本） |

**成功响应**:

```json
{
  "code": 200,
  "data": [
    {
      "rank": 1,
      "playerName": "张三",
      "teamName": "计算机系",
      "leagueId": 1,
      "leagueName": "2025新生杯",
      "played": 5,
      "goals": 8
    }
  ]
}
```

**注意事项**:
1. 💡 leagueId 和 categoryId 均为必填，默认使用联赛下拉第一项；year 仅用于历史数据
2. 💡 每年的球员统计按联赛区分
3. 💡 数据按登录态的 schoolId 过滤，无需传 schoolId，跨校需更换登录态

---

## 5. 社交模块

### 5.1 获取帖子列表

**接口**: `GET /api/posts`

**使用场景**:
- 首页动态流
- 按分类筛选帖子
- 仅显示当前用户所在学校的帖子

**前端页面**: `PostFeed.tsx`, `MyPosts.tsx`

**查询参数**:

| 参数 | 类型 | 默认值 | 说明 |
|------|------|-------|------|
| categoryId | Long | null | 运动分类ID（可选，筛选某运动分类的帖子） |

**成功响应**:

```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 2,
        "userName": "李四",
        "userAvatar": "https://example.com/avatar.jpg",
        "categoryCode": "football",
        "content": "今天的比赛太精彩了！",
        "images": ["https://example.com/post1.jpg"],
        "likesCount": 120,
        "commentsCount": 15,
        "isLiked": false,
        "createdAt": "2025-12-01T10:30:00"
      }
    ]
  }
}
```

**业务逻辑**:
1. 后端优先从登录态 Token 的 `schoolId` 获取学校ID，缺失时回落查询 `users.school_id`
2. 查询条件强制添加 `WHERE posts.school_id = 当前用户的 schoolId（来自 Token/用户表）`
3. 用户只能看到当前就读学校的帖子，切换学校后无法看到旧学校的帖子

**注意事项**:
1. ⚠️ 如果用户已毕业（users.school_id 为 NULL），返回空列表
2. 💡 前端无需传递 schoolId 参数，后端自动过滤（从 Token/用户表读取）
3. 💡 移动端应用不使用分页，后端返回所有帖子
4. 💡 响应不再返回学校字段（schoolId、schoolName），仅用于后端过滤

---

### 5.2 获取帖子详情

**接口**: `GET /api/posts/{id}`

**使用场景**:
- 查看帖子详情和评论

**前端页面**: `PostDetail.tsx`

**成功响应**:

```json
{
  "code": 200,
  "data": {
    "id": 1,
    "userId": 1,
    "userName": "伟大的灰太狼",
    "userAvatar": "https://unisport-upload.oss-cn-shenzhen.aliyuncs.com/9be23ff8-40fd-4c59-8617-3434882a32b3.jpg",
    "content": "今天的比赛太精彩了！",
    "images": ["https://unisport-upload.oss-cn-shenzhen.aliyuncs.com/44c8cd45-0989-42a8-afc8-d58e6b33252d.jpg"],
    "likesCount": 10,
    "commentsCount": 11,
    "Liked": true,
    "createdAt": "2025-12-01T10:35:00",
    "comments": [
      {
        "id": 1,
        "userName": "王五",
        "userAvatar": "https://unisport-upload.oss-cn-shenzhen.aliyuncs.com/9be23ff8-40fd-4c59-8617-3434882a32b3.jpg",
        "parentId": 0,
        "content": "说得对！",
        "likesCount": 12,
        "createdAt": "2025-12-01T10:35:00"
      }
    ]
  }
}
```

**字段说明**:

- `Liked`：boolean，true 表示当前用户已点赞，false 表示未点赞。
- `comments`:  评论数组。
- `parentId`:  Long,  表示父评论id，0 表示是直接评论帖子，非0表示前端需要将该评论挂在到父评论下，实现一种回复评论效果。

---

### 5.3 发布帖子

**接口**: `POST /api/posts`

**使用场景**:
- 发布新帖子
- 发帖时自动绑定用户当前就读学校

**前端页面**: `CreatePost.tsx`

**请求参数**:

```json
{
  "categoryId": 1,
  "content": "今天的比赛太精彩了！",
  "images": ["https://example.com/upload/123.jpg"]
}
```

**成功响应**:

```json
{
  "code": 200,
  "message": "发布成功",
  "data": {
    "id": 1,
    "userId": 2,
    "schoolId": 1,
    "categoryId": 1,
    "content": "今天的比赛太精彩了！",
    "images": ["https://example.com/upload/123.jpg"],
    "createdAt": "2025-12-09T11:00:00"
  }
}
```

**业务逻辑**:
1. 后端优先从登录态 Token 的 `schoolId` 自动获取学校ID，缺失时回落查询 `users.school_id`
2. 创建帖子时，`posts.school_id` 字段自动设置为上述学校ID
3. 用户无需（也无法）传递 schoolId 参数

**注意事项**:
1. ⚠️ 内容长度1-5000字符
2. ⚠️ 最多9张图片
3. ⚠️ 1分钟内最多发布3条
4. ⚠️ 如果用户已毕业（users.school_id 为 NULL），返回错误码 40007，提示"毕业用户暂不支持发帖"
5. 💡 school_id 字段对前端透明，由后端自动处理

---

### 5.4 点赞帖子

**接口**: `POST /api/posts/{id}/like`

**使用场景**:
- 点赞帖子

**前端页面**: `PostFeed.tsx`, `PostDetail.tsx`

---

### 5.5 取消点赞

**接口**: `DELETE /api/posts/{id}/like`

---

### 5.6 评论帖子

**接口**: `POST /api/posts/{id}/comments`

**使用场景**:
- 发表评论

**前端页面**: `PostDetail.tsx`

**请求参数**:

```json
{
  "content": "说得太对了！",
  "parentId": 0
}
```

---

### 5.7 删除帖子

**接口**: `DELETE /api/posts/{id}`

**注意事项**:
1. ⚠️ 只能删除自己的帖子
2. ⚠️ 逻辑删除（deleted=1，MyBatis Plus自动过滤）

---

### 5.8 回复评论

**接口**: `POST /api/comments/{id}/reply`

**使用场景**:
- 在帖子详情中对指定评论进行楼中楼回复
- 触发被回复用户的消息通知

**前端页面**: `PostDetail.tsx`

**请求参数**:

```json
{
  "content": "同意你的观点！"
}
```

**成功响应**:

```json
{
  "code": 200,
  "message": "回复成功",
  "data": {
    "id": 22,
    "postId": 5,
    "parentId": 8,
    "userId": 3,
    "userName": "张三",
    "userAvatar": "https://example.com/avatar.jpg",
    "content": "同意你的观点！",
    "likesCount": 0,
    "createdAt": "2026-01-16T12:00:00"
  }
}
```

**注意事项**:
1. ⚠️ 路径中的 `{id}` 视为父评论ID，后端需校验父评论存在且归属同一帖子，否则返回 40401。
2. ⚠️ 回复内容长度 1-1000 字符，需登录才能操作。
3. 💡 `parentId` 固定为目标评论ID，前端无需再传；返回结果沿用帖子详情中的评论字段结构，方便直接插入到原评论子列表。
4. 💡 回复成功后发送 comment 类型通知给父评论作者，并透传帖子ID以便客户端跳转。

---

### 5.9 点赞评论

**接口**: `POST /api/comments/{id}/like`

**使用场景**:
- 在帖子详情页为评论点赞或取消点赞

**前端页面**: `PostDetail.tsx`

**请求参数**:

```json
{
  "action": "like"
}
```

`action` 取值：`like` 点赞、`unlike` 取消点赞。

**成功响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "liked": true,
    "likesCount": 13
  }
}
```

**注意事项**:
1. ⚠️ 幂等处理：重复点赞/取消不报错，仅返回当前状态。
2. ⚠️ 需校验评论所属帖子与当前登录用户学校一致，否则拒绝。
3. 💡 点赞成功发送 like 类型通知给评论作者，避免给自己点赞时发送通知。
4. 💡 后端根据 `action` 决定增减点赞数，返回最新 `likesCount` 与当前 `liked` 状态，便于前端同步。

---

## 6. 消息通知模块

### 6.1 获取通知列表

**接口**: `GET /api/notifications`

**使用场景**:
- 查看消息通知

**前端页面**: `MyMessages.tsx`

**查询参数**:

| 参数 | 类型 | 默认值 | 说明 |
|------|------|-------|------|
| type | String | all | 通知类型：like/comment/follow/all |
| current | Integer | 1 | 页码 |
| size | Integer | 20 | 每页大小 |

**成功响应**:

```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "type": "like",
        "userName": "李四",
        "content": "赞了你的帖子",
        "isRead": false,
        "createdAt": "2025-12-01T15:00:00"
      }
    ],
    "unreadCount": 5
  }
}
```

---

### 6.2 标记已读

**接口**: `PUT /api/notifications/{id}/read`

---

### 6.3 全部已读

**接口**: `PUT /api/notifications/read-all`

---

### 6.4 未读数量

**接口**: `GET /api/notifications/unread-count`

**使用场景**:
- 底部导航栏红点提示

**成功响应**:

```json
{
  "code": 200,
  "data": {
    "unreadCount": 5
  }
}
```

---

## 7. 系统功能模块

### 7.2 获取学校列表

**接口**: `GET /api/schools`

**使用场景**:
- 注册时选择学校
- 系统初始化时获取学校列表

**前端页面**: `Register.tsx`

**查询参数**:

| 参数 | 类型 | 默认值 | 说明 |
|------|------|-------|------|
| province | String | null | 省份筛选（可选） |
| city | String | null | 城市筛选（可选） |

**成功响应**:

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "清华大学",
      "code": "THU",
      "province": "北京",
      "city": "北京市"
    },
    {
      "id": 2,
      "name": "北京大学",
      "code": "PKU",
      "province": "北京",
      "city": "北京市"
    }
  ]
}
```

**注意事项**:
1. ⚠️ 按 sort_order 排序
2. ⚠️ 只返回 status=1 的启用学校
3. 💡 前端启动时缓存学校列表

---

### 7.3 获取学院列表

**接口**: `GET /api/departments`

**使用场景**:
- 注册时根据学校选择学院

**前端页面**: `Register.tsx`

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| schoolId | Long | 是 | 学校ID |

**成功响应**:

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "schoolId": 1,
      "name": "计算机系",
      "code": "CS"
    },
    {
      "id": 2,
      "schoolId": 1,
      "name": "经管学院",
      "code": "SEM"
    }
  ]
}
```

**注意事项**:
1. ⚠️ schoolId 为必填参数
2. ⚠️ 按 sort_order 排序
3. ⚠️ 只返回 status=1 的启用学院
4. 💡 前端根据选择的学校动态加载学院列表

---

### 7.4 验证学号是否存在

**接口**: `GET /api/students/validate`

**使用场景**:
- 注册时实时验证学号

**前端页面**: `Register.tsx`

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| studentId | String | 是 | 学号 |
| schoolId | Long | 是 | 学校ID |
| departmentId | Long | 是 | 学院ID |

**成功响应**:

```json
{
  "code": 200,
  "data": {
    "valid": true,
    "message": "学号验证通过"
  }
}
```

**失败响应**:

```json
{
  "code": 200,
  "data": {
    "valid": false,
    "message": "该学号不存在或学校/学院信息不匹配"
  }
}
```

**注意事项**:
1. 💡 前端建议防抖处理，避免频繁请求
2. 💡 可以在表单提交前再次验证
3. ⚠️ 仅验证学生是否存在，不返回学生详细信息

---

### 7.5 文件上传

**接口**: `POST /api/common/upload`

**使用场景**:
- 上传头像、帖子图片

**请求类型**: `multipart/form-data`

**请求头**:
```
Authorization: Bearer <JWT Token>
Content-Type: multipart/form-data
```

**请求参数**:

```
file: [二进制文件]（必填，仅支持图片）
```

**成功响应**:

```json
{
  "code": 200,
  "message": "上传成功",
  "data": "https://cdn.example.com/upload/abc123.jpg",
  "timestamp": 1701234567890
}
```

**注意事项**:
1. ⚠️ 需登录，前端会自动携带 Authorization 头
2. ⚠️ 仅支持 JPG/JPEG、PNG、GIF，单个文件最大 5MB
3. 💡 前端已内置格式/大小校验，上传失败会提示重新选择

---

## 8. 错误码说明

### 8.1 业务错误码

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 40001 | 账号已存在 | 更换账号重新注册 |
| 40002 | 账号或密码错误 | 检查输入 |
| 40003 | 账号已被禁用 | 联系管理员 |
| 40004 | 参数验证失败 | 检查请求参数 |
| 40005 | 学号验证失败 | 该学号不存在或学校/学院信息不匹配 |
| 40006 | 教育经历已存在 | 同一学校+学号的教育经历已添加 |
| 40007 | 毕业用户禁止发帖 | 毕业用户（school_id为NULL）暂不支持发帖 |
| 40101 | Token无效或过期 | 重新登录 |
| 40301 | 无权限操作 | 检查操作权限 |
| 40401 | 资源不存在 | 检查资源ID |
| 40901 | 重复操作 | 避免重复提交 |
| 50001 | 服务器内部错误 | 联系技术支持 |

---

## 9. 前端页面与接口对应关系

### 9.1 登录注册流程

| 页面 | 接口 | 说明 |
|------|------|------|
| Login.tsx | POST /api/auth/login | 用户登录 |
| Register.tsx | POST /api/auth/register | 用户注册 |
| Register.tsx | GET /api/schools | 获取学校列表 |
| Register.tsx | GET /api/departments | 获取学院列表 |
| Register.tsx | GET /api/students/validate | 验证学号 |

---

### 9.2 首页流程

| 页面 | 接口 | 说明 |
|------|------|------|
| Navbar.tsx | GET /api/categories | 获取运动分类 |
| MatchList.tsx | GET /api/matches | 获取最近比赛 |
| PostFeed.tsx | GET /api/posts | 获取帖子列表 |

---

### 9.3 赛事流程

| 页面 | 接口 | 说明 |
|------|------|------|
| AllMatches.tsx | GET /api/matches | 获取全部比赛 |
| MatchDetail.tsx | GET /api/matches/{id} | 获取比赛详情 |
| Standings.tsx | GET /api/standings | 获取积分榜 |
| PlayerStats.tsx | GET /api/player-stats | 获取球员统计 |

---

### 9.4 社交流程

| 页面 | 接口 | 说明 |
|------|------|------|
| CreatePost.tsx | POST /api/posts | 发布帖子 |
| CreatePost.tsx | POST /api/common/upload | 上传图片 |
| PostDetail.tsx | GET /api/posts/{id} | 获取帖子详情 |
| PostDetail.tsx | POST /api/posts/{id}/like | 点赞帖子 |
| PostDetail.tsx | POST /api/posts/{id}/comments | 评论帖子 |
| PostDetail.tsx | POST /api/comments/{id}/reply | 回复评论 |
| PostDetail.tsx | POST /api/comments/{id}/like | 点赞/取消点赞评论 |

---

### 9.5 个人中心流程

| 页面 | 接口 | 说明 |
|------|------|------|
| Profile.tsx | GET /api/auth/current | 获取当前用户信息 |
| EditProfile.tsx | PUT /api/users/{id} | 更新用户信息 |
| EditProfile.tsx | GET /api/users/{id}/educations | 获取教育经历列表 |
| EditProfile.tsx | DELETE /api/users/educations/{id} | 删除教育经历 |
| EditProfile.tsx | POST /api/common/upload | 上传头像 |
| AddEducation.tsx | GET /api/schools | 获取学校列表 |
| AddEducation.tsx | GET /api/departments | 获取学院列表 |
| AddEducation.tsx | POST /api/users/educations | 添加教育经历 |
| MyPosts.tsx | GET /api/users/{id}/posts | 获取我的帖子 |
| MyMessages.tsx | GET /api/notifications | 获取通知列表 |
| FollowingList.tsx | GET /api/users/{id}/following | 获取关注列表 |
| UserProfile.tsx | GET /api/users/{id} | 获取用户信息 |
| UserProfile.tsx | POST /api/users/{id}/follow | 关注用户 |

---

## 10. 实时连接模块

### 10.1 WebSocket 连接
**接口**: `WS /api/ws?token=...`

- 用途：已登录用户建立 WebSocket 长连接，接收后端实时推送（通知/消息等）  
- 协议：WebSocket，地址 `ws://localhost:8080/api/ws`  
- 鉴权：token 通过查询参数传递（前端从 localStorage 或 cookie 读取）；未登录不发起连接  
- 断线重连：默认 3s 重连，需本地存在 token + user 才会再次尝试  
- 前端封装：`services/websocketService.ts` 暴露 `connect(token?)`、`disconnect()`、`addMessageListener(fn)`、`removeMessageListener(fn)`；`onmessage` 收到的 payload 为字符串，业务侧自行解析（通常为 JSON 文本）

---

## 附录：数据字典

### 运动分类 (category_code)

| Code | Name | Icon |
|------|------|------|
| football | 足球 | ⚽ |
| basketball | 篮球 | 🏀 |
| badminton | 羽毛球 | 🏸 |
| pingpong | 乒乓球 | 🏓 |
| fitness | 健身 | 💪 |

### 比赛状态 (match_status)

| Value | Text |
|-------|------|
| upcoming | 未开始 |
| live | 进行中 |
| finished | 已结束 |

### 通知类型 (notification_type)

| Value | Text |
|-------|------|
| like | 点赞通知 |
| comment | 评论通知 |
| follow | 关注通知 |
| system | 系统通知 |

---

**文档维护者**: UniSport 开发团队  
**最后更新**: 2025-12-03  
**文档版本**: 1.1.0
