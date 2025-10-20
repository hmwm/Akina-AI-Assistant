# Akina AI Assistant API 文档

## 📋 概述

Akina AI Assistant 是一个基于 Spring AI 框架构建的智能助手项目，通过 MCP (Model Context Protocol) 提供工具集成功能。本项目不提供传统的 REST API 接口，而是通过以下方式提供服务：

1. **MCP 工具集成**: 通过 MCP 协议提供各种工具功能
2. **Spring AI ChatClient**: 提供统一的对话接口
3. **向量数据库服务**: 提供知识库管理功能
4. **多模型支持**: 集成 OpenAI 和 Ollama 模型

## 🏗️ 架构说明

### 服务端口
- **主服务端口**: 8088
- **数据库端口**: 5432 (PostgreSQL)
- **Redis端口**: 16379

### 核心组件
- **ChatClient**: 统一的对话客户端
- **MCP工具提供者**: 提供文件系统和计算机信息工具
- **向量存储**: 支持 PgVector 和 SimpleVectorStore
- **多模型支持**: OpenAI 和 Ollama 模型

## 🔧 配置接口

### 1. MCP 服务器配置

**配置文件**: `src/main/resources/config/mcp-servers-config.json`

```json
{
  "mcpServers": {
    "filesystem": {
      "command": "npx",
      "args": [
        "-y",
        "--verbose",
        "@modelcontextprotocol/server-filesystem",
        "/path/to/directory",
        "/path/to/directory"
      ]
    },
    "mcp-server-computer": {
      "command": "java",
      "args": [
        "-Dspring.ai.mcp.server.stdio=true",
        "-jar",
        "path/to/mcp-server-computer-1.0.0.jar"
      ]
    }
  }
}
```

### 2. 应用配置

**配置文件**: `src/main/resources/application-dev.yml`

```yaml
server:
  port: 8088

spring:
  datasource:
    driver-class-name: org.postgresql.Driver
    username: postgres
    password: postgres
    url: jdbc:postgresql://localhost:5432/springai
  ai:
    mcp:
      client:
        stdio:
          servers-configuration: classpath:/config/mcp-servers-config.json
    openai:
      base-url: https://api.deepseek.com
      api-key: your-api-key
      chat:
        options:
          model: deepseek-chat
    ollama:
      base-url: http://your-ollama-server:11434
      embedding:
        model: nomic-embed-text
```

## 🤖 AI 模型接口

### 1. OpenAI 模型接口

#### 基础对话
```java
@Autowired
private OpenAiChatModel openAiChatModel;

// 同步调用
ChatResponse response = openAiChatModel.call(new Prompt(
    "你好，你是什么AI",
    OpenAiChatOptions.builder()
        .model("deepseek-chat")
        .build()
));

// 流式调用
Flux<ChatResponse> stream = openAiChatModel.stream(new Prompt(
    "你好，你是什么AI",
    OpenAiChatOptions.builder()
        .model("gpt-4o")
        .build()
));
```

#### 图像理解
```java
UserMessage userMessage = new UserMessage("请描述这张图片的主要内容",
    new Media(MimeType.valueOf(MimeTypeUtils.IMAGE_PNG_VALUE),
        imageResource));

ChatResponse response = openAiChatModel.call(new Prompt(userMessage));
```

### 2. Ollama 模型接口

#### 基础对话
```java
@Autowired
private OllamaChatModel ollamaChatModel;

// 同步调用
ChatResponse response = ollamaChatModel.call(new Prompt(
    "你好，你是什么AI",
    OllamaOptions.builder()
        .model("deepseek-r1:1.5b")
        .build()
));

// 流式调用
Flux<ChatResponse> stream = ollamaChatModel.stream(new Prompt(
    "1+1",
    OllamaOptions.builder()
        .model("deepseek-r1:1.5b")
        .build()
));
```

