package com.stack3mpty.crawler.server.web.controller;

import com.stack3mpty.crawler.common.business.common.model.Task;
import com.stack3mpty.crawler.server.business.dispatcher.DispatcherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TaskController {

    @Autowired
    private DispatcherService dispatcherService;

    @GetMapping("/task")
    public Task getTask(
            @RequestParam(required = false) String taskType
    ) {
        String clientCode = "";
        Task task = dispatcherService.getTask(taskType, clientCode);
        return task;
    }
}
