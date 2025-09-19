package com.stack3mpty.crawler.common.business.common.model;

import lombok.Data;

@Data
public class Result {

    private int taskType;
    private long buildTime;
    private int taskId;
    private TaskLevel taskLevel = TaskLevel.P0;
    private ResultDetail resultDetail;
    private Task task;

    public static Result newInstance(Task task, ResultDetail detail) {
        Result result = new Result();
        if (task != null) {
            result.setTaskType(task.getType());
            result.setBuildTime(task.getBuildTime());
            result.setTaskId(task.getId());
            result.setTaskLevel(task.getLevel());
            result.setTask(task);
        }
        result.setResultDetail(detail);
        return result;
    }
}