#### 图像理解
```java
UserMessage userMessage = new UserMessage("请描述图片的主要内容",
    new Media(MimeType.valueOf(MimeTypeUtils.IMAGE_PNG_VALUE),
        imageResource));

ChatResponse response = ollamaChatModel.call(new Prompt(
    userMessage,
    OllamaOptions.builder()
        .model("deepseek-r1:1.5b")
        .build()
));
```

## 🗄️ 向量数据库接口

### 1. PgVector 存储

#### 文档上传和向量化
```java
@Autowired
@Qualifier("openAiPgVectorStore")
private PgVectorStore pgVectorStore;

@Autowired
private TokenTextSplitter tokenTextSplitter;

public void uploadDocument(String filePath, String knowledgeTag) {
    // 读取文档
    TikaDocumentReader reader = new TikaDocumentReader(filePath);
    List<Document> documents = reader.get();
    
    // 文档分割
    List<Document> documentSplitterList = tokenTextSplitter.apply(documents);
    
    // 添加元数据
    documentSplitterList.forEach(doc -> 
        doc.getMetadata().put("knowledge", knowledgeTag));
    
    // 存储到向量数据库
    pgVectorStore.accept(documentSplitterList);
}
```

#### 相似性搜索
```java
public List<Document> searchSimilarDocuments(String query, String knowledgeTag) {
    SearchRequest request = SearchRequest.builder()
        .query(query)
        .topK(5)
        .filterExpression("knowledge == '" + knowledgeTag + "'")
        .build();
    
    return pgVectorStore.similaritySearch(request);
}
```

### 2. SimpleVectorStore 存储

```java
@Autowired
@Qualifier("openAiSimpleVectorStore")
private SimpleVectorStore vectorStore;

// 存储文档
vectorStore.accept(documents);

// 搜索文档
List<Document> results = vectorStore.similaritySearch(
    SearchRequest.builder()
        .query("搜索内容")
        .topK(5)
        .build()
);
```

## 🔧 MCP 工具接口

### 1. 文件系统工具

通过 MCP 协议提供文件操作功能：

- **读取文件**: 读取指定路径的文件内容
- **写入文件**: 向指定路径写入内容
- **列出目录**: 列出指定目录下的文件和文件夹
- **创建目录**: 创建新的目录结构

### 2. 计算机信息工具

通过 MCP 协议提供系统信息查询：

- **系统配置**: 获取操作系统、硬件配置信息
- **用户信息**: 获取当前用户和目录信息
- **Java环境**: 获取 Java 运行时环境信息

## 💬 统一对话接口

### ChatClient 接口

```java
@Autowired
private ChatClient chatClient;

// 基础对话
public String chat(String message) {
    return chatClient.prompt(message).call().content();
}

// 带系统提示的对话
public String chatWithSystemPrompt(String message, String systemPrompt) {
    return chatClient.prompt(message)
        .system(systemPrompt)
        .call()
        .content();
}

// 带工具的对话
public String chatWithTools(String message) {
    return chatClient.prompt(message)
        .defaultTools(syncMcpToolCallbackProvider)
        .call()
        .content();
}
```

## 📚 知识库管理接口

### 1. RAG (检索增强生成) 对话

```java
public String ragChat(String question, String knowledgeTag) {
    // 构建系统提示
    String SYSTEM_PROMPT = """
        Use the information from the DOCUMENTS section to provide accurate answers 
        but act as if you knew this information innately.
        If unsure, simply state that you don't know.
        Another thing you need to note is that your reply must be in Chinese!
        DOCUMENTS:
            {documents}
        """;
    
    // 搜索相关文档
    SearchRequest request = SearchRequest.builder()
        .query(question)
        .topK(5)
        .filterExpression("knowledge == '" + knowledgeTag + "'")
        .build();
    
    List<Document> documents = pgVectorStore.similaritySearch(request);
    String context = documents.stream()
        .map(Document::getText)
        .collect(Collectors.joining());
    
    // 构建消息
    List<Message> messages = new ArrayList<>();
    messages.add(new UserMessage(question));
    messages.add(new SystemPromptTemplate(SYSTEM_PROMPT)
        .createMessage(Map.of("documents", context)));
    
    // 调用模型
    ChatResponse response = chatModel.call(new Prompt(messages));
    return response.getResult().getOutput().getContent();
}
```

