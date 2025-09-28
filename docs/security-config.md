# 安全配置

## 🔐 JWT认证

- **访问令牌**: 15分钟有效期
- **刷新令牌**: 7天有效期
- **签名算法**: HS256
- **令牌存储**: Redis缓存

## 🛡️ 安全策略

- **路径保护**: 基于路径的权限控制
- **角色授权**: RBAC角色权限模型
- **内部调用**: 内部服务间使用专用Token
- **跨域配置**: 支持前端跨域访问

## 🏗️ 安全架构

### 模块化安全设计

基于重构后的模块结构，安全功能分布如下：

```
kawaii-common-core/          # 基础安全组件
├── enums/ApiCode.java       # 统一错误码定义
├── response/R.java          # 统一响应格式
└── exception/BusinessException.java

kawaii-common-spring/        # Spring安全基础设施
├── context/UserContextHolder.java  # 用户上下文访问
├── exception/GlobalExceptionHandler.java  # 通用异常处理
└── config/                  # Spring配置类

kawaii-common-business/      # 业务安全逻辑
├── auth/JwtTokenManager.java          # JWT统一管理
├── auth/SecurityExceptionHandler.java # Spring Security异常处理
└── audit/OperationAuditService.java   # 操作审计
```

### 安全边界设计

- **Gateway层**: 统一认证入口，JWT验证，用户上下文传递
- **微服务层**: 信任Gateway传递的用户信息，专注业务逻辑
- **模块依赖**: Gateway仅依赖core模块，避免Spring Security传递依赖

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