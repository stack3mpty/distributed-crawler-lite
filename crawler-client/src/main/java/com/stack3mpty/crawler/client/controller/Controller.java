package com.stack3mpty.crawler.client.controller;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Controller implements Runnable {

    private static void init() {

    }

    private static void branchLoop() {
        // Logic for looping through branches
    }

    private static boolean trunkLoop() {
        // Logic for looping through trunk
        return true; // Return true or false based on the condition
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(2000);
                init();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
