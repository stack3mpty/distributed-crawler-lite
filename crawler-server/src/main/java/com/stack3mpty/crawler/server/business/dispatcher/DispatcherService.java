package com.stack3mpty.crawler.server.business.dispatcher;

import com.stack3mpty.crawler.common.business.common.model.Task;
import com.stack3mpty.crawler.server.dao.redis.build.TaskAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DispatcherService {

    @Autowired
    private TaskAccessor taskAccessor;

    public Task getTask(String taskType, String clientCode) {
        int type = Integer.parseInt(taskType);
        return taskAccessor.pollTask(type, clientCode);
    }
}
