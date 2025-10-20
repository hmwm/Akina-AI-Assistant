# Akina AI Assistant

一个基于 Spring AI 框架构建的智能助手项目，集成了多种 AI 模型和 MCP (Model Context Protocol) 工具，提供知识库管理、文档处理和智能对话功能。

## 🚀 项目特性

- **多模型支持**: 集成 OpenAI 和 Ollama 模型，支持文本生成、图像理解和流式响应
- **MCP 工具集成**: 支持文件系统操作和计算机信息获取等 MCP 工具
- **向量数据库**: 支持 PgVector 和 SimpleVectorStore 两种向量存储方案
- **知识库管理**: 支持文档上传、向量化和相似性搜索
- **多模态支持**: 支持文本和图像输入处理
- **流式响应**: 支持实时流式对话体验

## 📁 项目结构

```
Akina-AI-Assistant/
├── ai-mcp-knowledge-app/          # 主应用模块
│   ├── src/main/java/com/akina/knowledge/
│   │   ├── Application.java       # 应用启动类
│   │   └── config/               # 配置类
│   │       ├── OllamaConfig.java # Ollama 配置
│   │       └── OpenaiConfig.java # OpenAI 配置
│   ├── src/main/resources/
│   │   ├── application.yml       # 主配置文件
│   │   ├── application-dev.yml   # 开发环境配置
│   │   └── config/
│   │       └── mcp-servers-config.json # MCP 服务器配置
│   └── src/test/java/            # 测试类
├── ai-mcp-knowledge-trigger/     # 触发器模块
├── docs/                        # 文档和运维配置
│   └── dev-ops/
│       ├── docker-compose-environment.yml # Docker 环境配置
│       ├── pgvector/sql/init.sql # 数据库初始化脚本
│       └── redis/redis.conf      # Redis 配置
└── pom.xml                      # Maven 父项目配置
```

## 🛠️ 技术栈

- **框架**: Spring Boot 3.4.3
- **AI 框架**: Spring AI 1.0.0-M6
- **数据库**: PostgreSQL + PgVector 扩展
- **缓存**: Redis 6.2
- **构建工具**: Maven 3.x
- **Java 版本**: JDK 17

## 📋 依赖组件

### AI 模型支持
- **OpenAI**: 支持 GPT-4o、DeepSeek 等模型
- **Ollama**: 支持本地部署的 DeepSeek-R1 等模型

### 向量数据库
- **PgVector**: 基于 PostgreSQL 的向量数据库
- **SimpleVectorStore**: 内存向量存储

### MCP 工具
- **文件系统工具**: 支持文件读写操作
- **计算机信息工具**: 获取系统配置信息

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.x
- PostgreSQL 12+ (支持 PgVector 扩展)
- Redis 6.2+
- Docker & Docker Compose (可选)

### 1. 克隆项目

```bash
git clone <repository-url>
cd Akina-AI-Assistant
```

### 2. 启动依赖服务

使用 Docker Compose 快速启动环境：

```bash
cd docs/dev-ops
docker-compose up -d
```

这将启动以下服务：
- PostgreSQL (端口: 5432)
- Redis (端口: 16379)
- Redis Admin (端口: 8081)
- PgAdmin (端口: 5050)

### 3. 配置应用

编辑 `ai-mcp-knowledge-app/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/springai
    username: postgres
    password: postgres
  ai:
    openai:
      base-url: https://api.deepseek.com  # 或你的 OpenAI API 地址
      api-key: your-api-key
      chat:
        options:
          model: deepseek-chat
    ollama:
      base-url: http://your-ollama-server:11434
      embedding:
        model: nomic-embed-text
```

### 4. 构建和运行

```bash
# 构建项目
mvn clean install

# 运行应用
cd ai-mcp-knowledge-app
mvn spring-boot:run
```

应用将在 `http://localhost:8088` 启动。

## 🔧 配置说明

### MCP 服务器配置

在 `config/mcp-servers-config.json` 中配置 MCP 服务器：

