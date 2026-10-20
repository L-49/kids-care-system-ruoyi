# 幼儿园管理系统（若依 RuoYi-Vue 3.9.2 版）

将原自研 Spring Boot 幼儿园管理系统整体迁移到 **RuoYi-Vue 3.9.2（Spring Boot 3 + Vue3 + Element Plus）** 骨架之上：
保留原有业务与演示数据，权限、菜单、字典、日志、代码生成等能力复用若依原生实现。

- 后端：`kids-care-ruoyi`（本仓库）
- 前端：`kids-care-ruoyi-ui`（RuoYi-Vue3）

---

## 一、技术栈

| 层次 | 选型 |
| --- | --- |
| 后端框架 | Spring Boot 3.5.x + Spring Security + JWT（JDK 17） |
| 持久层 | MyBatis XML + PageHelper |
| 数据库 | MySQL 8.0（库名 `kids_care`） |
| 缓存 | Redis（默认 db 1） |
| 接口文档 | knife4j（若依自带 `tool/swagger`） |
| 前端框架 | Vue 3.5 + Vite 6 + Element Plus 2.13 + Pinia + ECharts 5 |
| 脚手架 | RuoYi-Vue 3.9.2 / RuoYi-Vue3 3.9.2 |

## 二、模块结构

```
kids-care-ruoyi
├── kids-admin              启动模块（RuoYiApplication，端口 8081）
├── kids-framework          安全 / 拦截器 / 权限（LoginUser 扩展了 roles 字段）
├── kids-system             若依原生：用户、角色、菜单、部门、字典、参数、日志
├── kids-common             通用工具（AjaxResult、BaseController、ExcelUtil…）
├── kids-quartz             定时任务
├── kids-generator          代码生成器
├── kids-care-platform      ★ 幼儿园业务模块（包 com.kids.care）
│   ├── domain              实体（含 @Excel 注解参与导入导出）
│   ├── mapper + resources/mapper/care/*.xml
│   ├── service / service.impl
│   ├── controller          13 个控制器，统一 /kids/** 前缀
│   └── utils/ScopeUtils    家长数据范围判定
└── sql                     建库、迁移、菜单、演示数据脚本
```

## 三、快速启动

### 0. 环境要求

| 依赖 | 要求 | 说明 |
| --- | --- | --- |
| JDK | **17**（必须） | 根 `pom` 的 `java.version=17`；不要用 JDK 21/23，JDK 23 起注解处理默认关闭会让 Lombok 静默失效 |
| Maven | 3.9.x | 依赖已缓存到本地仓库，可加 `-o` 离线构建 |
| MySQL | 8.0 | 库 `kids_care`，连接信息见 `kids-admin/src/main/resources/application-druid.yml` |
| Redis | 5+ | `localhost:6379`，**database 1**（`application.yml` 已配好，无密码） |

### 0.1 一键启动（Windows，推荐）

直接在资源管理器里双击，不用敲命令：

| 脚本 | 位置 | 作用 |
| --- | --- | --- |
| `启动后端.bat` | `kids-care-ruoyi\` | 自动设置 JDK 17 → 编译各模块 → 启动后端 `http://localhost:8081` |
| `启动前端.bat` | `kids-care-ruoyi-ui\` | 首次运行自动 `npm install` → 启动前端 `http://localhost:81` |
| `打开系统.url` | `kids-care-ruoyi-ui\` | 双击直接用浏览器打开 `http://localhost:81`（前后端已启动时用这个最方便） |

两个脚本都有**防重复启动**判断：如果对应端口已在运行，再双击只会打开浏览器，不会报「端口被占用」。

两个窗口都保持开着，浏览器访问 **http://localhost:81** 即可。

> ⚠️ **只能通过 `http://localhost:81` 访问**。不要去双击 `dist/index.html` 之类的文件打开——
> 那是生产构建产物，`file://` 方式打开所有资源都会加载失败，表现为整页空白。想重新生成
> `dist` 请执行 `npm run build:prod`，部署时交给 Nginx 等静态服务器，而不是本地双击。

