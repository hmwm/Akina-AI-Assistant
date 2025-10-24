package com.akina.knowledge.test;

import com.akina.knowledge.mcp.McpClient;
import com.akina.knowledge.mcp.impl.AlipayMcpClient;
import com.akina.knowledge.mcp.impl.BaiduMcpClient;
import com.akina.knowledge.mcp.impl.GithubMcpClient;
import com.akina.knowledge.service.McpIntegrationService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * MCP 客户端测试类
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class McpClientTest {
    
    @Autowired
    private McpClient mcpClient;
    
    @Autowired
    private McpIntegrationService mcpIntegrationService;
    
    @Test
    public void testMcpClient() {
        String requestPayload = "{\"action\":\"test\",\"data\":\"hello world\"}";
        
        log.info("Testing MCP client with payload: {}", requestPayload);
        String response = mcpClient.callMcp(requestPayload);
        
        log.info("MCP client response: {}", response);
    }
    
    @Test
    public void testAlipayMcpClient() {
        AlipayMcpClient alipayClient = new AlipayMcpClient(
            "test-app-id", 
            "test-app-key", 
            "test-pub-key"
        );
        
        String requestPayload = "{\"method\":\"alipay.test.method\",\"params\":{}}";
        String response = alipayClient.callMcp(requestPayload);
        
        log.info("Alipay MCP client response: {}", response);
    }
    
    @Test
    public void testBaiduMcpClient() {
        BaiduMcpClient baiduClient = new BaiduMcpClient();
        
        String requestPayload = "{\"model\":\"ernie-bot\",\"messages\":[{\"role\":\"user\",\"content\":\"你好\"}]}";
        String response = baiduClient.callMcp(requestPayload);
        
        log.info("Baidu MCP client response: {}", response);
    }
    
    @Test
    public void testGithubMcpClient() {
        GithubMcpClient githubClient = new GithubMcpClient();
        
        String requestPayload = "{\"query\":\"query { viewer { login } }\"}";
        String response = githubClient.callMcp(requestPayload);
        
        log.info("Github MCP client response: {}", response);
    }
    
    @Test
    public void testMcpIntegrationService() {
        String userMessage = "请帮我处理一个支付请求";
        
        log.info("Testing MCP integration service with message: {}", userMessage);
        String response = mcpIntegrationService.processWithMcp(userMessage);
        
        log.info("MCP integration service response: {}", response);
    }
    
    @Test
    public void testMcpConnection() {
        String testResult = mcpIntegrationService.testMcpConnection();
        log.info("MCP connection test result: {}", testResult);
    }
}