package com.stack3mpty.crawler.common.business.common.config;

public class TaskConfig {

    public static long getTaskExpireTime(int taskType) {
        return 3600000L;
    }

    public static long getQueueExpireTime(int taskType) {
        return 3600000L;
    }

    public static long getJobExpireTime() {
        return 300000L;
    }
}
