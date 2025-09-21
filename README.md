# KawaiiChain Wallet 后端服务

基于Spring Cloud微服务架构的数字钱包后端系统。

## 🏗️ 项目架构

### 微服务模块

| 服务名 | 端口 | 描述 | 技术栈 |
|--------|------|------|-------|
| kawaii-gateway | 8080 | API网关 | Spring Cloud Gateway |
| kawaii-user | 8082 | 用户服务 | Spring Boot + MyBatis-Plus |
| kawaii-core | 8083 | 钱包核心服务 | Spring Boot + Web3j |
| kawaii-payment | 8084 | 支付服务 | Spring Boot |
| kawaii-common | - | 公共组件库 | Spring Boot Starter |
| kawaii-api | - | API接口定义 | Feign Client |

### 技术栈

- **框架**: Spring Boot 3.x + Spring Cloud 2023.x
- **服务发现**: Nacos Discovery
- **配置中心**: Nacos Config
- **数据库**: PostgreSQL 17
- **缓存**: Redis 7.0
- **ORM**: MyBatis-Plus
- **服务调用**: OpenFeign
- **API网关**: Spring Cloud Gateway
- **文档**: SpringDoc OpenAPI 3

## ⚙️ 环境配置

### 配置策略

本项目采用**统一Nacos配置中心 + 命名空间环境分离**的配置策略：

- **所有环境**: 统一使用Nacos配置中心
- **环境分离**: 通过Nacos命名空间分离不同环境配置
- **本地开发**: 可选择性连接本地Nacos或使用内置配置

### 配置文件结构

```
src/main/resources/
└── application.yml          # 唯一配置文件，通过环境变量控制Nacos命名空间
```

### Nacos命名空间设计

| 命名空间ID | 环境 | 描述 | Nacos地址 |
|------------|------|------|-----------|
| kawaii-local | 本地开发 | 开发人员本地调试 | localhost:8848 |
| kawaii-dev | 开发环境 | 团队联调测试 | nacos-dev.company.com:8848 |
| kawaii-test | 测试环境 | QA测试验证 | nacos-test.company.com:8848 |
| kawaii-staging | 预发布环境 | 生产前验证 | nacos-staging.company.com:8848 |
| kawaii-prod | 生产环境 | 正式生产环境 | nacos-prod.company.com:8848 |

### 配置原则

1. **统一配置文件**: 每个微服务只有一个 `application.yml`
2. **环境变量控制**: 通过 `NACOS_NAMESPACE` 等环境变量切换环境
3. **Nacos配置导入**: 统一从 `kawaii-common.yaml` 导入公共配置
4. **命名空间隔离**: 不同环境配置完全隔离，避免误操作

## 🚀 快速开始

### 前置要求

- JDK 21+
- Maven 3.9+
- PostgreSQL 17
- Redis 7.0
- Node.js 18+ (前端开发)

### 本地开发

#### 1. 启动基础设施

```bash
# 启动PostgreSQL (Docker)
docker run --name kawaii-postgres \
  -e POSTGRES_DB=kawaii_wallet \
  -e POSTGRES_USER=rw \
  -e POSTGRES_PASSWORD=Admin!123 \
  -p 5432:5432 -d postgres:17

# 启动Redis (Docker)
docker run --name kawaii-redis \
  -p 6379:6379 \
  -d redis:7.0 --requirepass 123456

# 启动Nacos (Docker) - 本地开发
docker run --name nacos-standalone \
  -e MODE=standalone \
  -p 8848:8848 \
  -d nacos/nacos-server:v2.3.0
```

#### 2. 配置Nacos

