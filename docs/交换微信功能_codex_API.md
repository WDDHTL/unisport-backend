## 交换微信功能 API

> **模块**: 交换微信号  
> **基于**: docs/API.md 规范  
> **相关表**: `users`（wechat_id）、`wechat_exchange_requests`（新增表，见下）

---

## 📌 目录
- [1. 功能概述](#1-功能概述)
- [2. 数据模型](#2-数据模型)
- [3. 状态机](#3-状态机)
- [4. 接口列表](#4-接口列表)
- [5. 接口详情](#5-接口详情)
- [6. 业务规则](#6-业务规则)
- [7. 通知与推送](#7-通知与推送)
- [8. 错误码](#8-错误码)
- [9. 安全与合规](#9-安全与合规)

---

## 1. 功能概述
- 支撑用户间“交换微信号”闭环：发起 → 处理 → 结果通知 → 查看/复制微信号。
- 角色：`requester`（发起方A）、`target`（接收/处理方B）。
- 前置：双方已登录、未互相拉黑；A 必须已设置有效 `users.wechat_id`。

## 2. 数据模型
- **users**（参考 `docs/database-design.md`）：`id`、`nickname`、`avatar`、`wechat_id`（当前账号微信号，发起/同意时的快照来源）。
- **wechat_exchange_requests**（新增表建议）  
  | 字段 | 类型 | 说明 |
  |------|------|------|
  | id | BIGINT PK | 记录ID |
  | requester_id | BIGINT | 发起方A（FK users） |
  | target_id | BIGINT | 处理方B（FK users） |
  | status | ENUM('pending','accepted','rejected','cancelled','expired') | 状态 |
  | requester_wechat_snapshot | VARCHAR(128) | A 发起时的微信号快照 |
  | target_wechat_snapshot | VARCHAR(128) | B 同意时写入的微信号快照 |
  | expired_at | DATETIME | 过期时间（例：48/72h） |
  | responded_at | DATETIME | 处理时间 |
  | created_at / updated_at | DATETIME | 创建/更新时间 |
  | 约束 | UNIQUE `(requester_id, target_id, status='pending')` |
  | 索引 | `(target_id, status, created_at)`、`(requester_id, status, created_at)` |
- 隐私：微信号建议 AES 加密/脱敏存储；记录审计日志（谁在何时发起/同意/拒绝/查看）。

## 3. 状态机
- `pending → accepted | rejected | cancelled | expired`
- 关键流转：
  1) A 在 B 个人页点击“交换微信”创建 `pending`。
  2) B 在通知或详情页同意/拒绝。
  3) 同意：写入 `target_wechat_snapshot`，双方详情可见对方微信号。
  4) 拒绝：A 收到拒绝通知，详情呈现拒绝态。
  5) 可选：A 在 pending 时撤销；超时自动 `expired`。

## 4. 接口列表
| 接口 | 方法 | 场景 |
|------|------|------|
| /wechat-exchange/requests | POST | A 发起交换请求 |
| /wechat-exchange/requests | GET | A/B 列表（sent/received） |
| /wechat-exchange/requests/{id} | GET | A/B 查看详情 |
| /wechat-exchange/requests/{id}/accept | POST | B 同意并写入微信号快照 |
| /wechat-exchange/requests/{id}/reject | POST | B 拒绝 |
| /wechat-exchange/requests/{id}/cancel | POST | A 撤销 pending |

---

## 5. 接口详情

### 5.1 创建请求
**接口**: `POST /wechat-exchange/requests`  
**使用场景**: A 在 B 个人页点击“交换微信”  
**鉴权**: 需要登录  
**请求体**
```json
{
  "target_id": 2002,
  "source": "profile" // 可选：埋点/场景
}
```
**字段说明**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| target_id | Long | 是 | 目标用户ID |
| source | String | 否 | 场景标记（profile/feed等） |

**校验要点**
1. 目标用户存在且未互拉黑。  
2. requester 已设置合法 `users.wechat_id`。  
3. `(requester,target)` 不存在 pending 记录。  
4. 未超频（单对/全局）。  

**成功响应**
```json
{
  "code": 200,
  "data": {
    "id": 301,
    "status": "pending",
    "expired_at": "2024-06-30T12:00:00Z"
  }
}
```

**副作用**
- 写入 `requester_wechat_snapshot`（取当前 `users.wechat_id`）。
- 向 target 写入通知 `wechat_exchange_request`。

---

### 5.2 列表
**接口**: `GET /wechat-exchange/requests`  
**使用场景**: 我的消息/交换微信列表  
**鉴权**: 需要登录  

**查询参数**
| 参数 | 类型 | 默认 | 说明 |
|------|------|------|------|
| role | String | received | received（收到）/sent（发起） |
| status | String | all | 多选逗号分隔：pending/accepted/rejected/cancelled/expired |
| page | Integer | 1 | 页码 |
| size | Integer | 10 | 页大小 |

**成功响应示例**
```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 301,
        "status": "pending",
        "requester": {"id": 1001, "nickname": "Alice", "avatar": "..."},
        "target": {"id": 2002, "nickname": "Bob", "avatar": "..."},
        "expired_at": "2024-06-30T12:00:00Z",
        "created_at": "2024-06-28T12:00:00Z"
      }
    ],
    "total": 3,
    "current": 1,
    "size": 10,
    "pages": 1
  }
}
```

---

### 5.3 详情
**接口**: `GET /wechat-exchange/requests/{id}`  
**使用场景**: 通知卡片点击/列表点击进入详情  
**鉴权**: 需要登录，且仅 requester/target 可访问  

**成功响应示例**
```json
{
  "code": 200,
  "data": {
    "id": 301,
    "status": "accepted",
    "requester": {"id": 1001, "nickname": "Alice", "avatar": "..."},
    "target": {"id": 2002, "nickname": "Bob", "avatar": "..."},
    "other_wechat_id": "wx_bob_123",
    "expired_at": "2024-06-30T12:00:00Z",
    "created_at": "2024-06-28T12:00:00Z",
    "responded_at": "2024-06-28T13:00:00Z"
  }
}
```

**展示规则**
- 仅 `status=accepted` 且当前用户属于该记录时返回 `other_wechat_id`（解密自对应 snapshot），否则为 null。

---

### 5.4 同意
**接口**: `POST /wechat-exchange/requests/{id}/accept`  
**使用场景**: B 在详情页点击“同意”  
**鉴权**: 需要登录，且当前用户必须为 target  
**请求体**
```json
{
  "target_wechat_id": "wx_bob_123"
}
```
（可选；为空则读取 B 的 `users.wechat_id`）

**处理要点**
- 状态须为 `pending` 且未过期。  
- 写入 `target_wechat_snapshot`（加密存储），`status→accepted`，`responded_at` 赋值。  

**成功响应**
```json
{
  "code": 200,
  "data": { "status": "accepted" }
}
```

**副作用**
- 向 requester 推送 `wechat_exchange_accept`（附 B 的微信号）。  
- 向 target 推送成功提示（附 A 的微信号）。  

---

### 5.5 拒绝
**接口**: `POST /wechat-exchange/requests/{id}/reject`  
**使用场景**: B 拒绝请求  
**鉴权**: 需要登录，且当前用户为 target，状态须为 `pending`  
**请求体（可选）**
```json
{
  "reason": "暂时不方便交换"
}
```
**成功响应**
```json
{
  "code": 200,
  "data": { "status": "rejected" }
}
```
**副作用**：向 requester 推送 `wechat_exchange_reject`。

---

### 5.6 撤销
**接口**: `POST /wechat-exchange/requests/{id}/cancel`  
**使用场景**: A 撤销 pending 请求  
**鉴权**: 需要登录，且当前用户为 requester，状态须为 `pending` 且未过期  
**成功响应**
```json
{
  "code": 200,
  "data": { "status": "cancelled" }
}
```
**副作用**：可选择向 target 发送“已撤销”通知。

---

## 6. 业务规则
1. 认证：所有接口需登录态。  
2. 权限：仅 requester/target 可访问对应记录，跨用户访问返回 403。  
3. 频控：  
   - 单对用户：对同一 target 每日上限 N 次。  
   - 全局：单用户每日上限 M 次。  
   - pending 未处理前禁止重复创建。  
4. 拉黑校验：任意一方拉黑对方则禁止创建/处理。  
5. 过期处理：定时任务或请求时检测 `expired_at < now()` → 状态置 `expired`。  

## 7. 通知与推送
- 新增消息类型：`wechat_exchange_request`、`wechat_exchange_accept`、`wechat_exchange_reject`。  
- 跳转：通知卡片点击跳转 `/wechat-exchange/{id}`。  
- 未读：纳入现有通知未读红点计数。  

## 8. 错误码
| 业务码 | 说明 |
|--------|------|
| 400_WechatMissing | 发起/同意时未提供合法微信号 |
| 400_DuplicatePending | 同一 `(requester, target)` 已存在 pending |
| 400_StatusNotAllowed | 当前状态不支持此操作 |
| 403_Blocked | 双方存在拉黑关系 |
| 404_NotFound | 记录不存在或无访问权限 |
| 429_RateLimited | 触发频控 |

## 9. 安全与合规
1. 微信号加密/脱敏存储；仅 `accepted` 且为双方成员时返回对方微信号。  
2. 记录审计日志（发起/同意/拒绝/查看）。  
3. 文本校验：过滤非常规字符或敏感词。  
4. 定期清理过期/撤销的 pending，避免长期占用。  
