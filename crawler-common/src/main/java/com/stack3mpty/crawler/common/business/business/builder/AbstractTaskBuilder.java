package com.stack3mpty.crawler.common.business.business.builder;

import com.stack3mpty.crawler.common.business.common.model.Task;
import com.stack3mpty.crawler.common.business.common.model.TaskDetail;
import lombok.extern.slf4j.Slf4j;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class AbstractTaskBuilder implements TaskBuilder<Object> {

    protected abstract List<? extends TaskDetail> build(Map<String, String> params);

    @Override
    public List<Task> build(Object input) {
        if (!(input instanceof Map)) {
            throw new IllegalArgumentException("Input must be a Map");
        }

        List<Task> tasks = new ArrayList<>();
        List<TaskDetail> details = new ArrayList<>();
        details = (List<TaskDetail>) build((Map<String, String>) input);

        if (details == null) {
            return null;
        }
        for (TaskDetail detail : details) {
            Task task = new Task();
            task.setTaskDetail(detail);
            tasks.add(task);
        }
        return tasks;
    }

}
