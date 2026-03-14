# 邀请中心 API 文档

> **版本**: v1.0.0  
> **更新时间**: 2026-03-14  
> **基础 URL**: `http://localhost:8080/api`  
> **前端页面**: `InviteCenter.tsx`

---

## 📚 目录

- [1. 接口规范](#1-接口规范)
- [2. 数据结构](#2-数据结构)
- [3. 邀请中心模块](#3-邀请中心模块)
- [4. 错误码](#4-错误码)
- [5. 前端页面与接口映射](#5-前端页面与接口映射)

---

## 1. 接口规范

- 统一响应：`{ code, message, data, timestamp }`；分页：`{ records, total, current, size, pages }`。
- 认证：除分享落地详情外，其余接口需登录；从 Token 解析 `userId`、`schoolId`。
- 时区：`activityDate`（DATE）与 `activityTime`（TIME）需使用同一时区；过期判定以服务器时间为准。

---

## 2. 数据结构

### 2.1 InviteItem

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long/String | 邀请 ID |
| hostId | Long | 发起人用户 ID |
| hostName | String | 发起人昵称 |
| hostAvatar | String | 发起人头像 |
| schoolId | Long | 学校 ID（同校隔离） |
| categoryId | Long | 运动分类（传分类 id，而不是字符串标识） |
| title | String | 邀请标题（可选） |
| description | String | 活动说明 |
| activityDate | Date | 活动日期 |
| activityTime | Time | 活动时间 |
| location | String | 活动地点 |
| maxPlayers | Integer | 最大人数（2-50） |
| joinedCount | Integer | 已加入人数（含发起人） |
| status | String | open/full/finished/canceled/expired |
| shareToken | String | 分享 Token（可选） |
| createdAt | DateTime | 创建时间 |
| isJoined | Boolean | 计算字段，当前用户是否 active 成员 |

### 2.2 MemberBrief

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | Long | 成员 ID |
| role | String | host/member |
| status | String | active/left |
| joinedAt | DateTime | 加入时间 |
| leftAt | DateTime | 退出时间（可空） |

---

## 3. 邀请中心模块

### 3.1 获取邀请广场列表

**接口**: `GET /api/invites`

**使用场景**:
- “邀请中心”默认 Tab，按当前分类/学校展示 open/full 的可加入活动。

**前端页面**: `InviteCenter.tsx`

**请求参数（Query）**:

| 参数 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| categoryId | Long | 否 | 当前首页分类 | 运动分类筛选（传分类 id） |
| status | String | 否 | open | 支持 `open,full` 组合 |
| current | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 10 | 每页条数 |
| order | String | 否 | created_at desc | 排序字段 |
| excludeExpired | Boolean | 否 | true | 是否过滤已过期 |

**成功响应**（分页）:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "hostId": 101,
        "hostName": "张伟",
        "hostAvatar": "https://...",
        "categoryId": 101,
        "activityDate": "2025-05-20",
        "activityTime": "18:00",
        "location": "北操场",
        "description": "周五晚踢球，缺2个后卫",
        "joinedCount": 8,
        "maxPlayers": 10,
        "status": "open",
        "createdAt": "2025-05-18T10:00:00",
        "isJoined": false
      }
    ],
    "total": 100,
    "current": 1,
    "size": 10,
    "pages": 10
  },
  "timestamp": 1701234567890
}
```

**注意事项**:
1. 仅返回与登录用户 `schoolId` 相同的数据。
2. `isJoined` 由 `invite_members` 计算，不落库。
3. open/full 状态用于前端按钮状态（加入/已满员）。

---

### 3.2 获取我的邀请（发起/已加入）

**接口**: `GET /api/invites/mine`

**使用场景**:
- “我的邀请”Tab：合并我发起的 + 我参加的。

**前端页面**: `InviteCenter.tsx`

**请求参数（Query）**:

| 参数 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| view | String | 否 | all | host/joined/all |
| status | String | 否 | all | open/full/finished/canceled/expired |
| current | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 10 | 每页条数 |

**成功响应**: 结构同 3.1。  
**注意事项**:
1. 发起列表按 `created_at desc`；已加入列表建议 `activity_date desc, activity_time desc`。
2. 发起人 `isJoined=true` 固定。

---

### 3.3 创建邀请

**接口**: `POST /api/invites`

**使用场景**:
- “发起”弹窗提交，创建组队。

**前端页面**: `InviteCenter.tsx`

**请求参数（Body）**:

```json
{
  "categoryId": 1,
  "activityDate": "2025-05-25",
  "activityTime": "18:00",
  "location": "北操场",
  "description": "周末轻松踢球，守门已有人",
  "maxPlayers": 8,
  "title": "周末踢球",
  "shareToken": "ABCD1234"
}
```

**参数说明**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| categoryId | Long | 是 | 运动分类（传分类 id，而不是字符串标识） |
| activityDate | String | 是 | 活动日期（YYYY-MM-DD） |
| activityTime | String | 是 | 活动时间（HH:mm） |
| location | String | 是 | 活动地点 |
| description | String | 是 | 活动说明 |
| maxPlayers | Integer | 是 | 2-50 |
| title | String | 否 | 邀请标题 |
| shareToken | String | 否 | 未传则后端生成 |

**成功响应**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 123,
    "hostId": 999,
    "hostName": "我",
    "hostAvatar": "https://...",
    "categoryId": 101,
    "activityDate": "2025-05-25",
    "activityTime": "18:00",
    "location": "北操场",
    "description": "周末轻松踢球，守门已有人",
    "joinedCount": 1,
    "maxPlayers": 8,
    "status": "open",
    "createdAt": "2025-05-20T12:00:00",
    "isJoined": true,
    "shareToken": "ABCD1234"
  },
  "timestamp": 1701234567890
}
```

**注意事项**:
1. 同一事务写入 `invites` + `invite_members`（host, role=host, status=active）。
2. 校验：`maxPlayers` 2-50；时间不得早于当前；发起频率限制。

---

### 3.4 加入邀请

**接口**: `POST /api/invites/{id}/join`

**使用场景**:
- 邀请卡片“加入”按钮；我的邀请 Tab 重新加入。

**前端页面**: `InviteCenter.tsx`

**请求参数**:
- Path: `id`
- Body（可选）:

```json
{
  "comment": "一起踢球",
  "fromShareToken": "ABCD1234"
}
```

**成功响应**: InviteItem（最新状态，`isJoined=true`）。

**注意事项**:
1. 校验 `status=open`、未过期、`joinedCount < maxPlayers`。
2. 行级锁更新 `joined_count`，与 `invite_members` 同事务。

---

### 3.5 退出邀请

**接口**: `DELETE /api/invites/{id}/join`

**使用场景**:
- 已加入用户点击“已加入”再次退出（UI toggle）。

**前端页面**: `InviteCenter.tsx`

**请求参数**:
- Path: `id`

**成功响应**: InviteItem（`isJoined=false`，计数-1，必要时 status 从 full 恢复为 open）。

**注意事项**:
1. host 不可退出；成员退出将 `invite_members.status='left'`。
2. 计数更新需在事务内完成。

---

### 3.6 取消邀请（发起人）

**接口**: `PUT /api/invites/{id}/cancel`

**使用场景**:
- 发起人取消活动，列表展示“已取消”。

**前端页面**: `InviteCenter.tsx`

**请求参数**:
- Path: `id`

**成功响应**: InviteItem（`status="canceled"`）。

**注意事项**:
1. 仅 host 可操作。
2. 可联动通知（invite_canceled）给 active 成员。

---

### 3.7 标记完成（发起人）

**接口**: `PUT /api/invites/{id}/finish`

**使用场景**:
- 活动结束后收尾，列表展示“已结束”。

**前端页面**: `InviteCenter.tsx`

**请求参数**:
- Path: `id`

**成功响应**: InviteItem（`status="finished"`）。

---

### 3.8 邀请好友/分享记录（预留）

**接口**: `POST /api/invites/{id}/share`

**使用场景**:
- “邀请好友/分享”入口产生站内邀请或外部分享。

**前端页面**: （预留）

**请求参数（Body）**:

```json
{
  "channel": "internal",
  "toUserId": 2001,
  "shareToken": "ABCD1234"
}
```

**参数说明**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| channel | String | 是 | internal/link/poster/external |
| toUserId | Long | 站内邀请必填 | 被邀请人 |
| shareToken | String | 否 | 未传则复用 invite.shareToken |

**成功响应**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "shareId": 10,
    "token": "ABCD1234"
  },
  "timestamp": 1701234567890
}
```

**注意事项**:
1. 记录到 `invite_shares`，便于归因与通知（invite_share）。
2. 加入时带 `fromShareToken` 便于转化统计。

---

### 3.9 获取邀请详情（落地页/通知跳转，预留）

**接口**: `GET /api/invites/{id}`

**使用场景**:
- 分享链接、站内通知点击后的单条落地展示。

**前端页面**: （预留）

**成功响应**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "invite": { /* InviteItem */ },
    "members": [
      {
        "userId": 101,
        "role": "host",
        "status": "active",
        "joinedAt": "2025-05-18T10:00:00",
        "leftAt": null
      }
    ]
  },
  "timestamp": 1701234567890
}
```

