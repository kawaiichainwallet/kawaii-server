# ⚡ KawaiiChain Wallet Server

> 高性能微服务后端，为数字钱包提供坚实的技术基础

## 📸 架构预览

![功能截图-1](./docs/images/功能截图-1.png)

## ✨ 核心特性

- 🔐 **安全可靠** - JWT认证、Token黑名单、多重加密保护
- ⚡ **高性能** - 微服务架构、Redis缓存、数据库优化
- 🔄 **易扩展** - 模块化设计、服务治理、配置中心
- 🌐 **多链支持** - ETH、BTC等主流区块链集成
- 📊 **可观测** - 完善的日志、监控和链路追踪

## 🏗️ 微服务架构

| 服务 | 端口 | 职责 |
|------|------|------|
| kawaii-gateway | 8080 | API网关、认证鉴权 |
| kawaii-user | 8081 | 用户管理、认证服务 |
| kawaii-core | 8082 | 钱包核心、链交互 |
| kawaii-payment | 8083 | 支付结算 |

## 🚀 快速开始

```bash
# 1. 启动基础设施
docker-compose up -d  # PostgreSQL + Redis + Nacos

# 2. 启动微服务
cd kawaii-gateway && mvn spring-boot:run
cd kawaii-user && mvn spring-boot:run
cd kawaii-core && mvn spring-boot:run
```

**环境要求**: JDK 21+ | Maven 3.9+ | PostgreSQL 17 | Redis 7.0

📖 详细步骤请参考：**[环境配置指南](docs/environment-setup.md)**

## 🛠 技术栈

- Spring Boot 3.x + Spring Cloud
- PostgreSQL + Redis
- MyBatis-Plus + Web3j
- Nacos 配置中心

## 📚 开发文档

- [API路由规范](docs/api-routes.md) - 网关路由和API设计
- [错误码管理](docs/error-codes.md) - 统一错误码规范
- [安全配置](docs/security-config.md) - JWT认证和权限控制
- [数据库设计](docs/database-design.md) - 数据库架构设计

## 🤝 参与贡献

我们欢迎所有形式的贡献！无论是报告Bug、提出新功能建议、改进文档还是提交代码。

## 🔗 相关项目

- [kawaii-mobile](../kawaii-mobile) - Flutter移动端应用
- [kawaii-admin](../kawaii-admin) - Next.js管理后台
- [kawaii-website](../kawaii-website) - 产品宣传网站

## 📄 许可证

BSD 3-Clause License - 查看 [LICENSE](LICENSE) 文件了解详情

## ⚠️ 免责声明

### 技术交流与教育目的

**KawaiiChain Wallet 是一个开源的技术研究和教育项目，仅供学习、研究和技术交流使用。**

### 重要声明

1. **纯技术项目**: 本项目仅作为区块链和数字钱包技术的研究与学习工具，不提供任何金融服务或投资建议。

2. **合规责任**: 使用者需自行了解并遵守所在国家和地区关于数字货币、区块链技术的相关法律法规。我们不对任何违法使用承担责任。

3. **地区限制**: 本项目可能在某些国家和地区不被允许使用。如果您所在的司法管辖区禁止或限制此类技术，请不要下载、安装或使用本软件。

4. **风险提示**:
    - 数字资产具有高风险性，价格波动剧烈
    - 私钥丢失可能导致资产永久损失
    - 区块链交易具有不可逆性
    - 智能合约可能存在未知漏洞

5. **无担保声明**: 本软件按"现状"提供，不提供任何明示或暗示的担保。开发团队不对使用本软件造成的任何损失承担责任。

6. **数据安全**: 虽然我们致力于提供安全的技术方案，但无法保证绝对的安全性。用户应当采取适当的安全措施保护自己的数字资产。

### 使用条件

使用本项目即表示您：
- 已阅读并理解上述免责声明
- 承诺仅将本项目用于合法的技术学习和研究目的
- 同意自行承担使用本项目的所有风险和责任
- 确认在您所在的司法管辖区使用本项目是合法的

---

**Built with 💖 by KawaiiChain Team**
