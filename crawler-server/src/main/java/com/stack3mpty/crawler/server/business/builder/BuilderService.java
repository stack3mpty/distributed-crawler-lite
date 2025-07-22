package com.stack3mpty.crawler.server.business.builder;

import com.stack3mpty.crawler.common.business.business.builder.TaskBuilder;
import com.stack3mpty.crawler.common.business.common.model.Task;
import com.stack3mpty.crawler.common.business.common.model.TaskType;
import com.stack3mpty.crawler.common.business.common.util.Conf;
import com.stack3mpty.crawler.server.dao.redis.build.TaskAccessor;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 将任务构建到redis
 */
@Slf4j
@Service
public class BuilderService {

    @Getter
    private final Map<Integer, TaskBuilder<?>> builderMap = new ConcurrentHashMap<>();
    private final TaskAccessor taskAccessor;

    public BuilderService(TaskAccessor taskAccessor) {
        this.taskAccessor = taskAccessor;
    }

    @PostConstruct
    public void init() {
        for (String className : Conf.getAllBuilderClassNames()) {
            try {
                Class<?> clazz = Class.forName(className);
                if (TaskBuilder.class.isAssignableFrom(clazz)) {
                    TaskType taskTypeAnnotation = clazz.getAnnotation(TaskType.class);
                    if (taskTypeAnnotation != null) {
                        TaskBuilder<?> taskBuilder = (TaskBuilder<?>) clazz.getDeclaredConstructor().newInstance();
                        int type = taskTypeAnnotation.value();
                        builderMap.put(type, taskBuilder);
                        log.info("Registered task builder: {} with type: {}", className, type);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to register task builder: {}", className, e);
            }
        }
    }

    public void build(int type ,Map<String, String> input) {
        long buildTime = Long.parseLong(input.get("buildTime"));
        int priority = Optional.ofNullable(input.get("priority")).map(Integer::parseInt).orElse(0);
        int limit = Optional.ofNullable(input.get("limit")).map(Integer::parseInt).orElse(0);
        // some other params
        buildWithRecord(type, buildTime, priority, limit, input);
    }

    private void buildWithRecord(int type, long buildTime, int priority, int limit, Map<String, String> input) {
        // build
        try {
            int taskNum = doBuild(type, buildTime, priority, limit, input);
            System.out.println("build task type: " + type + ", task num: " + taskNum + ", build time: " + buildTime + ", priority: " + priority + ", limit: " + limit);
        } catch (Throwable e) {
            log.error("build error: {}", e.getMessage());
        }
    }

    private int doBuild(int type, long buildTime, int priority, int limit, Map<String, String> input) {
        TaskBuilder<?> builder = get(type);
        List<Task> tasks = (List<Task>) builder.build(input);
        taskAccessor.offerTasks(tasks, type, input);
        return tasks.size();
    }

    public TaskBuilder<?> get(int type) {
        return builderMap.get(type);
    }

}

