package com.stack3mpty.crawler.server.web.controller;

import com.stack3mpty.crawler.common.business.common.model.TaskTypes;
import com.stack3mpty.crawler.server.business.builder.BuilderService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/build")
public class BuildController {
    private final BuilderService builderService;

    public BuildController(BuilderService builderService) {
        this.builderService = builderService;
    }

    @RequestMapping(value = "/{typeName}")
    public Map<String, Long> build(
        @PathVariable("typeName") String typeName,
        @RequestParam Map<String, String> input
    ) {
        long buildTime = Optional.ofNullable(input.get("buildTime")).map(Long::parseLong).orElse(0L);
        buildTime = buildTime <= 0 ? System.currentTimeMillis() : buildTime;
        int type = TaskTypes.parseType(typeName);
        builderService.build(type, input);
        return Map.of(
            "type", (long) type,
            "buildTime", buildTime
        );
    }
}
