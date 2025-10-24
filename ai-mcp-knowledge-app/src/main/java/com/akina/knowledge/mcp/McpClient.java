package com.akina.knowledge.mcp;

/**
 * MCP (Model Context Protocol) 客户端接口
 * 定义了调用 MCP 服务的统一方法
 */
public interface McpClient {
    
    /**
     * 调用 MCP 服务
     * 
     * @param requestPayload 请求载荷，通常为 JSON 格式的字符串
     * @return 响应结果，通常为 JSON 格式的字符串
     */
    String callMcp(String requestPayload);
}