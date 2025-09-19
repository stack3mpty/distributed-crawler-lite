package com.stack3mpty.crawler.common.business.business.taskAssembler;

import com.stack3mpty.crawler.common.business.common.model.TaskType;
import com.stack3mpty.crawler.common.business.common.util.Conf;
import com.stack3mpty.crawler.common.business.common.util.TaskAssemblerLoadUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TaskAssemblersLoader {
    
    @PostConstruct
    public void init() {
        for (String className : Conf.getClasses("com.stack3mptu.examples.taskAssembler")) {
            Class<?> clazz;
            try {
                clazz = Class.forName(className);
            } catch (Error | RuntimeException | ReflectiveOperationException e) {
                log.error("TaskAssemblersLoader 类加载失败", e);
                continue;
            }
            if (!TaskAssembler.class.isAssignableFrom(clazz)) {
                continue;
            }
            TaskType annot = clazz.getAnnotation(TaskType.class);
            if (annot == null) {
                continue;
            }
            TaskAssembler assembler;
            try {
                assembler = (TaskAssembler) clazz.newInstance();
            } catch (Error | RuntimeException | ReflectiveOperationException e) {
                log.error("", e);
                continue;
            }
            TaskAssemblerLoadUtil.register(annot.value(), assembler);
        }

        for (String className : Conf.getClassesByJar("taskAssembler")) {
            Class<?> clazz;
            try {
                clazz = Class.forName(className);
            } catch (Error | RuntimeException | ReflectiveOperationException e) {
                log.error("", e);
                continue;
            }
            if (!TaskAssembler.class.isAssignableFrom(clazz)) {
                continue;
            }
            TaskType annot = clazz.getAnnotation(TaskType.class);
            if (annot == null) {
                continue;
            }
            TaskAssembler assembler;
            try {
                assembler = (TaskAssembler) clazz.newInstance();
            } catch (Error | RuntimeException | ReflectiveOperationException e) {
                log.error("", e);
                continue;
            }

            TaskAssemblerLoadUtil.register(annot.value(), assembler);
        }
    }
    
}