### 1. 数据库与缓存（首次初始化）

```bash
mysql -uroot -p < sql/ry_20260417.sql      # 若依基础表 + 演示菜单（首次）
mysql -uroot -p < sql/kids_migrate_v1.sql  # 幼儿园业务表 + 业务数据
mysql -uroot -p < sql/kids_menu_v2.sql     # 业务菜单与角色授权（幂等，可重跑）
mysql -uroot -p < sql/kids_today_attendance.sql  # 可选：生成当天考勤，便于看板演示
```

> 演示数据说明：`kids_migrate_v1.sql` 已含 36 名幼儿、13 名教职工、4 个班级、课程、考勤、收费、食谱、食材与公告数据。

Redis 使用 `localhost:6379` 的 **db 1**（避免与其它项目抢 db 0），配置在 `application.yml` 的 `spring.data.redis.database`。

### 2. 后端

**方式 A：IDEA（最常用）**

1. 用 IDEA 打开 `kids-care-ruoyi` 根目录（Maven 项目）
2. `File → Project Structure → Project SDK` 选 **JDK 17**
3. 运行 `kids-admin/src/main/java/com/kids/RuoYiApplication.java` 的 `main` 方法

**方式 B：命令行**

```bash
cd kids-care-ruoyi
mvn -o -DskipTests install              # 首次 / 改过代码后都要执行
mvn -o -pl kids-admin spring-boot:run   # 启动，端口 8081
```

> 启动成功的标志是日志出现 `Started RuoYiApplication in x seconds`。
> 注意：`spring-boot:run` 读的是**本地仓库里安装过的 jar**，改了 `kids-care-platform` 的代码
> 必须先 `mvn -o -DskipTests install`，否则跑的还是旧代码。用 IDEA 直接运行则无此问题。

### 3. 前端

```bash
cd kids-care-ruoyi-ui
npm install          # 首次运行必须执行
npm run dev          # http://localhost:81，代理 /dev-api -> http://localhost:8081
```

> 前端 `.env.development` 中 `VITE_APP_BASE_API = '/dev-api'`，`vite.config.js` 顶部 `baseUrl` 指向后端；
> 若后端端口变化，只需改 `baseUrl` 一处。

> ⚠️ 交付前请在本目录**重新 `npm install` 一次**。当前 `node_modules` 若是指向其它工程的软链接，
> 换机器后 `npm run dev` 会直接报找不到 `vite`。
>
> ⚠️ `vite.config.js` 中的 `cacheDir: '.vite-cache'` 不要删。若沿用默认缓存目录，
> vite 会去清理共用 `node_modules/.vite`，触发本机安全删除策略导致服务起不来。

**访问 http://localhost:81**，用下表账号登录。

### 4. 常见问题

| 现象 | 原因与处理 |
| --- | --- |
| 启动报 `Could not resolve placeholder 'xss.excludes'` | `kids-admin/target/classes` 不完整（编译或 `spring-boot:run` 中途被中断，资源没拷全）。执行一次 `mvn -o -DskipTests install` 再启动 |
| 后端跑着跑着自己退出，日志有 `Unexpected end of ZLIB input stream` | devtools 监听到 `target/classes` 变化自动重启，正好撞上 Maven 写文件。启动时加 `-Dspring.devtools.restart.enabled=false`；用 IDEA 运行不受影响 |
| 8081 端口被占用 | 上次的 java 进程没退干净：用 `taskkill /F /PID <pid>` 结束（PID 从 `netstat -ano` 查） |
| 前端 `npm run dev` 提示找不到 vite | `node_modules` 是指向其它工程的软链接，在本目录重新 `npm install` |
| `vite build` 失败或 `dist` 删不掉 | 本机安全删除策略拦截，先手动删 `dist`，或加 `--emptyOutDir false` |
| **启动后页面空白，刷新一次才正常** | 依赖预构建缓存 `.vite-cache` 不完整。删掉 `kids-care-ruoyi-ui\.vite-cache` 目录后重新 `npm run dev`（会重建约 45MB 完整缓存） |
| 前端进程启动即退出，日志出现 `SAFE_DELETE_BULK_CONFIRM_REQUIRED` | vite 重建依赖时要删 50+ 个文件的临时目录，被本机安全删除策略拦下。同上：手动删 `.vite-cache` 再启动；**不要**在 `vite.config.js` 里加 `optimizeDeps.include`，改它必然触发重建 |
| 登录后侧边栏没有业务菜单 | 菜单脚本未执行或角色未授权，重新跑 `sql/kids_menu_v2.sql`（幂等） |

