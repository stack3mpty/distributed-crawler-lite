package com.stack3mpty.crawler.client.controller;

import com.stack3mpty.crawler.client.dto.TaskDTO;
import com.stack3mpty.crawler.client.service.CrawlerServerService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Controller implements Runnable {

    private static void init() {
        log.info("init crawler environment...");
    }

    private static void branchLoop() {
        // Logic for looping through branches
    }

    private static boolean trunkLoop() {
        log.info("start a new trunkLoop");
        TaskDTO task = CrawlerServerService.getTask();
        if (task == null) {
            log.info("get no task, just return");
            return false;
        }

        return true; // Return true or false based on the condition
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(2000);
                init();
                break;
            } catch (Exception e) {
                log.error("Controller.init.error", e);
            }
        }
        while (true) {
            try {
                Thread.sleep(trunkLoop() ? 500 : 1000);
            } catch (Exception e) {
                log.error("Controller.trunkLoop.error", e);
            }
        }
    }
}
