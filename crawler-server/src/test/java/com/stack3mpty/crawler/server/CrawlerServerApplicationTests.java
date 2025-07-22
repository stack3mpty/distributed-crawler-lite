package com.stack3mpty.crawler.server;

import com.stack3mpty.crawler.server.business.builder.BuilderService;
import com.stack3mpty.crawler.server.web.controller.BuildController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class CrawlerServerApplicationTests {

    @Autowired
    private BuilderService builderService;

    @Autowired
    private BuildController buildController;

    private Map<String, String> input = new HashMap<>();

    @BeforeEach
    void setUp() {
        input = new HashMap<>();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void buildTest() {
        String typeName = "DEMO_TASK_TYPE";
        long buildTime = System.currentTimeMillis();
        input.put("buildTime", String.valueOf(buildTime));
        Map<String, Long> result = buildController.build(typeName, input);
        System.out.println("Build test executed successfully.");
    }

}