## 四、账号与角色

| 账号 | 密码 | 角色 | 数据范围 |
| --- | --- | --- | --- |
| `admin` | `123456` | 园长（超级管理员） | 全部功能与全部数据 |
| `teacher` | `123456` | 教师 | 业务数据可维护；教职工、收费只读；系统管理不可见 |
| `parent` | `123456` | 家长（张建国） | 仅「工作台 + 我的孩子」，只能看到自己绑定的幼儿 |

家长账号通过 `sys_user.child_id` 绑定幼儿，所有查询在 Service 层强制按该字段过滤，越权访问直接 403。

### 登录页

- 页面预填 `admin` / `123456`，打开即可直接点登录（`src/views/login.vue` 的 `loginForm` 默认值），账号密码以本表为准
- **验证码已启用**：图形验证码由后端 `GET /captchaImage` 生成（存 Redis `captcha_codes:{uuid}`，2 分钟有效），
  配置项为 `sys_config` 的 `sys.account.captchaEnabled`
- 关闭/开启验证码：改 `sys_config` 的值后同步改 Redis 缓存键 `sys_config:sys.account.captchaEnabled`
  （值是带引号的 JSON 字符串，如 `"true"`），或调 `PUT /system/config/refreshCache` 刷新缓存，无需重启
- 「记住账号」只保存用户名，密码不再写入 Cookie——否则浏览器里遗留的旧口令会把页面默认值覆盖掉
- **重新打开页面没有登录页、直接进入系统，是正常的**：登录凭证存在浏览器 Cookie（`Admin-Token`，
  会话 Cookie，关掉标签页不清除），且后端 Token 30 分钟内滑动续期。想回到登录页，二选一：
  ① 点右上角头像 → **退出登录**；② 关闭**所有**浏览器窗口再重开（会话 Cookie 随浏览器关闭而清除）。
  另外 Token 空闲超过 30 分钟会失效，之后访问会自动跳回登录页。

## 五、功能清单

| 模块 | 页面 | 主要能力 |
| --- | --- | --- |
| 工作台 | `kids/dashboard/index` | 幼儿/教职工/待审批/待缴四张卡片、近 7 天出勤趋势、班级人数分布、今日考勤、待缴提醒 |
| 幼儿管理 | 幼儿档案 | 分页检索、档案完整度、过敏清单、Excel 导入（含逐行错误提示）与导出、**下载导入模板**；出生/入园日期校验（出生不晚于今天、早于入园、入园时 3~6 岁）；**在园状态由入园日期自动判定**（未来日期=预约离园，到期自动转在园） |
| | 班级管理 | 人数与容量、满班状态、班主任设置（原班主任需显式替换）、班级幼儿名单 |
| | 教职工管理 | 在职统计、任教班级（班主任/任课）、任课课程、设为班主任、离职人员限制删除 |
| 教学管理 | 课程管理 | 按班级/教师/星期排课 |
| | 考勤管理 | 按日期/班级登记，重复登记自动复用（软删复活），自动带出班级 |
| | 请假管理 | 家长申请、教师审批（通过/驳回）、天数自动计算、防重复审批 |
| 后勤管理 | 每周食谱 | 周视图 3 餐 × 7 天、上一周/下一周、复制上周、打印 |
| | 食材库存 | 库存预警、出入库流水（记录 before/after）、超领拦截 |
| 财务与公告 | 收费管理 | 账单检索、确认收款（幂等，重复缴费报错） |
| | 通知公告 | 草稿 / 定时待发 / 已发布 / 已撤回 四状态机；定时发布选具体时间，后端每分钟扫描到点自动发布；可见范围（全园 / 指定班级，列表显示班级名）；家长端按孩子班级过滤 |
| | 数据报表 | 近 6 个月收费趋势、各班出勤率、今日考勤构成、库存预警与日志量 |
| 我的孩子 | `kids/my-child/index` | **仅家长角色**：档案卡、今日状态、近期考勤、待缴账单、请假记录与在线申请、园所公告 |

