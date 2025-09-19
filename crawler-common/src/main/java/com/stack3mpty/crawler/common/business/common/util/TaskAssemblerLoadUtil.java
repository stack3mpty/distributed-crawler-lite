package com.stack3mpty.crawler.common.business.common.util;

import com.stack3mpty.crawler.common.business.business.taskAssembler.TaskAssembler;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

@Slf4j
public class TaskAssemblerLoadUtil {

    private static HashMap<Integer, TaskAssembler> assemblerMap = new HashMap<>();

    public static TaskAssembler getAssembler(int type) {
        return assemblerMap.get(type);
    }

    public static void register(int type, TaskAssembler assembler) {
        assemblerMap.put(type, assembler);
    }

    public static void closeAll() {
        for (TaskAssembler assembler : assemblerMap.values()) {
            if (assembler instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) assembler).close();
                } catch (Exception e) {
                    log.error("assembler-close 异常：", e);
                }
            }
        }
    }
}