### 2. 文档处理接口

```java
// 支持多种文档格式
TikaDocumentReader reader = new TikaDocumentReader(filePath);
List<Document> documents = reader.get();

// 文档分割
List<Document> splitDocuments = tokenTextSplitter.apply(documents);

// 添加元数据
splitDocuments.forEach(doc -> {
    doc.getMetadata().put("knowledge", "知识库名称");
    doc.getMetadata().put("source", "文档来源");
    doc.getMetadata().put("timestamp", System.currentTimeMillis());
});
```

## 🔄 流式响应接口

### 1. OpenAI 流式响应

```java
public void streamChat(String message) {
    Flux<ChatResponse> stream = openAiChatModel.stream(new Prompt(
        message,
        OpenAiChatOptions.builder()
            .model("gpt-4o")
            .build()
    ));
    
    stream.subscribe(
        chatResponse -> {
            AssistantMessage output = chatResponse.getResult().getOutput();
            System.out.println("流式响应: " + output.getContent());
        },
        Throwable::printStackTrace,
        () -> System.out.println("流式响应完成")
    );
}
```

### 2. Ollama 流式响应

```java
public void streamChatOllama(String message) {
    Flux<ChatResponse> stream = ollamaChatModel.stream(new Prompt(
        message,
        OllamaOptions.builder()
            .model("deepseek-r1:1.5b")
            .build()
    ));
    
    stream.subscribe(
        chatResponse -> {
            AssistantMessage output = chatResponse.getResult().getOutput();
            System.out.println("流式响应: " + output.getContent());
        },
        Throwable::printStackTrace,
        () -> System.out.println("流式响应完成")
    );
}
```

## 🛠️ 工具集成接口

### 1. MCP 工具提供者

```java
@Bean("syncMcpToolCallbackProvider")
public SyncMcpToolCallbackProvider syncMcpToolCallbackProvider(
    List<McpSyncClient> mcpClients) {
    
    // 去重处理
    Map<String, Integer> nameToIndexMap = new HashMap<>();
    Set<Integer> duplicateIndices = new HashSet<>();
    
    for (int i = 0; i < mcpClients.size(); i++) {
        String name = mcpClients.get(i).getServerInfo().name();
        if (nameToIndexMap.containsKey(name)) {
            duplicateIndices.add(i);
        } else {
            nameToIndexMap.put(name, i);
        }
    }
    
    // 删除重复元素
    List<Integer> sortedIndices = new ArrayList<>(duplicateIndices);
    sortedIndices.sort(Collections.reverseOrder());
    for (Integer idx : sortedIndices) {
        mcpClients.remove(idx);
    }
    
    return new SyncMcpToolCallbackProvider(mcpClients);
}
```

### 2. 聊天记忆管理

```java
@Bean
public ChatMemory chatMemory() {
    return new InMemoryChatMemory();
}

@Bean
public ChatClient chatClient(OpenAiChatModel openAiChatModel,
                           ToolCallbackProvider syncMcpToolCallbackProvider,
                           ChatMemory chatMemory) {
    return ChatClient.builder(openAiChatModel)
        .defaultTools(syncMcpToolCallbackProvider)
        .defaultAdvisors(new PromptChatMemoryAdvisor(chatMemory))
        .build();
}
```

## 📊 数据模型

### 1. 文档模型

```java
public class Document {
    private String id;
    private String content;
    private Map<String, Object> metadata;
    private float[] embedding;
}
```

### 2. 搜索请求模型

