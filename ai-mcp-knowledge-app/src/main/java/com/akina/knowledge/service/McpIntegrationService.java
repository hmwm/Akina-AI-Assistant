package com.akina.knowledge.service;

import com.akina.knowledge.mcp.McpClient;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * MCP 集成服务
 * 演示如何将 MCP 客户端集成到现有的 AI 助手功能中
 */
@Slf4j
@Service
public class McpIntegrationService {
    
    @Autowired
    private McpClient mcpClient;
    
    @Autowired
    private ChatClient chatClient;
    
    /**
     * 通过 MCP 客户端处理用户请求
     * 
     * @param userMessage 用户消息
     * @return AI 助手的响应
     */
    public String processWithMcp(String userMessage) {
        log.info("Processing user message with MCP: {}", userMessage);
        
        try {
            // 1. 构建 MCP 请求载荷
            String mcpRequest = buildMcpRequest(userMessage);
            
            // 2. 调用 MCP 服务
            String mcpResponse = mcpClient.callMcp(mcpRequest);
            
            // 3. 解析 MCP 响应
            JSONObject mcpResult = JSON.parseObject(mcpResponse);
            
            // 4. 将 MCP 结果传递给 AI 助手
            String enhancedPrompt = buildEnhancedPrompt(userMessage, mcpResult);
            
            // 5. 获取 AI 助手的最终响应
            String aiResponse = chatClient.prompt(enhancedPrompt).call().content();
            
            log.info("MCP integration completed successfully");
            return aiResponse;
            
        } catch (Exception e) {
            log.error("MCP integration failed", e);
            
            // 降级处理：直接使用 AI 助手
            return chatClient.prompt(userMessage).call().content();
        }
    }
    
    /**
     * 构建 MCP 请求载荷
     * 
     * @param userMessage 用户消息
     * @return MCP 请求 JSON 字符串
     */
    private String buildMcpRequest(String userMessage) {
        JSONObject request = new JSONObject();
        request.put("action", "process_message");
        request.put("message", userMessage);
        request.put("timestamp", System.currentTimeMillis());
        request.put("source", "akina-ai-assistant");
        
        return request.toJSONString();
    }
    
    /**
     * 构建增强的提示词
     * 
     * @param userMessage 原始用户消息
     * @param mcpResult MCP 处理结果
     * @return 增强的提示词
     */
    private String buildEnhancedPrompt(String userMessage, JSONObject mcpResult) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("用户消息: ").append(userMessage).append("\n\n");
        prompt.append("MCP 处理结果: ").append(mcpResult.toJSONString()).append("\n\n");
        prompt.append("请基于以上信息为用户提供帮助。");
        
        return prompt.toString();
    }
    
    /**
     * 测试 MCP 客户端连接
     * 
     * @return 测试结果
     */
    public String testMcpConnection() {
        log.info("Testing MCP client connection");
        
        try {
            String testPayload = "{\"action\":\"health_check\",\"timestamp\":" + System.currentTimeMillis() + "}";
            String response = mcpClient.callMcp(testPayload);
            
            log.info("MCP client test successful: {}", response);
            return "MCP 客户端连接测试成功: " + response;
            
        } catch (Exception e) {
            log.error("MCP client test failed", e);
            return "MCP 客户端连接测试失败: " + e.getMessage();
        }
    }
}