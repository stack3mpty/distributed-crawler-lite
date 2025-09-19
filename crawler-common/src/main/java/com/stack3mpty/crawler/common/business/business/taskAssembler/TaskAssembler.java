package com.stack3mpty.crawler.common.business.business.taskAssembler;

import com.stack3mpty.crawler.common.business.common.model.Task;

public interface TaskAssembler {
    boolean isAvailable(int taskType, String clientCode);

    Task assemble(Task task, String clientCode);
}
