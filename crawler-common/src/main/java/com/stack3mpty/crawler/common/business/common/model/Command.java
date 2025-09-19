package com.stack3mpty.crawler.common.business.common.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Command implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Command.class);
    private final Runnable runnable;

    public Command(Runnable runnable) {
        this.runnable = runnable;
    }

    public void run() {
        try {
            this.runnable.run();
        } catch (Throwable e) {
            log.error("{}.run.error", this.runnable.getClass().getName(), e);
        }

    }
}