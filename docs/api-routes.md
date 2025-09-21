# API路由规范

## 🌐 网关路由规则

```
外部请求 → 网关 → 微服务

/api/v1/user/**    → kawaii-user
/api/v1/core/**    → kawaii-core
/api/v1/payment/** → kawaii-payment
```

## 🔗 路由特点

- **统一入口**: 所有API请求通过网关 `/api/v1/{serviceName}/**` 格式
- **路径重写**: 网关自动去除 `/api/v1/{serviceName}` 前缀转发给微服务
- **版本管理**: API版本统一在网关层管理
- **服务解耦**: 微服务内部不感知API版本前缀

## 📋 API示例

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