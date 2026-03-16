# Fast 系统架构优化计划

## 1. 背景与目标

当前 `fast-*` 项目已经具备多模块 Maven 结构，但模块边界仍然偏弱，主要问题是：

- `fast-system-api / biz / facade` 分层存在，但边界没有真正做实
- `fast-framework`、`fast-utils`、`fast-database` 职责交叉
- `fast-auth`、缓存、审计、数据库元数据填充之间存在跨层耦合
- 未来要同时支撑“系统管理模块 + 多 App 接口 + 独立业务模块”，现有结构扩展成本偏高

本次优化的核心目标不是继续增加模块数量，而是建立一套可长期演进的模块化单体结构，满足以下需求：

- 系统管理能力作为平台底座长期稳定存在
- 后续可以面向多个 App 暴露不同接口
- 可以持续增加独立业务模块，而不破坏已有模块边界
- 基础设施能力可复用，但不侵入业务模块
- 未来若有需要，可逐步向服务化拆分演进

## 2. 总体设计原则

- 单体优先，边界清晰优先，不为“看起来高级”过度拆模块
- 业务模块按领域划分，技术模块按能力划分
- 启动层只负责装配，不承载业务
- 控制器层、应用服务层、领域服务层、持久化层职责明确
- 所有依赖保持单向流动，禁止跨层反向依赖
- 通用能力沉淀到基础模块，但不把业务语义塞进基础模块

## 3. 目标架构

建议将项目逐步演进为如下结构：

```text
fast-admin
├── fast-dependencies        统一依赖版本管理（可选，第二阶段引入）
├── fast-boot               启动与装配层
├── fast-common             通用能力层
├── fast-framework-web      Web 公共能力
├── fast-framework-data     数据访问公共能力
├── fast-framework-cache    缓存公共能力
├── fast-module-system      系统管理模块
├── fast-module-app         面向多 App 的接口聚合层
├── fast-module-ai          AI 独立业务模块
└── fast-module-xxx         后续新增独立业务模块
```

如果短期不想一次性引入太多新模块，可以先采用中间态：

```text
fast-admin
├── fast-application        启动与装配层
├── fast-common             通用能力
├── fast-framework-web      Web 能力
├── fast-framework-data     数据能力
├── fast-framework-cache    缓存能力
├── fast-auth               认证授权模块
├── fast-system             系统管理模块
├── fast-app                多 App 接口层
├── fast-ai                 AI 模块
└── fast-module-xxx         其它业务模块
```

## 4. 推荐模块职责

### 4.1 启动装配层

模块建议：`fast-boot` 或保留 `fast-application`

职责：

- Spring Boot 启动入口
- 配置文件聚合
- 装配需要启用的业务模块
- 环境区分、组件扫描、自动配置启用

约束：

- 不写业务逻辑
- 不放 Controller、Service、Mapper
- 只负责“引模块”

### 4.2 通用能力层

模块建议：`fast-common`

职责：

- 统一响应模型
- 统一异常定义
- 错误码规范
- 通用常量
- 纯工具类
- 审计上下文模型
- 通用 DTO 基类、分页模型

约束：

- 不依赖 Spring Web、MyBatis、Redis
- 不定义任何业务实体和业务服务

### 4.3 Web 公共能力层

模块建议：`fast-framework-web`

职责：

- 全局异常处理
- Web MVC 配置
- 参数绑定与校验
- 接口响应封装
- 控制器公共能力
- 多 App 路由隔离规范

约束：

- 只处理 Web 共性问题
- 不放业务 Controller

### 4.4 数据公共能力层

模块建议：`fast-framework-data`

职责：

- MyBatis / MyBatis-Plus 配置
- Mapper 扫描
- 分页拦截器
- 通用 BaseEntity / BaseMapper / BaseService
- 审计字段自动填充

约束：

- 不依赖业务模块
- 不放具体业务表 Mapper

### 4.5 缓存公共能力层

模块建议：`fast-framework-cache`

职责：

- Redis 配置
- Cache Key 规范
- Redis 操作封装
- 二级缓存与热点缓存能力

约束：

- 不承载业务缓存策略
- 业务缓存 key 放在对应业务模块中定义

