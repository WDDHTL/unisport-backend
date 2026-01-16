# UniSport 后端已实现接口清单

- 基础路径：`/api`（见 `server.servlet.context-path`）
- 默认返回：`Result<T>`，出错时 `Result.error(code, message)`
- 鉴权：除白名单（`/auth/**` 登录/注册、`/schools`、`/departments`、`/students`、`/students/validate`、Swagger 文档）外，均需携带 `Authorization: Bearer <token>`

## 认证授权 `/auth`
| 方法 | 路径 | 描述 | 主要入参 | 鉴权 |
| --- | --- | --- | --- | --- |
| POST | `/api/auth/register` | 用户注册，验证学号及学校/学院 | Body: `RegisterDTO{account,password,schoolId,departmentId,school,department,studentId}` | 否 |
| POST | `/api/auth/login` | 用户登录，返回 JWT（同时设置 `ACCESS_TOKEN` Cookie） | Body: `LoginDTO{account,password}` | 否 |

## 系统信息 `/system`
| 方法 | 路径 | 描述 | 主要入参 | 鉴权 |
| --- | --- | --- | --- | --- |
| GET | `/api/system/health` | 健康检查 | - | 是 |
| GET | `/api/system/info` | 系统基础信息 | - | 是 |

## 公共能力 `/common`
| 方法 | 路径 | 描述 | 主要入参 | 鉴权 |
| --- | --- | --- | --- | --- |
| POST | `/api/common/upload` | 文件上传到 OSS，返回 URL | Form: `file` (Multipart) | 是 |

## 学校/学院/学生
| 方法 | 路径 | 描述 | 主要入参 | 鉴权 |
| --- | --- | --- | --- | --- |
| GET | `/api/schools` | 获取学校列表，支持省/市筛选 | Query: `province?`, `city?` | 否 |
| GET | `/api/departments` | 根据学校获取学院列表 | Query: `schoolId` | 否 |
| GET | `/api/students/validate` | 验证学号与学校/学院是否匹配 | Query: `studentId`, `schoolId`, `departmentId` | 否 |

## 运动分类 `/categories`
| 方法 | 路径 | 描述 | 主要入参 | 鉴权 |
| --- | --- | --- | --- | --- |
| GET | `/api/categories` | 获取启用的运动分类列表 | - | 是 |

## 联赛 `/leagues`
| 方法 | 路径 | 描述 | 主要入参 | 鉴权 |
| --- | --- | --- | --- | --- |
| GET | `/api/leagues` | 获取联赛列表 | Query: `categoryId`，`schoolId?` | 是 |

## 比赛 `/matches`
| 方法 | 路径 | 描述 | 主要入参 | 鉴权 |
| --- | --- | --- | --- | --- |
| GET | `/api/matches` | 比赛列表，按分类/联赛/状态筛选 | Query: `categoryId`，`leagueId?`，`schoolId?`，`status?`(upcoming/live/finished/all) | 是 |
| GET | `/api/matches/{id}` | 比赛详情（阵容、事件等） | Path: `id` | 是 |

## 球员榜 `/player-stats`
| 方法 | 路径 | 描述 | 主要入参 | 鉴权 |
| --- | --- | --- | --- | --- |
| GET | `/api/player-stats` | 球员榜/排名 | Query: `categoryId`，`leagueId`，`year?` | 是 |

## 积分榜 `/standings`
| 方法 | 路径 | 描述 | 主要入参 | 鉴权 |
| --- | --- | --- | --- | --- |
| GET | `/api/standings` | 联赛积分榜 | Query: `categoryId`，`leagueId`，`year?` | 是 |

## 帖子 `/posts`
| 方法 | 路径 | 描述 | 主要入参 | 鉴权 |
| --- | --- | --- | --- | --- |
| POST | `/api/posts` | 发布帖子 | Body: `CreatePostDTO{categoryId,content,images?}` | 是 |
| GET | `/api/posts` | 帖子列表 | Query: `categoryId` | 是 |
| GET | `/api/posts/{id}` | 帖子详情 | Path: `id` | 是 |
| POST | `/api/posts/{id}/like` | 点赞帖子（幂等，依赖唯一约束） | Path: `id` | 是 |
| DELETE | `/api/posts/{id}/like` | 取消点赞 | Path: `id` | 是 |

## 用户 `/users`
| 方法 | 路径 | 描述 | 主要入参 | 鉴权 |
| --- | --- | --- | --- | --- |
| GET | `/api/users/{id}` | 获取用户信息 | Path: `id` | 是 |
| PUT | `/api/users/{id}` | 更新用户信息 | Path: `id`; Body: `UpdateUserDTO{nickname?,avatar?,bio?,gender?}` | 是 |
| POST | `/api/users/{id}/follow` | 关注用户 | Path: `id` | 是 |
| DELETE | `/api/users/{id}/follow` | 取消关注 | Path: `id` | 是 |
| GET | `/api/users/{id}/following` | 获取关注列表（分页） | Path: `id`; Query: `current?`(默认1), `size?`(默认20) | 是 |
| GET | `/api/users/{id}/educations` | 获取用户教育经历 | Path: `id` | 是 |
| POST | `/api/users/educations` | 添加教育经历 | Body: `AddEducationDTO{schoolId,departmentId,studentId,startDate,endDate?}` | 是 |
