package com.akina.knowledge.controller;

import com.akina.knowledge.mcp.McpClient;
import com.akina.knowledge.service.McpIntegrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * MCP 控制器
 * 提供 MCP 客户端相关的 REST API 接口
 */
@Slf4j
@RestController
@RequestMapping("/api/mcp")
public class McpController {
    
    @Autowired
    private McpClient mcpClient;
    
    @Autowired
    private McpIntegrationService mcpIntegrationService;
    
    /**
     * 直接调用 MCP 客户端
     * 
     * @param requestPayload 请求载荷
     * @return MCP 响应
     */
    @PostMapping("/call")
    public String callMcp(@RequestBody String requestPayload) {
        log.info("Received MCP call request: {}", requestPayload);
        
        try {
            String response = mcpClient.callMcp(requestPayload);
            log.info("MCP call successful: {}", response);
            return response;
        } catch (Exception e) {
            log.error("MCP call failed", e);
            return "{\"error\":\"MCP call failed\",\"message\":\"" + e.getMessage() + "\"}";
        }
    }
    
    /**
     * 通过 MCP 集成服务处理用户消息
     * 
     * @param message 用户消息
     * @return AI 助手响应
     */
    @PostMapping("/chat")
    public String chatWithMcp(@RequestParam String message) {
        log.info("Received chat request with MCP: {}", message);
        
        try {
            String response = mcpIntegrationService.processWithMcp(message);
            log.info("Chat with MCP successful");
            return response;
        } catch (Exception e) {
            log.error("Chat with MCP failed", e);
            return "抱歉，处理您的请求时出现了错误：" + e.getMessage();
        }
    }
    
    /**
     * 测试 MCP 客户端连接
     * 
     * @return 测试结果
     */
    @GetMapping("/test")
    public String testConnection() {
        log.info("Testing MCP client connection");
        
        try {
            String testResult = mcpIntegrationService.testMcpConnection();
            log.info("MCP connection test completed");
            return testResult;
        } catch (Exception e) {
            log.error("MCP connection test failed", e);
            return "MCP 客户端连接测试失败：" + e.getMessage();
        }
    }
    
    /**
     * 获取 MCP 客户端信息
     * 
     * @return 客户端信息
     */
    @GetMapping("/info")
    public String getMcpInfo() {
        return "{\"clientType\":\"" + mcpClient.getClass().getSimpleName() + 
               "\",\"status\":\"active\",\"timestamp\":" + System.currentTimeMillis() + "}";
    }
}