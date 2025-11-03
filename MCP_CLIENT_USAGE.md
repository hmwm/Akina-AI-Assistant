# MCP 客户端使用指南

## 📋 概述

本文档介绍如何在 Akina AI Assistant 项目中使用新创建的 MCP (Model Context Protocol) 客户端。

## 🏗️ 架构说明

### 核心组件

1. **McpClient 接口**: 定义了统一的 MCP 调用方法
2. **实现类**:
   - `AlipayMcpClient`: 支付宝 MCP 客户端
   - `BaiduMcpClient`: 百度 MCP 客户端  
   - `GithubMcpClient`: GitHub MCP 客户端
3. **McpClientConfig**: Spring 配置类，负责创建和配置 MCP 客户端
4. **McpIntegrationService**: 集成服务，将 MCP 客户端与 AI 助手结合
5. **McpController**: REST API 控制器，提供 HTTP 接口

## 🚀 快速开始

### 1. 配置 MCP 客户端

在 `application-dev.yml` 中配置 MCP 客户端：

```yaml
# MCP 客户端配置
mcp:
  client:
    type: alipay  # 可选值: alipay, baidu, github
  alipay:
    app-id: your-alipay-app-id
    app-key: your-alipay-app-key
    pub-key: your-alipay-pub-key
  baidu:
    api-key: your-baidu-api-key
    secret-key: your-baidu-secret-key
  github:
    token: your-github-token
    api-url: https://api.github.com
```

### 2. 切换 MCP 客户端类型

#### 方法一：修改配置文件
```yaml
mcp:
  client:
    type: baidu  # 切换到百度客户端
```

#### 方法二：使用环境变量
```bash
export MCP_CLIENT_TYPE=github
```

#### 方法三：使用 JVM 参数
```bash
java -Dmcp.client.type=github -jar app.jar
```

### 3. 启动应用

```bash
cd ai-mcp-knowledge-app
mvn spring-boot:run
```

## 🔧 API 接口

### 1. 直接调用 MCP 客户端

**POST** `/api/mcp/call`

```bash
curl -X POST http://localhost:8088/api/mcp/call \
  -H "Content-Type: application/json" \
  -d '{"action":"test","data":"hello world"}'
```

**响应示例**:
```json
{
  "status": "success",
  "message": "Alipay MCP call completed",
  "appId": "your-app-id"
}
```

### 2. 通过 MCP 集成服务聊天

**POST** `/api/mcp/chat?message=你的消息`

```bash
curl -X POST "http://localhost:8088/api/mcp/chat?message=请帮我处理一个支付请求"
```

**响应示例**:
```
基于 MCP 处理结果，我来帮您处理支付请求...
```

### 3. 测试 MCP 连接

**GET** `/api/mcp/test`

```bash
curl http://localhost:8088/api/mcp/test
```

**响应示例**:
```
MCP 客户端连接测试成功: {"status":"success","message":"Alipay MCP call completed"}
```

### 4. 获取 MCP 客户端信息

**GET** `/api/mcp/info`

```bash
curl http://localhost:8088/api/mcp/info
```

**响应示例**:
```json
{
  "clientType": "AlipayMcpClient",
  "status": "active",
  "timestamp": 1703123456789
}
```

## 💻 代码使用示例

### 1. 在服务中注入 MCP 客户端

```java
@Service
public class MyService {
    
    @Autowired
    private McpClient mcpClient;
    
    public String processRequest(String request) {
        String mcpResponse = mcpClient.callMcp(request);
        // 处理 MCP 响应
        return processMcpResponse(mcpResponse);
    }
}
```

### 2. 使用 MCP 集成服务

```java
@Service
public class ChatService {
    
    @Autowired
    private McpIntegrationService mcpIntegrationService;
    
    public String chatWithMcp(String userMessage) {
        return mcpIntegrationService.processWithMcp(userMessage);
    }
}
```

### 3. 直接创建 MCP 客户端实例

```java
// 创建支付宝客户端
AlipayMcpClient alipayClient = new AlipayMcpClient(
    "app-id", "app-key", "pub-key"
);
String response = alipayClient.callMcp("{\"action\":\"test\"}");

// 创建百度客户端
BaiduMcpClient baiduClient = new BaiduMcpClient();
String response = baiduClient.callMcp("{\"model\":\"ernie-bot\"}");

// 创建 GitHub 客户端
GithubMcpClient githubClient = new GithubMcpClient();
String response = githubClient.callMcp("{\"query\":\"query { viewer { login } }\"}");
```

## 🧪 测试

### 运行单元测试

```bash
# 运行所有 MCP 相关测试
mvn test -Dtest=McpClientTest

# 运行特定测试方法
mvn test -Dtest=McpClientTest#testMcpClient
```

### 测试用例说明

1. **testMcpClient**: 测试 Spring 注入的 MCP 客户端
2. **testAlipayMcpClient**: 测试支付宝客户端
3. **testBaiduMcpClient**: 测试百度客户端
4. **testGithubMcpClient**: 测试 GitHub 客户端
5. **testMcpIntegrationService**: 测试 MCP 集成服务
6. **testMcpConnection**: 测试 MCP 连接

## 🔧 扩展开发

### 1. 添加新的 MCP 客户端

1. 创建新的实现类：

```java
public class NewMcpClient implements McpClient {
    @Override
    public String callMcp(String requestPayload) {
        // 实现具体的 MCP 调用逻辑
        return "{\"status\":\"success\"}";
    }
}
```

2. 在 `McpClientConfig` 中添加创建方法：

```java
private NewMcpClient createNewMcpClient() {
    return new NewMcpClient();
}
```

3. 在 `mcpClient()` 方法中添加新的 case：

```java
case "new":
    return createNewMcpClient();
```

### 2. 添加配置参数

在 `application-dev.yml` 中添加新客户端的配置：

```yaml
mcp:
  new-client:
    api-url: https://api.new-service.com
    api-key: your-api-key
```

在 `McpClientConfig` 中注入配置：

```java
@Value("${mcp.new-client.api-url:}")
private String newClientApiUrl;

@Value("${mcp.new-client.api-key:}")
private String newClientApiKey;
```

## 📝 注意事项

1. **配置安全**: 敏感信息如 API 密钥应通过环境变量或加密配置管理
2. **错误处理**: 所有 MCP 客户端都包含完善的错误处理机制
3. **日志记录**: 使用 `@Slf4j` 注解进行日志记录
4. **TODO 注释**: 代码中包含详细的 TODO 注释，指导实际实现
5. **测试覆盖**: 确保所有新功能都有对应的测试用例

## 🔗 相关文档

- [Spring AI 官方文档](https://spring.io/projects/spring-ai)
- [Model Context Protocol 规范](https://modelcontextprotocol.io/)
- [Spring Boot 配置文档](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html)

---

*本文档基于 Akina AI Assistant v1.0.0 版本编写，如有疑问请联系开发团队。*