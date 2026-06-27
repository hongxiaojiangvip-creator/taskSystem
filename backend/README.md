# 后端服务 (Spring Boot + MySQL)

健身打卡小程序后端,基于 Spring Boot 3 + MyBatis-Plus + MySQL 8。

## 环境要求
- JDK 17+
- Maven 3.8+
- MySQL 8.0+

## 快速开始

### 1. 初始化数据库
```bash
mysql -u root -p < src/main/resources/db/schema.sql
```
会创建 `fitness_checkin` 库、所有表,并初始化运动类型字典。

### 2. 配置(通过环境变量,可选)
| 变量 | 说明 | 默认 |
|---|---|---|
| `MYSQL_HOST` | MySQL 地址 | localhost |
| `MYSQL_PORT` | 端口 | 3306 |
| `MYSQL_USER` | 用户名 | root |
| `MYSQL_PASSWORD` | 密码 | root |
| `JWT_SECRET` | JWT 签名密钥(生产必改) | 内置默认值 |
| `WX_APPID` | 微信小程序 AppID | 空 |
| `WX_SECRET` | 微信小程序 AppSecret | 空 |
| `WX_MOCK_LOGIN` | 未配置 AppID 时用 mock 登录 | true |
| `UPLOAD_DIR` | 图片上传目录 | ./uploads |
| `UPLOAD_URL_PREFIX` | 图片访问前缀 | http://localhost:8080/uploads |
| `ANTHROPIC_API_KEY` | Claude API Key,启用 AI 能力(为空时返回降级文案) | 空 |
| `AI_MODEL` | 使用的 Claude 模型 | claude-opus-4-8 |

> 本地联调:不配 `WX_APPID` 时,`mock-login` 会把 `wx.login` 的 code 直接当作 openid,无需真实微信后台即可登录调试。

### 3. 运行
```bash
mvn spring-boot:run
```
服务默认监听 `http://localhost:8080`。

## API 概览
| 方法 | 路径 | 说明 | 需登录 |
|---|---|---|---|
| POST | `/api/auth/login` | 微信登录,返回 token | 否 |
| GET  | `/api/sport-types` | 运动类型字典 | 否 |
| GET  | `/api/user/profile` | 当前用户信息 | 是 |
| POST | `/api/user/profile` | 更新昵称/头像 | 是 |
| POST | `/api/checkins` | 创建打卡 | 是 |
| GET  | `/api/checkins/today` | 今天是否已打卡 | 是 |
| GET  | `/api/checkins/mine` | 我的打卡列表(分页) | 是 |
| GET  | `/api/checkins/square` | 打卡广场(分页) | 是 |
| GET  | `/api/checkins/calendar` | 某月打卡日期 | 是 |
| GET  | `/api/checkins/{id}` | 打卡详情 | 是 |
| DELETE | `/api/checkins/{id}` | 删除打卡 | 是 |
| POST | `/api/checkins/{id}/like` | 点赞/取消点赞 | 是 |
| POST | `/api/upload` | 图片上传 | 是 |
| GET  | `/api/stats/overview` | 统计概览 | 是 |
| GET  | `/api/stats/trend?days=7` | 运动趋势 | 是 |
| GET  | `/api/goal` | 获取目标 | 是 |
| POST | `/api/goal` | 保存目标 | 是 |
| GET  | `/api/rank?period=week` | 排行榜 week/month/all | 是 |
| POST | `/api/ai/comment` | AI 教练点评某次打卡 `{checkinId}` | 是 |
| GET  | `/api/ai/weekly-report` | AI 周报(分析最近 7 天) | 是 |
| POST | `/api/ai/plan` | AI 训练计划 `{goal?}` | 是 |
| POST | `/api/ai/chat` | AI 健身问答 `{messages:[{role,content}]}` | 是 |

> AI 能力基于 Anthropic Claude(`claude-opus-4-8`)。未配置 `ANTHROPIC_API_KEY` 时接口返回友好的降级文案,小程序功能不受影响。

统一返回体:
```json
{ "code": 0, "message": "ok", "data": { } }
```
`code=0` 成功;`401` 未登录/登录过期;其它为业务错误,`message` 为提示文案。

鉴权:登录后所有 `/api/**`(除登录、运动类型)需带请求头 `Authorization: Bearer <token>`。
