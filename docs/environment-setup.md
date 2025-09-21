# 环境配置指南

## ⚙️ 配置策略

本项目采用**统一Nacos配置中心 + 命名空间环境分离**的配置策略：

- **所有环境**: 统一使用Nacos配置中心
- **环境分离**: 通过Nacos命名空间分离不同环境配置
- **本地开发**: 可选择性连接本地Nacos或使用内置配置

## 📁 配置文件结构

```
src/main/resources/
└── application.yml          # 唯一配置文件，通过环境变量控制Nacos命名空间
```

## 🏷️ Nacos命名空间设计

| 命名空间ID | 环境 | 描述 | Nacos地址 |
|------------|------|------|-----------|
| kawaii-local | 本地开发 | 开发人员本地调试 | localhost:8848 |
| kawaii-dev | 开发环境 | 团队联调测试 | nacos-dev.company.com:8848 |
| kawaii-test | 测试环境 | QA测试验证 | nacos-test.company.com:8848 |
| kawaii-staging | 预发布环境 | 生产前验证 | nacos-staging.company.com:8848 |
| kawaii-prod | 生产环境 | 正式生产环境 | nacos-prod.company.com:8848 |

## 🎯 配置原则

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

### 配置示例文件

项目提供了完整的配置示例文件：

```
docs/nacos-configs/
├── local/kawaii-common.yml      # 本地开发环境配置
├── dev/kawaii-common.yml        # 开发环境配置
├── test/kawaii-common.yml       # 测试环境配置
├── staging/kawaii-common.yml    # 预发布环境配置
└── prod/kawaii-common.yml       # 生产环境配置
```

### 配置部署步骤

1. 登录对应环境的Nacos控制台
2. 创建对应的命名空间（如：kawaii-dev）
3. 在命名空间下创建配置文件：`kawaii-common.yml`
4. 复制对应环境的配置内容到Nacos中
5. 发布配置

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