### 4.6 系统管理模块

模块建议：`fast-module-system`

职责：

- 用户、角色、部门、菜单、权限
- 登录用户信息、权限编码、组织结构
- 系统配置、字典、租户、通知等平台底座能力
- 对外暴露“系统能力接口”，供其它模块调用

建议内部结构：

```text
fast-module-system
├── controller
│   ├── admin
│   └── app
├── service
├── convert
├── dal
│   ├── dataobject
│   └── mapper
├── enums
├── api
└── framework
```

说明：

- `api` 只放其它模块需要调用的稳定接口与 DTO
- `controller.admin` 面向后台管理端
- `controller.app` 面向 App 端
- 不再单独拆 `api / biz / facade` 三个 Maven 子模块，先做模块内分层

### 4.7 认证授权模块

模块建议：保留 `fast-auth`

职责：

- 登录、登出、刷新 token
- 登录态与权限校验
- 鉴权拦截器
- 认证上下文构建

依赖建议：

- 依赖 `fast-common`
- 依赖 `fast-framework-web`
- 依赖 `fast-framework-cache`
- 依赖 `fast-module-system` 的公开接口

边界约束：

- 不直接依赖系统模块内部实现类
- 不依赖具体 Mapper
- 不通过 Redis 保存“必须正确”的用户真相数据

### 4.8 多 App 接口层

模块建议：`fast-app`

职责：

- 统一承接未来不同 App 的接口出口
- 按 App 维度组织接口，如 `app-admin`、`app-user`、`app-mobile`
- 处理不同终端的鉴权策略、返回结构和路由分组

建议结构：

```text
fast-app
├── admin
├── user
├── mobile
└── openapi
```

说明：

- 这里主要承担“接口编排”和“终端差异隔离”
- 真正业务逻辑仍然放在 `fast-module-system` 或独立业务模块

### 4.9 独立业务模块

模块建议：`fast-module-xxx`

职责：

- 承载某一个完整业务域
- 独立定义 Controller、Service、DAL、枚举、事件
- 可按需要依赖 `system` 模块暴露的用户/组织/权限能力

适用场景：

- 活动管理
- 工单管理
- 任务中心
- AI 辅助功能
- 设备管理
- 支付与订单

## 5. 推荐依赖关系

目标依赖方向如下：

```text
fast-boot
  -> fast-auth
  -> fast-app
  -> fast-module-system
  -> fast-module-ai
  -> fast-module-xxx
  -> fast-framework-web
  -> fast-framework-data
  -> fast-framework-cache
  -> fast-common

fast-app
  -> fast-auth
  -> fast-module-system
  -> fast-module-ai
  -> fast-module-xxx
  -> fast-common

fast-auth
  -> fast-module-system(api)
  -> fast-framework-web
  -> fast-framework-cache
  -> fast-common

fast-module-system
  -> fast-framework-data
  -> fast-framework-cache
  -> fast-framework-web
  -> fast-common

fast-module-ai / fast-module-xxx
  -> fast-module-system(api)
  -> fast-framework-data
  -> fast-framework-cache
  -> fast-common
```

禁止出现以下依赖：

- `fast-common -> Spring Web / MyBatis / Redis`
- `fast-auth -> system 内部 service 实现类`
- `业务模块 A -> 业务模块 B 的 controller / mapper`
- `app 接口层 -> 直接操作 mapper`
- `framework 模块 -> 反向依赖业务模块`

## 6. 重点优化项

### 6.1 重构 `fast-system`

当前问题：

- `fast-system-api / biz / facade` 形式上拆分，但代码边界没有真正稳固

调整建议：

- 收敛成一个 Maven 模块 `fast-module-system`
- 在模块内部用包结构分层，而不是先拆过细的 Maven 模块
- 如果后续确实要 RPC 或远程服务化，再单独抽 `api` 包或 `client` 包

### 6.2 拆分 `fast-framework`

当前问题：

- 一个模块同时承载 Web、异常、MyBatis、控制器基类等多种职责

调整建议：

- 拆为 `fast-framework-web` 与 `fast-framework-data`
- 纯通用模型迁移到 `fast-common`

