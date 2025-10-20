package com.akina.knowledge.config;

import com.akina.knowledge.mcp.McpClient;
import com.akina.knowledge.mcp.impl.AlipayMcpClient;
import com.akina.knowledge.mcp.impl.BaiduMcpClient;
import com.akina.knowledge.mcp.impl.GithubMcpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MCP 客户端配置类
 * 负责配置和创建 MCP 客户端 Bean
 */
@Slf4j
@Configuration
public class McpClientConfig {
    
    /**
     * MCP 客户端类型配置
     * 可选值: alipay, baidu, github
     */
    @Value("${mcp.client.type:alipay}")
    private String mcpClientType;
    
    /**
     * 支付宝应用 ID
     */
    @Value("${mcp.alipay.app-id:}")
    private String alipayAppId;
    
    /**
     * 支付宝应用密钥
     */
    @Value("${mcp.alipay.app-key:}")
    private String alipayAppKey;
    
    /**
     * 支付宝公钥
     */
    @Value("${mcp.alipay.pub-key:}")
    private String alipayPubKey;
    
    /**
     * 创建 MCP 客户端 Bean
     * 默认使用 AlipayMcpClient，可通过配置切换
     * 
     * @return MCP 客户端实例
     */
    @Bean
    public McpClient mcpClient() {
        log.info("Creating MCP client with type: {}", mcpClientType);
        
        switch (mcpClientType.toLowerCase()) {
            case "alipay":
                return createAlipayMcpClient();
            case "baidu":
                return createBaiduMcpClient();
            case "github":
                return createGithubMcpClient();
            default:
                log.warn("Unknown MCP client type: {}, using default AlipayMcpClient", mcpClientType);
                return createAlipayMcpClient();
        }
    }
    
    /**
     * 创建支付宝 MCP 客户端
     * 
     * @return AlipayMcpClient 实例
     */
    private AlipayMcpClient createAlipayMcpClient() {
        log.info("Creating AlipayMcpClient with appId: {}", alipayAppId);
        
        // TODO: 添加配置验证
        // if (StringUtils.isEmpty(alipayAppId) || StringUtils.isEmpty(alipayAppKey) || StringUtils.isEmpty(alipayPubKey)) {
        //     throw new IllegalStateException("Alipay MCP client configuration is incomplete");
        // }
        
        return new AlipayMcpClient(alipayAppId, alipayAppKey, alipayPubKey);
    }
    
    /**
     * 创建百度 MCP 客户端
     * 
     * @return BaiduMcpClient 实例
     */
    private BaiduMcpClient createBaiduMcpClient() {
        log.info("Creating BaiduMcpClient");
        
        // TODO: 添加百度 API 配置
        // @Value("${mcp.baidu.api-key:}")
        // private String baiduApiKey;
        // @Value("${mcp.baidu.secret-key:}")
        // private String baiduSecretKey;
        
        return new BaiduMcpClient();
    }
    
    /**
     * 创建 GitHub MCP 客户端
     * 
     * @return GithubMcpClient 实例
     */
    private GithubMcpClient createGithubMcpClient() {
        log.info("Creating GithubMcpClient");
        
        // TODO: 添加 GitHub API 配置
        // @Value("${mcp.github.token:}")
        // private String githubToken;
        // @Value("${mcp.github.api-url:https://api.github.com}")
        // private String githubApiUrl;
        
        return new GithubMcpClient();
    }
    
    /**
     * 如何切换到其他 MCP 客户端：
     * 
     * 1. 通过配置文件切换 (推荐):
     *    在 application.yml 或 application.properties 中添加:
     *    mcp:
     *      client:
     *        type: baidu  # 或 github
     * 
     * 2. 通过环境变量切换:
     *    export MCP_CLIENT_TYPE=baidu
     * 
     * 3. 通过 JVM 参数切换:
     *    java -Dmcp.client.type=github -jar app.jar
     * 
     * 4. 通过 Spring Profile 切换:
     *    spring:
     *      profiles:
     *        active: baidu-mcp
     * 
     * 5. 通过条件注解切换 (高级用法):
     *    @Bean
     *    @ConditionalOnProperty(name = "mcp.client.type", havingValue = "baidu")
     *    public McpClient baiduMcpClient() {
     *        return new BaiduMcpClient();
     *    }
     * 
     *    @Bean
     *    @ConditionalOnProperty(name = "mcp.client.type", havingValue = "github")
     *    public McpClient githubMcpClient() {
     *        return new GithubMcpClient();
     *    }
     */
}