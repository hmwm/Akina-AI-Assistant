package com.akina.knowledge.test;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class MCPTest {

    @Resource
    private ChatClient chatClient;

//    @Autowired
//    private ToolCallbackProvider tools;

    @Test
    public void test_tool() {
        String userInput = "有哪些工具可以使用";
        userInput = "有哪些服务可用";

        System.out.println("\n>>> Question: " + userInput);
        System.out.println("\n>>> Assistant: " + chatClient.prompt(userInput).call().content());
    }

    @Test
    public void test_workflow() {
        String userInput = """
                    你是一名负责连载现代都市网文小说的写作执行器。本次调用必须依次完成以下流程：
                    
                    =================【必须执行的动作】=================
                    1）读取文件：read_text_file("E:\\MCP_test\\novel_outline.txt")
                    2）判断文件内容：
                       - 若为空或不存在：说明是第一次写作 → 创建小说名 + 初始大纲 + 第1节正文
                       - 若有内容：从中解析【小说名】【大纲】【当前进度】，继续写下一节正文并更新大纲与进度
                    3）将更新后的完整大纲（不含正文）覆盖写回：write_file("E:\\MCP_test\\novel_outline.txt", updatedOutline)
                    4）将新一节正文通过 queryHaloBlogConfig 发布文章
                       - 标题由 AI 根据大纲和节序自行生成（不需要用户提供）
                       - 文章内容为“本次生成的正文”
                       - 其余参数设置由 AI 自行决定（例如标签、分类）
                    
                    =================【写作要求】=================
                    - 现代都市网文风格
                    - 每节约 200 字
                    - 必须承接现有剧情（参考文件大纲，若无则开新故事）
                    - 结尾必须留钩子（悬念/未决/拐点）
                    - 正文中禁止出现“读者”“本节”“作者”等元叙事
                    - 不得在对话中向用户解释，不得输出正文在返回内容中，只能通过发布服务提交
                    
                    =================【大纲文件格式规范】=================
                    文件必须始终保持如下结构（无正文）：
                    【小说名】
                    xxxx
                    
                    【大纲】
                    - 第1节：xxx
                    - 第2节：xxx
                    ……
                    
                    【当前进度】
                    N
                    
                    =================【输出限制】=================
                    你不得输出任何正文、解释或自然语言回答。
                    你必须只输出 MCP 服务调用 JSON (read_text_file / write_file / queryHaloBlogConfig)，并严格按流程顺序执行。
                    
                    
                    """;


        System.out.println("\n>>> Question: " + userInput);
        System.out.println("\n>>> Assistant: " + chatClient.prompt(userInput).call().content());
    }

}