## 六、迁移时对齐的若依约定

- **响应体**：统一 `AjaxResult`（`code`/`msg`/`data`），列表用 `TableDataInfo`（`rows`/`total`），前端直接复用 `request.js` 拦截器。
- **分页**：Controller 里 `startPage()` + `getDataTable(list)`，前端 `<pagination>` 传 `pageNum`/`pageSize`。
- **权限**：菜单按钮级 `perms` 形如 `kids:child:add`，后端 `@PreAuthorize("@ss.hasPermi('kids:child:add')")`，前端 `v-hasPermi`。
- **角色判定**：`LoginUser` 增加 `roles` 集合（登录时由 `PermissionService.getRolePermission` 填充），`ScopeUtils.hasRole("parent")` 据此判断，避免污染 `permissions`。
- **审计字段**：实体继承 `BaseEntity`，`create_by`/`create_time` 由 `ZZYLMetaObjectHandler` 等价机制自动填充。
- **软删除**：业务表统一 `deleted` 标记；查询一律 `deleted = 0`；更新语句不写 `deleted`，避免部分更新把标记清空。
- **Excel**：`ExcelUtil<T>` + 实体 `@Excel` 注解，导入导出模板与错误汇总都在 `ChildController` 中实现。
- **数据库唯一键**：`uk_att(child_id, att_date)`、`uk_menu(menu_date, meal_type)` 与软删冲突时，采用「查软删记录 → 复活」而非直接插入。

## 七、接口速查

```
GET  /kids/stat/overview            看板汇总（家长不下发待缴明细）
GET  /kids/stat/report              报表数据（kids:report:query）
GET  /kids/clazz/overview           班级统计（人数/容量/出勤率）
GET  /kids/teacher/overview         教职工统计
GET  /kids/teacher/{id}/classes     教师任教班级
PUT  /kids/teacher/{id}/head-teacher/{clazzId}?replace=true
GET  /kids/food/overview            库存概览与预警
POST /kids/food/{id}/stock          出入库（body: type/quantity/remark）
GET  /kids/menu/week?weekStart=     周食谱
POST /kids/menu/copy-last-week      复制上周食谱
POST /kids/child/import             幼儿档案导入
POST /kids/child/export             幼儿档案导出
PUT  /kids/fee/pay/{id}             确认收款
PUT  /kids/leave/approve/{id}?pass= 请假审批
GET  /kids/my-child/info            家长端聚合（档案+考勤+账单+请假+公告）
POST /kids/my-child/leave           家长提交请假
```

## 八、已知取舍

- 系统管理、系统监控、系统工具保留若依原生页面，仅园长可见（教师角色未授权这些菜单）。
- 家长账号若未绑定幼儿，进入「我的孩子」会提示联系园方。
- 「我的孩子」是**家长专属**页面：菜单 2500 / 2510 的 `remark` 标为 `parent-only`，
  `SysMenuServiceImpl.selectMenuTreeByUserId` 在超级管理员分支显式剔除，园长登录后看不到该入口（也无法通过 URL 访问）。
- `kids_today_attendance.sql` 只用于演示：给当天补一份混合状态的全园考勤，可重复执行（已存在则跳过）。
- 在园状态不接受手工指定：保存时按入园日期落库，查询时用 `enroll_date > curdate()` 实时重算，
  因此「离园」只表示「预约未到/未入园」。若将来要做退园，需要新增独立的退园日期字段。
- 顶部工作台的统计口径见根目录 [`README.md`](../README.md) 与 [`CHANGELOG-修改日志.md`](../CHANGELOG-修改日志.md)。

---

本项目基于 [RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue) 3.9.2 构建，遵循其 MIT 许可。
