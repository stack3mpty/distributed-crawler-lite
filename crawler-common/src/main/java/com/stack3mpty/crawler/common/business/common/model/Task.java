package com.stack3mpty.crawler.common.business.common.model;

import lombok.Data;

@Data
public class Task {
    private String taskType;
    private int type;
    private long buildTime;
    private int id;
    private int priority;
    private TaskDetail taskDetail;
}