```java
public class SearchRequest {
    private String query;
    private int topK;
    private String filterExpression;
    private double similarityThreshold;
}
```

### 3. 聊天响应模型

```java
public class ChatResponse {
    private ChatResult result;
    private Map<String, Object> metadata;
    private Usage usage;
}
```

## 🔍 使用示例

### 1. 基础对话示例

```java
@RestController
public class ChatController {
    
    @Autowired
    private ChatClient chatClient;
    
    @PostMapping("/chat")
    public String chat(@RequestBody String message) {
        return chatClient.prompt(message).call().content();
    }
}
```

### 2. 知识库问答示例

```java
@Service
public class KnowledgeService {
    
    @Autowired
    private PgVectorStore vectorStore;
    
    @Autowired
    private ChatClient chatClient;
    
    public String askQuestion(String question, String knowledgeBase) {
        // 搜索相关文档
        SearchRequest request = SearchRequest.builder()
            .query(question)
            .topK(5)
            .filterExpression("knowledge == '" + knowledgeBase + "'")
            .build();
        
        List<Document> documents = vectorStore.similaritySearch(request);
        String context = documents.stream()
            .map(Document::getText)
            .collect(Collectors.joining());
        
        // 构建提示
        String prompt = String.format(
            "基于以下文档回答问题：\n%s\n\n问题：%s", 
            context, question
        );
        
        return chatClient.prompt(prompt).call().content();
    }
}
```

### 3. 文档上传示例

```java
@Service
public class DocumentService {
    
    @Autowired
    private PgVectorStore vectorStore;
    
    @Autowired
    private TokenTextSplitter tokenTextSplitter;
    
    public void uploadDocument(String filePath, String knowledgeBase) {
        try {
            // 读取文档
            TikaDocumentReader reader = new TikaDocumentReader(filePath);
            List<Document> documents = reader.get();
            
            // 分割文档
            List<Document> splitDocuments = tokenTextSplitter.apply(documents);
            
            // 添加元数据
            splitDocuments.forEach(doc -> 
                doc.getMetadata().put("knowledge", knowledgeBase));
            
            // 存储到向量数据库
            vectorStore.accept(splitDocuments);
            
        } catch (Exception e) {
            log.error("文档上传失败", e);
            throw new RuntimeException("文档上传失败", e);
        }
    }
}
```

## 🚀 部署和运行

### 1. 环境要求

- JDK 17+
- Maven 3.x
- PostgreSQL 12+ (支持 PgVector 扩展)
- Redis 6.2+

### 2. 启动命令

```bash
# 构建项目
mvn clean install

# 运行应用
cd ai-mcp-knowledge-app
mvn spring-boot:run

# 或运行 JAR 包
java -jar target/ai-mcp-knowledge-app-1.0.0.jar
```

### 3. Docker 部署

```bash
# 启动依赖服务
cd docs/dev-ops
docker-compose up -d

# 运行应用
docker run -p 8088:8088 akina-ai-assistant:latest
```

## 📝 注意事项

1. **API 密钥安全**: 请妥善保管 OpenAI API 密钥，不要提交到版本控制系统
2. **数据库配置**: 确保 PostgreSQL 已安装 PgVector 扩展
3. **MCP 工具**: 确保 MCP 服务器配置正确，工具路径有效
4. **内存管理**: 向量数据库操作可能消耗大量内存，请合理配置 JVM 参数
5. **错误处理**: 所有接口都包含完善的错误处理机制

## 🔗 相关链接

- [Spring AI 官方文档](https://spring.io/projects/spring-ai)
- [Model Context Protocol 规范](https://modelcontextprotocol.io/)
- [PgVector 扩展文档](https://github.com/pgvector/pgvector)
- [Ollama 官方文档](https://ollama.ai/)

---

*本文档基于 Akina AI Assistant v1.0.0 版本编写，如有疑问请联系开发团队。*