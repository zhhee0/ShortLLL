# ShortLLL

一个基于 Java 17 + Spring Boot 的短链接系统，覆盖短链创建、跳转、分组管理、回收站、访问统计，以及简单内容广场能力。

## 项目简介

这个项目的核心目标是搭建一套可落地的短链接系统，重点关注：

- 短链高并发跳转
- 多用户场景下的数据隔离
- 访问统计异步化
- 缓存穿透、击穿与一致性处理
- 后台管理与分组维度统计

## 核心亮点

- 基于自定义拦截器实现统一登录校验，结合 `ThreadLocal` 维护用户上下文，支撑多用户场景下的数据隔离与分组访问控制
- 使用 RabbitMQ 异步化访问统计写入，利用消息队列削峰填谷，保障高并发场景下的稳定落库
- 结合 Redis 实现消费端时间窗口幂等控制，降低重复消费对访问统计结果的影响
- 短链跳转场景下，结合布隆过滤器、空值缓存、分布式锁与双重检查机制，防止缓存穿透与缓存击穿
- 基于 Cache Aside 模式，采用先更新数据库、再删除缓存的策略，降低缓存与数据库不一致风险
- 使用 Redisson 分布式读写锁，保证短链迁组与异步统计并发场景下的数据一致性
- 基于 Redis、AOP 和自定义注解实现用户维度、接口维度的限流能力，应用于短链创建、短链修改及后台统计查询等核心接口

## 技术栈

### 后端

- Java 17
- Spring Boot 3
- Spring Cloud
- MyBatis-Plus
- MySQL
- Redis
- RabbitMQ
- Redisson
- ShardingSphere

### 前端

- Vue 3
- Vite
- Pinia
- Element Plus

## 项目结构

```text
.
├── admin         # 后台管理服务
├── project       # 短链核心业务服务
├── gateway       # 网关层
├── aggregation   # 聚合层
├── console-vue   # 前端控制台
└── resources     # SQL、部署资源等
```

## 典型业务流程

### 1. 短链跳转

1. 根据域名 + 短码拼出完整短链
2. 优先查询 Redis 跳转缓存
3. 未命中时先过布隆过滤器和空值缓存
4. 必要时通过分布式锁 + 双重检查回源数据库
5. 命中有效短链后执行 302 跳转
6. 同时异步发送访问统计消息到 RabbitMQ

### 2. 访问统计

1. 跳转主链路先组装 PV / UV / UIP 相关统计消息
2. 通过 RabbitMQ 异步投递
3. 消费端先基于 Redis `messageId` 做时间窗口幂等判断
4. 成功后写入访问明细、地区、浏览器、设备、网络及汇总统计

### 3. 缓存一致性

- 读：先查缓存，缓存未命中再查数据库并回填
- 写：先更新数据库，再删除缓存
- 适用场景：短链修改、移入回收站、回收站恢复等

## 本地启动

### 后端

在项目根目录执行：

```bash
./mvnw -pl aggregation -am package
./mvnw -pl aggregation -am spring-boot:run
```

如果本地使用私有配置，先在 PowerShell 中加载本地环境变量：

```powershell
. .\local-dev.ps1
```

或按模块分别启动：

```bash
./mvnw -pl admin -am spring-boot:run
./mvnw -pl project -am spring-boot:run
./mvnw -pl gateway -am spring-boot:run
```

### 前端

在 `console-vue` 目录执行：

```bash
pnpm install
pnpm dev
```

## 数据库脚本

- 主脚本：`resources/database/link.sql`
- 额外内容脚本：`resources/t_story.sql`

## 注意事项

- 配置文件中的数据库、Redis、MQ 地址需要根据本地环境调整
- `application*.yaml` 和 `shardingsphere-config-*.yaml` 建议通过环境变量或本地覆盖文件管理敏感配置
- 当前仓库包含较多实验性和业务扩展代码，正式部署前建议先梳理配置与依赖

## 后续可优化方向

- 将访问统计从 Redis 时间窗口幂等升级为持久化消费幂等
- 对超热点短链的 UV / UIP 判重做大 key 优化
- 进一步拆分统计模型，降低迁组与统计之间的耦合
- 增强消息链路监控、补偿与告警能力
