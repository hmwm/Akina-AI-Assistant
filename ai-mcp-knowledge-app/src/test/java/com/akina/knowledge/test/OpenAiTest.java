package com.akina.knowledge.test;


import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.Media;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.CountDownLatch;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class OpenAiTest {

    @Value("classpath:data/kafka.png")
    private org.springframework.core.io.Resource imageResource;

    @Autowired
    private OpenAiChatModel openAiChatModel; // 这里注入的是 OpenAiChatModel 实例

    @Resource(name = "openAiSimpleVectorStore")
    private SimpleVectorStore vectorStore;

    @Resource(name = "openAiPgVectorStore")
    private PgVectorStore pgVectorStore;

    @Resource
    private TokenTextSplitter tokenTextSplitter;

    @Resource
    private OpenAiApi openAiApi;

    @Test
    public void test_call() {
        ChatResponse response = openAiChatModel.call(new Prompt(
                "你好，你是什么AI",
                OpenAiChatOptions.builder()
                        .model("deepseek-chat")
                        .build()));

        log.info("测试结果(call):{}", JSON.toJSONString(response));
    }

    @Test
    public void test_call_images() {
        // 构建请求信息
        UserMessage userMessage = new UserMessage("请描述这张图片的主要内容",
                new Media(MimeType.valueOf(MimeTypeUtils.IMAGE_PNG_VALUE),
                        imageResource));

        ChatResponse response = openAiChatModel.call(new Prompt(
                userMessage));

        log.info("测试结果(images):{}", JSON.toJSONString(response));
    }

    @Test
    public void test_stream() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        Flux<ChatResponse> stream = openAiChatModel.stream(new Prompt(
                "你好，你是什么AI",
                OpenAiChatOptions.builder().model("gpt-4o").build()
        ));

        stream.subscribe(chatResponse -> {
            AssistantMessage output = chatResponse.getResult().getOutput();
            log.info("测试结果(stream): {}", JSON.toJSONString(output));
        },
                Throwable::printStackTrace,
                () -> {
                    countDownLatch.countDown();
                    log.info("测试结果(stream): done!");
                }
        );

        countDownLatch.await();
    }

    @Test
    public void upload() {
        TikaDocumentReader reader = new TikaDocumentReader("file:///E:/data/file.txt");

        List<Document> documents = reader.get();
        List<Document> documentSplitterList = tokenTextSplitter.apply(documents);

        // tag
        documents.forEach(doc -> doc.getMetadata().put("knowledge", "知识库名称v2"));
        documentSplitterList.forEach(doc -> doc.getMetadata().put("knowledge", "知识库名称v2"));

        // 上传到向量库
        pgVectorStore.accept(documentSplitterList);

        log.info("上传完成");
    }
}

