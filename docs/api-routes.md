# API路由规范

## 🌐 网关路由规则

基于 Spring Cloud Gateway 的自动服务发现路由：

```
外部请求 → 网关 → 微服务

/kawaii-user/**       → kawaii-user (http://localhost:8091)
/kawaii-core/**       → kawaii-core (http://localhost:8083)
/kawaii-payment/**    → kawaii-payment (http://localhost:8084)
/kawaii-merchant/**   → kawaii-merchant (http://localhost:8085)
/kawaii-notification/** → kawaii-notification (http://localhost:8086)
```

## 🔄 路径重写规则

网关自动应用以下重写规则：

```
原始请求路径: /kawaii-user/auth/send-register-otp
↓ (正则匹配: /kawaii-user/?(?<remaining>.*))
↓ (捕获 remaining = "auth/send-register-otp")
↓ (重写为: /${remaining})
转发路径: /auth/send-register-otp
```

## 🔗 路由特点

- **自动服务发现**: 基于 Nacos 服务注册中心的自动路由发现
- **路径重写**: 网关自动去除服务名前缀 `/kawaii-{service}/` 转发给对应微服务
- **负载均衡**: 支持多实例服务的负载均衡
- **服务解耦**: 微服务内部使用标准的 REST 路径，不感知服务名前缀

## 🔒 安全配置

### 公开路径 (无需认证)
```yaml
public-paths:
  - "/kawaii-user/auth/**"        # 用户认证相关接口
  - "/kawaii-user/check-**"       # 用户检查接口
  - "/kawaii-core/health"         # 健康检查
  - "/kawaii-payment/webhook/**"  # 支付回调
  - "/actuator/**"                # 监控端点
  - "/health"                     # 健康检查
```

### 内部路径 (阻止外部直接访问)
```yaml
internal-paths:
  - "/kawaii-*/internal/**"
```

## 🌐 访问方式说明

### 正确的访问方式 ✅ (统一通过网关)
```bash
# 所有API请求都应该通过网关访问
curl http://localhost:8090/kawaii-user/auth/send-register-otp
curl http://localhost:8090/kawaii-core/health
curl http://localhost:8090/kawaii-payment/orders

# 需要认证的接口携带JWT Token
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8090/kawaii-user/users/profile
```

### 错误的访问方式 ❌
```bash
# 错误1：直接访问微服务端口
curl http://localhost:8091/auth/send-register-otp
# 原因：缺少Gateway传递的用户上下文信息，可能导致认证失败

# 错误2：直接向微服务发送包含服务名的路径
curl http://localhost:8091/kawaii-user/auth/send-register-otp
# 原因：会返回404或静态资源错误，微服务不认识服务名前缀
```

### 为什么必须通过网关？
- **用户上下文**: Gateway解析JWT并设置用户信息Headers (X-User-Id, X-User-Email等)
- **认证状态**: Gateway设置X-Authenticated状态，微服务依赖此信息
- **统一鉴权**: 公开路径和内部路径的访问控制在Gateway层实现
- **生产一致**: 保持开发调试与生产环境的访问方式完全一致

## 📖 API文档

各微服务的详细API文档请访问对应的 Swagger UI：

- **用户服务**: http://localhost:8091/swagger-ui.html
- **核心服务**: http://localhost:8083/swagger-ui.html
- **支付服务**: http://localhost:8084/swagger-ui.html
- **商户服务**: http://localhost:8085/swagger-ui.html
- **通知服务**: http://localhost:8086/swagger-ui.html

## 🔧 开发调试

- **网关端口**: 8090 (统一访问入口)
- **Nacos控制台**: http://localhost:8848/nacos
- **路由发现**: 网关启动时自动发现并输出路由规则日志
