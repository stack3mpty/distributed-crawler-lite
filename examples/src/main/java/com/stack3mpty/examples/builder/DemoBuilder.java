package com.stack3mpty.examples.builder;

import com.stack3mpty.crawler.common.business.business.builder.AbstractTaskBuilder;
import com.stack3mpty.crawler.common.business.common.model.TaskDetail;
import com.stack3mpty.crawler.common.business.common.model.TaskType;
import com.stack3mpty.examples.taskType.BuTaskType;

import java.util.List;
import java.util.Map;

@TaskType(BuTaskType.DEMO_TASK_TYPE)
public class DemoBuilder extends AbstractTaskBuilder {
    @Override
    protected List<? extends TaskDetail> build(Map<String, String> params) {
        DemoTaskDetail detail = new DemoTaskDetail();
        detail.setUrl(params.get("url"));
        System.out.println("-> demo builder");
        return List.of(detail);
    }
}