```json
{
  "mcpServers": {
    "filesystem": {
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-filesystem",
        "/path/to/your/directory"
      ]
    },
    "mcp-server-computer": {
      "command": "java",
      "args": [
        "-jar",
        "path/to/mcp-server-computer.jar"
      ]
    }
  }
}
```

### 向量数据库配置

项目支持两种向量存储方案：

1. **PgVector** (推荐生产环境)
   - 表名: `vector_store_openai` (1536 维)
   - 表名: `vector_store_ollama_deepseek` (768 维)

2. **SimpleVectorStore** (适合开发测试)
   - 基于内存存储

## 🧪 测试功能

项目提供了完整的测试用例：

### 基础功能测试
```bash
# 测试 OpenAI 模型
mvn test -Dtest=OpenAiTest

# 测试 Ollama 模型
mvn test -Dtest=OllamaTest

# 测试 MCP 工具
mvn test -Dtest=MCPTest
```

### 知识库功能测试
- 文档上传和向量化
- 相似性搜索
- RAG (检索增强生成) 对话

## 📚 使用示例

### 1. 基础对话

```java
@Autowired
private ChatClient chatClient;

public String chat(String message) {
    return chatClient.prompt(message).call().content();
}
```

### 2. 文档上传和向量化

```java
@Autowired
private PgVectorStore vectorStore;

public void uploadDocument(String filePath) {
    TikaDocumentReader reader = new TikaDocumentReader(filePath);
    List<Document> documents = reader.get();
    List<Document> splitDocuments = tokenTextSplitter.apply(documents);
    
    // 添加元数据
    splitDocuments.forEach(doc -> 
        doc.getMetadata().put("knowledge", "知识库名称"));
    
    vectorStore.accept(splitDocuments);
}
```

### 3. 知识库问答

```java
public String ragChat(String question) {
    SearchRequest request = SearchRequest.builder()
        .query(question)
        .topK(5)
        .filterExpression("knowledge == '知识库名称'")
        .build();
    
    List<Document> documents = vectorStore.similaritySearch(request);
    String context = documents.stream()
        .map(Document::getText)
        .collect(Collectors.joining());
    
    return chatClient.prompt(question)
        .system("基于以下文档回答问题: " + context)
        .call().content();
}
```

## 🔍 功能特性详解

### 多模型支持
- **OpenAI**: 支持 GPT-4o、DeepSeek 等商业模型
- **Ollama**: 支持本地部署的开源模型
- **流式响应**: 支持实时流式对话体验

### MCP 工具集成
- **文件系统工具**: 支持文件读写、目录操作
- **计算机信息工具**: 获取系统配置、硬件信息

### 知识库管理
- **文档处理**: 支持多种格式文档解析 (Tika)
- **向量化**: 支持 OpenAI 和 Ollama 嵌入模型
- **相似性搜索**: 基于向量相似度的文档检索

## 🐳 Docker 部署

项目提供了完整的 Docker 环境配置：

```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

## 📝 开发指南

### 添加新的 AI 模型

1. 在 `pom.xml` 中添加相应依赖
2. 创建配置类 (参考 `OllamaConfig.java`)
3. 添加测试用例

### 添加新的 MCP 工具

1. 在 `mcp-servers-config.json` 中配置服务器
2. 在 `OpenaiConfig.java` 中注册工具
3. 编写测试用例验证功能

## 🤝 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系方式

- 项目维护者: Akina Team
- 邮箱: [1037462262@qq.com]
- 项目链接: [https://github.com/hmwm/Akina-AI-Assistant](https://github.com/your-username/Akina-AI-Assistant)

## 🙏 致谢

- [Spring AI](https://spring.io/projects/spring-ai) - 强大的 AI 集成框架
- [PgVector](https://github.com/pgvector/pgvector) - PostgreSQL 向量扩展
- [Ollama](https://ollama.ai/) - 本地 AI 模型运行环境
- [Model Context Protocol](https://modelcontextprotocol.io/) - AI 工具集成协议