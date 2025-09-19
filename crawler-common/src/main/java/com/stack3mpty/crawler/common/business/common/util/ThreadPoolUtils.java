package com.stack3mpty.crawler.common.business.common.util;

import com.stack3mpty.crawler.common.business.common.model.Command;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolUtils {
    private static volatile ThreadPoolExecutor executor;
    private static Integer MIN_SIZE = 10;

    public static ThreadPoolExecutor getExecutor() {
        if (executor == null) {
            synchronized(ThreadPoolUtils.class) {
                if (executor == null) {
                    int availableProcessors = Math.max(Runtime.getRuntime().availableProcessors() * 2, MIN_SIZE);
                    executor = new ThreadPoolExecutor(availableProcessors, availableProcessors * 2, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue(512), new ThreadPoolExecutor.CallerRunsPolicy());
                }
            }
        }

        return executor;
    }

    public static void execute(Runnable runnable) {
        Command command = new Command(runnable);
        getExecutor().execute(command);
    }

    public static void setMIN_SIZE(Integer MIN_SIZE) {
        ThreadPoolUtils.MIN_SIZE = MIN_SIZE;
    }
}