**注意事项**:
1. 可放开匿名访问，但仅返回同校且未被逻辑删除的 open/full/finished 记录。
2. 过期/取消返回业务错误或 40401。

---

## 4. 错误码

| 错误码 | 说明 | 解决方案 |
|-------|------|---------|
| 40401 | 邀请不存在 | 检查 inviteId |
| 40021 | 邀请已关闭/取消/结束 | 刷新列表或选择其他活动 |
| 40022 | 邀请已满员 | 等待退出或选择其他活动 |
| 40023 | 邀请已过期 | 选择其他活动 |
| 40024 | 已加入 | 前端同步状态，无需重复提交 |
| 40025 | 发起人不可退出 | 保持 host 角色 |
| 40301 | 无权限（非 host 操作 cancel/finish） | 仅发起人可操作 |
| 40901 | 重复操作/并发冲突 | 重试或刷新列表 |

---

## 5. 前端页面与接口映射

| 页面 | 功能 | 接口 |
|------|------|------|
| InviteCenter.tsx | 邀请广场列表 | `GET /api/invites` |
| InviteCenter.tsx | 我的邀请 | `GET /api/invites/mine` |
| InviteCenter.tsx | 发起邀请 | `POST /api/invites` |
| InviteCenter.tsx | 加入/退出 | `POST /api/invites/{id}/join` / `DELETE /api/invites/{id}/join` |
| InviteCenter.tsx | 取消/完成 | `PUT /api/invites/{id}/cancel` / `PUT /api/invites/{id}/finish` |
| （预留）邀请好友/分享 | 分享/站内邀请 | `POST /api/invites/{id}/share` |
| （预留）邀请详情 | 落地页/通知跳转 | `GET /api/invites/{id}` |

---

**文档维护者**: UniSport 开发团队  
**最后更新**: 2026-03-14  
**文档版本**: 1.0.0
