package com.akina.knowledge.domain;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class McpServerHaloJob {

    @Resource
    private ChatClient chatClient;

    @PostConstruct
    public void runAtStartup() {
        log.info("====== 应用启动，立即执行第一次连载任务 ======");
        exec();
    }

//    @Scheduled(cron = "0 * * * * ?")
    @Scheduled(cron = "0 0 * * * *")
    public void exec() {
        // 检查当前时间是否在执行的时间范围内（8点到23点之间）
//        int currentHour = LocalDateTime.now().getHour();
//        if (currentHour >= 23 || currentHour < 8 ) {
//            log.info("当前时间 {}点 不在任务执行时间范围内，跳过执行", currentHour);
//            return;
//        }
        try {
            String userInput = """
                    你是一名负责连载现代都市网文小说的写作执行器。本次调用必须依次完成以下流程：
                    
                    =================【必须执行的动作】=================
                    1）读取文件：read_text_file("E:\\MCP_test\\novel_outline.txt")
                    2）判断文件内容：
                       - 若为空或不存在：说明是第一次写作 → 创建小说名 + 初始大纲（20节起步） + 第1节正文
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

//            String userInput = """
//                    我需要你帮我生成一篇文章，要求如下；
//                    1. 场景为互联网大厂java求职者面试
//                    2. 提问的技术栈如下；
//
//                        核心语言与平台: Java SE (8/11/17), Jakarta EE (Java EE), JVM
//                        构建工具: Maven, Gradle, Ant
//                        Web框架: Spring Boot, Spring MVC, Spring WebFlux, Jakarta EE, Micronaut, Quarkus, Play Framework, Struts (Legacy)
//                        数据库与ORM: Hibernate, MyBatis, JPA, Spring Data JDBC, HikariCP, C3P0, Flyway, Liquibase
//                        测试框架: JUnit 5, TestNG, Mockito, PowerMock, AssertJ, Selenium, Cucumber
//                        微服务与云原生: Spring Cloud, Netflix OSS (Eureka, Zuul), Consul, gRPC, Apache Thrift, Kubernetes Client, OpenFeign, Resilience4j
//                        安全框架: Spring Security, Apache Shiro, JWT, OAuth2, Keycloak, Bouncy Castle
//                        消息队列: Kafka, RabbitMQ, ActiveMQ, JMS, Apache Pulsar, Redis Pub/Sub
//                        缓存技术: Redis, Ehcache, Caffeine, Hazelcast, Memcached, Spring Cache
//                        日志框架: Log4j2, Logback, SLF4J, Tinylog
//                        监控与运维: Prometheus, Grafana, Micrometer, ELK Stack, New Relic, Jaeger, Zipkin
//                        模板引擎: Thymeleaf, FreeMarker, Velocity, JSP/JSTL
//                        REST与API工具: Swagger/OpenAPI, Spring HATEOAS, Jersey, RESTEasy, Retrofit
//                        序列化: Jackson, Gson, Protobuf, Avro
//                        CI/CD工具: Jenkins, GitLab CI, GitHub Actions, Docker, Kubernetes
//                        大数据处理: Hadoop, Spark, Flink, Cassandra, Elasticsearch
//                        版本控制: Git, SVN
//                        工具库: Apache Commons, Guava, Lombok, MapStruct, JSch, POI
//                        其他: JUnit Pioneer, Dubbo, R2DBC, WebSocket
//
//                    3. 提问的场景方案可包括但不限于；音视频场景,内容社区与UGC,AIGC,游戏与虚拟互动,电商场景,本地生活服务,共享经济,支付与金融服务,互联网医疗,健康管理,医疗供应链,企业协同与SaaS,产业互联网,大数据与AI服务,在线教育,求职招聘,智慧物流,供应链金融,智慧城市,公共服务数字化,物联网应用,Web3.0与区块链,安全与风控,广告与营销,能源与环保。               \s
//                    4. 按照故事场景，以严肃的面试官和搞笑的水货程序员聂博进行提问。
//                    5. 每次进行3轮提问，每轮可以有3-5个问题。这些问题要有技术业务场景上的衔接性，循序渐进引导提问。最后是面试官让程序员回家等通知类似的话术。
//                    6. 提问后把问题的答案详细的，写到文章最后，讲述出业务场景和技术点，让小白可以学习下来。
//
//                        根据以上内容，不要阐述其他信息，请直接提供；文章标题（需要含带技术点）、文章内容、文章标签（多个用英文逗号隔开）、文章简述（100字）
//                        将以上内容发布文章到Halo
//                    """;

            log.info("执行结果:{} {}", userInput, chatClient.prompt(userInput).call().content());
        } catch (Exception e) {
            log.error("定时任务，回调通知AI发帖失败", e);
        }
    }
}
