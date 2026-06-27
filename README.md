# 健身打卡微信小程序

一个完整的健身打卡微信小程序:**原生小程序前端 + Spring Boot + MySQL 后端**。

支持微信登录、每日打卡、运动记录、连续天数统计、日历视图、打卡广场与点赞、排行榜、数据图表与目标管理。

## 技术栈
| 层 | 技术 |
|---|---|
| 前端 | 原生微信小程序(WXML / WXSS / JS) |
| 后端 | Spring Boot 3 + MyBatis-Plus |
| 数据库 | MySQL 8 |
| 鉴权 | 微信 code2session + JWT |
| 图片 | 本地存储(可扩展为 OSS) |

## 目录结构
```
.
├── backend/                # Spring Boot 后端
│   ├── src/main/java/...    # 代码(controller/service/mapper/entity)
│   ├── src/main/resources/
│   │   ├── application.yml  # 配置
│   │   └── db/schema.sql    # 建表脚本
│   └── README.md           # 后端运行说明 + API 文档
└── miniprogram/            # 原生小程序前端
    ├── app.js / app.json   # 入口与全局配置
    ├── config.js           # 后端地址配置
    ├── utils/              # request / api 封装
    └── pages/              # 9 个页面
```

## 功能清单
- ✅ 微信一键登录(支持本地 mock 调试)
- ✅ 每日打卡:选运动类型、填时长备注、上传图片
- ✅ 连续打卡天数 / 累计天数 / 最长连续统计
- ✅ 打卡日历视图
- ✅ 我的打卡记录列表与详情、删除
- ✅ 打卡广场(社交动态流)+ 点赞
- ✅ 排行榜(周 / 月 / 总榜)
- ✅ 数据统计图表(运动趋势柱状图)+ 目标进度
- ✅ 健身目标设置、个人资料编辑

## 快速开始

### 后端
详见 [backend/README.md](backend/README.md)。简版:
```bash
# 1. 建库建表
mysql -u root -p < backend/src/main/resources/db/schema.sql
# 2. 启动(默认 8080,未配 AppID 时走 mock 登录)
cd backend && mvn spring-boot:run
```

### 前端
1. 用 **微信开发者工具** 导入 `miniprogram/` 目录。
2. 修改 `miniprogram/config.js` 里的 `BASE_URL` 为你的后端地址。
3. 本地调试:在开发者工具「详情 → 本地设置」勾选 **不校验合法域名**(因为本地是 http)。
4. 正式发布前:在 `project.config.json` 填入真实 `appid`,后端配置 `WX_APPID` / `WX_SECRET`,并在小程序后台配置 request/uploadFile 的合法域名(需 https)。

## 页面一览
| 页面 | 路径 | 说明 |
|---|---|---|
| 打卡首页 | pages/index | 连续天数、今日打卡、日历 |
| 记录打卡 | pages/checkin | 选类型/时长/备注/图片 |
| 我的记录 | pages/records | 打卡历史列表 |
| 打卡详情 | pages/detail | 详情、点赞、删除 |
| 打卡广场 | pages/square | 全站动态 + 点赞 |
| 排行榜 | pages/rank | 周/月/总榜 |
| 数据统计 | pages/stats | 趋势图 + 目标进度 |
| 我的 | pages/mine | 资料、统计入口 |
| 目标设置 | pages/goal | 每周天数/分钟目标 |

## 说明
- 本仓库为同一代码库内的前后端两个子目录,便于一起管理。
- 卡路里为按运动类型估算值,仅供参考。
- 生产部署请务必修改 `JWT_SECRET`,并使用 HTTPS。