在Nacos控制台 (http://localhost:8848/nacos) 中：
1. 创建命名空间：`kawaii-local`
2. 在该命名空间下创建配置文件：`kawaii-common.yml`
3. 将 `docs/nacos-configs/local/kawaii-common.yml` 内容复制到配置中

#### 3. 启动微服务

```bash
# 启动网关服务 (本地环境，默认连接localhost:8848的kawaii-local命名空间)
cd kawaii-gateway
mvn spring-boot:run

# 启动用户服务
cd kawaii-user
mvn spring-boot:run

# 启动其他服务...
```

默认使用 `local` profile，自动连接本地Nacos的 `kawaii-local` 命名空间。

### 其他环境部署

通过环境变量控制不同环境的Nacos连接：

#### 开发环境

```bash
# 设置环境变量
export SPRING_PROFILES_ACTIVE=dev
export NACOS_SERVER_ADDR=nacos-dev.company.com:8848
export NACOS_NAMESPACE=kawaii-dev
export NACOS_USERNAME=nacos
export NACOS_PASSWORD=nacos

# 启动服务
java -jar kawaii-gateway.jar
```

#### 测试环境

```bash
# 设置环境变量
export SPRING_PROFILES_ACTIVE=test
export NACOS_SERVER_ADDR=nacos-test.company.com:8848
export NACOS_NAMESPACE=kawaii-test

# 启动服务
java -jar kawaii-gateway.jar
```

#### 预发布环境

```bash
# 设置环境变量
export SPRING_PROFILES_ACTIVE=staging
export NACOS_SERVER_ADDR=nacos-staging.company.com:8848
export NACOS_NAMESPACE=kawaii-staging

# 启动服务
java -jar kawaii-gateway.jar
```

#### 生产环境

```bash
# 设置环境变量
export SPRING_PROFILES_ACTIVE=prod
export NACOS_SERVER_ADDR=nacos-prod.company.com:8848
export NACOS_NAMESPACE=kawaii-prod

# 启动服务
java -jar kawaii-gateway.jar
```

## 📝 Nacos配置中心

### 命名空间配置

本项目采用Nacos命名空间进行环境隔离，每个环境有独立的命名空间和配置：

| 命名空间 | 环境 | 配置文件 |
|----------|------|----------|
| kawaii-local | 本地开发 | kawaii-common.yml |
| kawaii-dev | 开发环境 | kawaii-common.yml |
| kawaii-test | 测试环境 | kawaii-common.yml |
| kawaii-staging | 预发布环境 | kawaii-common.yml |
| kawaii-prod | 生产环境 | kawaii-common.yml |

### 配置文件结构

#### kawaii-common.yml (公共配置)

每个命名空间下都有一个 `kawaii-common.yml` 文件，包含该环境的所有微服务共享配置：
- 数据库连接配置
- Redis配置
- 日志配置
- JWT配置
- 业务配置

### 配置部署步骤

1. 登录对应环境的Nacos控制台
2. 创建对应的命名空间（如：kawaii-dev）
3. 在命名空间下创建配置文件：`kawaii-common.yml`
4. 复制对应环境的配置内容到Nacos中
5. 发布配置

## 🌐 API路由规范

### 网关路由规则

```
外部请求 → 网关 → 微服务

/api/v1/user/**    → kawaii-user
/api/v1/core/**    → kawaii-core
/api/v1/payment/** → kawaii-payment
```

### 路由特点

- **统一入口**: 所有API请求通过网关 `/api/v1/{serviceName}/**` 格式
- **路径重写**: 网关自动去除 `/api/v1/{serviceName}` 前缀转发给微服务
- **版本管理**: API版本统一在网关层管理
- **服务解耦**: 微服务内部不感知API版本前缀

### API示例

```bash
# 用户注册
POST /api/v1/user/register

# 用户登录
POST /api/v1/user/login

# 获取钱包余额
GET /api/v1/core/wallet/{userId}/balance

# 发起支付
POST /api/v1/payment/orders
```

## 🔧 错误码管理

项目使用统一的错误码管理：

- **位置**: `kawaii-common/src/main/java/com/kawaiichainwallet/common/enums/ApiCode.java`
- **使用**: 所有API响应统一使用 `R<T>` 包装器和 `ApiCode` 枚举
- **分类**: 按业务模块划分错误码区间

### 错误码分类

| 类别 | 区间 | 说明 |
|------|------|------|
| 成功响应 | 200 | 操作成功 |
| 客户端错误 | 4xx | 请求错误 |
| 服务端错误 | 5xx | 服务器错误 |
| 业务错误 | 1xxx | 参数验证等 |
| 用户相关 | 2xxx | 用户业务错误 |
| 验证码相关 | 3xxx | OTP验证错误 |
| Token相关 | 4xxx | 认证授权错误 |
| 钱包相关 | 5xxx | 钱包业务错误 |
| 支付相关 | 6xxx | 支付业务错误 |

## 🔐 安全配置

### JWT认证

- **访问令牌**: 15分钟有效期
- **刷新令牌**: 7天有效期
- **签名算法**: HS256
- **令牌存储**: Redis缓存

### 安全策略

- **路径保护**: 基于路径的权限控制
- **角色授权**: RBAC角色权限模型
- **内部调用**: 内部服务间使用专用Token
- **跨域配置**: 支持前端跨域访问

## 📊 监控和管理

### Actuator端点

本地开发环境暴露的监控端点：
- `/actuator/health` - 健康检查
- `/actuator/info` - 应用信息
- `/actuator/metrics` - 指标监控
- `/actuator/gateway` - 网关路由信息

### 日志配置

- **本地环境**: DEBUG级别，控制台输出
- **其他环境**: 通过Nacos配置，支持动态调整
- **格式**: 统一的日志格式，便于日志收集

## 🗃️ 数据库设计

### 数据库分离

- **用户服务**: 用户信息、认证数据
- **钱包服务**: 钱包、交易记录
- **支付服务**: 支付订单、商户信息

### 数据库特性

- **软删除**: 使用 `deleted` 字段标记删除
- **审计字段**: 自动维护创建时间、更新时间
- **连接池**: HikariCP高性能连接池
- **事务管理**: 基于Spring的声明式事务

---

# application.yml 配置示例

每个微服务的 `application.yml` 统一配置格式：


## 📋 配置说明

### 环境变量控制

通过环境变量控制应用连接到不同的Nacos命名空间：

```bash
# 本地开发环境 (默认)
export SPRING_PROFILES_ACTIVE=local
export NACOS_SERVER_ADDR=localhost:8848
export NACOS_NAMESPACE=kawaii-local

# 开发环境
export SPRING_PROFILES_ACTIVE=dev
export NACOS_SERVER_ADDR=nacos-dev.company.com:8848
export NACOS_NAMESPACE=kawaii-dev

# 生产环境
export SPRING_PROFILES_ACTIVE=prod
export NACOS_SERVER_ADDR=nacos-prod.company.com:8848
export NACOS_NAMESPACE=kawaii-prod

# 敏感配置通过环境变量注入到Nacos配置中
export DB_USERNAME=kawaii_user
export DB_PASSWORD=your_secure_password
export JWT_SECRET=your-super-secret-jwt-key
export SMS_ACCESS_KEY_ID=your-access-key
export SMS_ACCESS_KEY_SECRET=your-secret
```

### 配置优先级

1. **环境变量** (最高优先级)
2. **Nacos配置中心 kawaii-common.yaml**
3. **application.yml** (最低优先级)

### 配置管理策略

- **单一配置文件**: 每个微服务只维护一个 `application.yml`
- **命名空间隔离**: 通过Nacos命名空间完全隔离不同环境配置
- **环境变量注入**: 敏感配置通过环境变量注入，不在配置文件中明文存储
- **统一配置中心**: 所有环境共享配置统一在Nacos中管理
