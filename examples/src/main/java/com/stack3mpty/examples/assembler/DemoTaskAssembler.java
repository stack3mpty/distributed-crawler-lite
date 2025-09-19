package com.stack3mpty.examples.assembler;

import com.stack3mpty.crawler.common.business.business.taskAssembler.TaskAssembler;
import com.stack3mpty.crawler.common.business.common.model.Task;

public class DemoTaskAssembler implements TaskAssembler {
    @Override
    public boolean isAvailable(int taskType, String clientCode) {
        return true;
    }

    @Override
    public Task assemble(Task task, String clientCode) {
        return task;
    }
}
