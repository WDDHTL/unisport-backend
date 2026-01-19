# UniSport 后端待实现接口清单
- 基础路径：`/api`（见 `server.servlet.context-path`）
- 默认返回：`Result<T>`，出错时 `Result.error(code, message)`
- 鉴权：除白名单（`/auth/**` 登录/注册、`/schools`、`/departments`、`/students`、`/students/validate`、Swagger 文档）外，均需携带 `Authorization: Bearer <token>`

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

### 10.1 WebSocket 连接
**接口**: `WS /api/ws?token=...`

- 用途：已登录用户建立 WebSocket 长连接，接收后端实时推送（通知/消息等）  
- 协议：WebSocket，地址 `ws://localhost:8080/api/ws`  
- 鉴权：token 通过查询参数传递（前端从 localStorage 或 cookie 读取）；未登录不发起连接  
- 断线重连：默认 3s 重连，需本地存在 token + user 才会再次尝试  
- 前端封装：`services/websocketService.ts` 暴露 `connect(token?)`、`disconnect()`、`addMessageListener(fn)`、`removeMessageListener(fn)`；`onmessage` 收到的 payload 为字符串，业务侧自行解析（通常为 JSON 文本）

---

### 取消评论点赞

### 删除评论
