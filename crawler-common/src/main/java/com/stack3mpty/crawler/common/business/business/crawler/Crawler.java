package com.stack3mpty.crawler.common.business.business.crawler;

import com.stack3mpty.crawler.common.business.common.model.Result;
import com.stack3mpty.crawler.common.business.common.model.ResultDetail;
import com.stack3mpty.crawler.common.business.common.model.Task;
import com.stack3mpty.crawler.common.business.common.model.TaskDetail;

public interface Crawler {
    ResultDetail execute(TaskDetail taskDetail) throws CrawlerException;

    default Result execute(Task task) throws  CrawlerException{
        return Result.newInstance(task, execute(task.getTaskDetail()));
    }
}
