package com.stack3mpty.crawler.common.business.common.model;

public enum TaskLevel {
    P0, P1, P2, P3, P4, P5, P6, UNDEFINED;
    public static TaskLevel getTaskLevel(String level) {
        switch (level) {
            case "P0":
                return TaskLevel.P0;
            case "P1":
                return TaskLevel.P1;
            case "P2":
                return TaskLevel.P2;
            case "P3":
                return TaskLevel.P3;
            case "P4":
                return TaskLevel.P4;
            case "P5":
                return TaskLevel.P5;
            case "P6":
                return TaskLevel.P6;
            default:
                return TaskLevel.UNDEFINED;
        }
    }
}