### 6.3 收敛 `fast-utils`

当前问题：

- 纯工具与 Spring Bean 混放

调整建议：

- 纯静态工具保留到 `fast-common`
- `RedisUtil` 迁移到 `fast-framework-cache`
- `AuditContextHolder` 迁移到 `fast-common` 或 `fast-framework-web`

### 6.4 优化认证与审计链路

当前问题：

- 认证模块与缓存、审计、数据库元数据填充之间存在过深耦合

调整建议：

- 审计上下文应来自登录上下文，而不是 Redis 缓存昵称
- Redis 只做缓存，不做强一致身份来源
- 数据层只消费审计上下文，不感知 auth 细节

### 6.5 提前设计多 App 接口规范

建议统一路由分组：

- `/admin-api/**` 后台管理端
- `/app-api/**` 终端 App
- `/open-api/**` 开放接口

建议统一控制器包路径：

- `controller.admin`
- `controller.app`
- `controller.openapi`

这样后续新增 App 时，不需要再改业务模块边界，只需扩展接口层。

## 7. 分阶段实施计划

### 阶段一：边界收敛

目标：

- 先把当前混乱的职责边界拉开

任务：

- 新增 `fast-common`
- 新增 `fast-framework-web`
- 新增 `fast-framework-data`
- 新增 `fast-framework-cache`
- 将 `fast-framework`、`fast-utils` 的代码迁移到新模块
- 保留旧模块一段时间做兼容过渡

交付结果：

- 基础能力边界清晰
- 业务模块不再直接依赖杂糅基础包

### 阶段二：系统模块重组

目标：

- 将 `fast-system-api / fast-system-biz / fast-system-facade` 重组为单一系统模块

任务：

- 建立 `fast-module-system`
- 按 `controller / service / dal / api / convert / enums` 重组代码
- 统一系统模块对外能力接口
- 调整 `fast-auth` 对系统能力的调用方式

交付结果：

- 系统管理模块成为稳定底座
- 权限、用户、组织结构成为其它模块共享能力

### 阶段三：多 App 接口层建设

目标：

- 给未来不同 App 提供统一出口

任务：

- 新增 `fast-app`
- 按终端建立 `admin / app / openapi` 接口目录
- 明确路由前缀、鉴权策略、VO 规范
- 将终端差异从业务模块中剥离

交付结果：

- 同一业务能力可以服务多个终端
- 接口组织方式稳定可扩展

### 阶段四：业务模块化扩展

目标：

- 支撑新增独立业务模块

任务：

- 将 `fast-ai` 重构为真实独立业务模块
- 未来新增 `fast-module-task`、`fast-module-activity`、`fast-module-order` 等
- 统一新模块模板与开发规范

交付结果：

- 后续业务扩展不再破坏系统底座

## 8. 模块命名建议

推荐命名方式统一为：

- `fast-common`
- `fast-framework-web`
- `fast-framework-data`
- `fast-framework-cache`
- `fast-auth`
- `fast-module-system`
- `fast-module-ai`
- `fast-module-xxx`
- `fast-app`
- `fast-boot`

命名原则：

- `framework-*` 代表技术基础设施
- `module-*` 代表业务域模块
- `boot` 代表启动装配
- `app` 代表终端接口编排

## 9. 研发规范建议

- Controller 只做参数接收、结果返回、鉴权声明
- Service 才承载业务逻辑
- Mapper 只做数据访问
- DTO / VO / DO 严格区分
- 所有输入对象使用参数校验
- 所有业务错误使用统一错误码
- 跨模块调用优先通过公开 API，而不是直接引用内部实现
- 新增业务模块时，先定义模块边界，再开始写代码

## 10. 本次改造的结论

本次优化建议采用“模块化单体 + 平台底座 + 多 App 接口层 + 独立业务模块”的方向。

短期重点不是继续拆更多 Maven 模块，而是：

- 先把基础能力分清
- 再把系统模块做实
- 再建立面向多终端的接口层
- 最后平滑扩展独立业务模块

这条路径兼顾当前项目规模、未来可扩展性和后续维护成本，适合作为 `fast` 系统后续 1 到 2 个版本的架构演进路线。
