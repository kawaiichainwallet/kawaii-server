# JWT密钥说明

## ES256算法密钥对

本项目使用ES256 (ECDSA with P-256 and SHA-256) 算法进行JWT签名：

### 密钥文件
- `ec-private-key.pem` - EC私钥 (用于kawaii-user服务生成JWT)
- `ec-public-key.pem` - EC公钥 (用于kawaii-gateway和其他服务验证JWT)

### 服务配置

#### kawaii-user服务
需要配置私钥和公钥（用于生成和自验证JWT）：
```yaml
app:
  security:
    jwt:
      private-key: |
        -----BEGIN EC PRIVATE KEY-----
        [私钥内容]
        -----END EC PRIVATE KEY-----
      public-key: |
        -----BEGIN PUBLIC KEY-----
        [公钥内容]
        -----END PUBLIC KEY-----
```

#### kawaii-gateway服务
只需要配置公钥（用于验证JWT）：
```yaml
app:
  security:
    jwt:
      public-key: |
        -----BEGIN PUBLIC KEY-----
        [公钥内容]
        -----END PUBLIC KEY-----
```

### 安全说明

**开发环境：**
- 当前密钥对仅用于本地开发测试
- 已提交到Git仓库便于团队开发

**生产环境：**
- 必须重新生成密钥对
- 私钥绝对不能提交到Git
- 通过Kubernetes Secret或云服务密钥管理注入
- 定期轮换密钥

### 密钥生成命令

如需重新生成密钥对：
```bash
# 生成EC私钥 (P-256曲线，SEC1格式)
openssl ecparam -genkey -name prime256v1 -noout -out ec-private-key-sec1.pem

# 转换为PKCS#8格式 (Java标准格式)
openssl pkcs8 -topk8 -nocrypt -in ec-private-key-sec1.pem -out ec-private-key.pem

# 从私钥导出公钥
openssl ec -in ec-private-key.pem -pubout -out ec-public-key.pem
```

**注意：** Java的KeyFactory需要PKCS#8格式的私钥（`BEGIN PRIVATE KEY`），而不是SEC1格式（`BEGIN EC PRIVATE KEY`）。

### ES256算法优势

相比HS256 (HMAC-SHA256):
- **非对称加密**: 私钥签名，公钥验证，无需共享密钥
- **微服务友好**: Gateway和其他服务只需公钥即可验证
- **安全性高**: 一个服务被攻破不会影响整个系统

相比RS256 (RSA):
- **性能更好**: 签名和验证速度更快
- **密钥更小**: 256位EC密钥 ≈ 3072位RSA密钥的安全强度
- **带宽友好**: JWT体积更小

### 参考文档
- [RFC 7518 - JSON Web Algorithms (JWA)](https://tools.ietf.org/html/rfc7518)
- [NIST P-256曲线](https://csrc.nist.gov/projects/cryptographic-algorithm-validation-program/digital-signatures)
