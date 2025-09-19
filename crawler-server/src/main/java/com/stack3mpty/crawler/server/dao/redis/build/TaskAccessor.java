package com.stack3mpty.crawler.server.dao.redis.build;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.stack3mpty.crawler.common.business.business.taskAssembler.TaskAssembler;
import com.stack3mpty.crawler.common.business.common.config.TaskConfig;
import com.stack3mpty.crawler.common.business.common.model.Task;
import com.stack3mpty.crawler.common.business.common.model.TaskTypes;
import com.stack3mpty.crawler.common.business.common.util.GzipUtils;
import com.stack3mpty.crawler.common.business.common.util.JsonUtils;
import com.stack3mpty.crawler.common.business.common.util.TaskAssemblerLoadUtil;
import com.stack3mpty.crawler.server.dao.redis.TaskJedisPools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service("taskAccessor")
public class TaskAccessor {

    @Resource
    private TaskJedisPools pools;


    public void offerTasks(List<Task> tasks, int type, Map<String, String> input) {
        String typeName = TaskTypes.toString(type);
        final Task task = tasks.get(0);
        final long buildTime = task.getBuildTime();
        final int priority = task.getPriority();
        int[] last_ = new int[1];
        int taskNum = tasks.size();
        final String jobKey = getJobKey(typeName, buildTime);
        setJobs(type, input, typeName, jobKey, last_, taskNum, buildTime, priority);
        AtomicInteger count = setTasks(type, typeName, tasks, last_, taskNum, buildTime, priority, input);
        log.info("offerTasks type: {}, buildTime: {}, priority: {}, taskNum: {}, last: {}, count: {}",
                typeName, buildTime, priority, taskNum, last_[0], count.get());
    }

    private void setJobs(int type, Map<String, String> input, String typeName, String jobKey, int[] last, int taskNum, long buildTime, int priority) {
        int poolIndex = Math.abs(jobKey.hashCode()) % pools.getPoolNum();
        Long l =  updateJobTaskNum(jobKey, taskNum, poolIndex);
        last[0] = l == null ? 0 : l.intValue();
    }

    private AtomicInteger setTasks(int type ,String typeName, List<Task> tasks, int[] last_, int taskNum, long buildTime, int priority, Map<String, String> input) {
        AtomicInteger count = new AtomicInteger(0);
        final ArrayList<ArrayList<Task>> taskss = new ArrayList<>();
        for (int i = 0; i < pools.getPoolNum(); i++) {
            taskss.add(new ArrayList<>());
        }
        int id = 0;
        int last = last_[0];
        id = last - taskNum;
        final byte[] queueKey = getQueueKey(typeName).getBytes();
        final byte[] tasksKey = getTaskKey(typeName, buildTime).getBytes();
        for (Task task : tasks) {
            task.setId(id);
            task.setPriority(priority);
            taskss.get(id % pools.getPoolNum()).add(task);
            id++;
        }
        List<Future<Integer>> submits = new ArrayList<>();
        for (int i = 0; i < pools.getPoolNum(); i++) {
            doOfferTasks(type, tasks, i, buildTime, tasksKey, queueKey, priority);
            submits.add(CompletableFuture.completedFuture(taskss.get(i).size()));
        }

        submits.forEach(submit -> {
            try {
                count.addAndGet(submit.get());
            } catch (Exception e) {
                log.error("task build error");
            }
        });

        return count;
    }

    private void doOfferTasks(int type, List<Task> tasks, int poolIndex, long buildTime, byte[] tasksKey, byte[] queueKey, int priority) {
        final List<List<Task>> splitTasks = Lists.partition(tasks, 1024);
        for (List<Task> taskList : splitTasks) {
            HashMap<byte[], byte[]> taskMap = new HashMap<>();
            ArrayList<byte[]> queue = new ArrayList<>();
            for (Task task : taskList) {
                int id = task.getId();
                byte[] taskBytes = serializeTask(task);
                taskMap.put(Ints.toByteArray(id), taskBytes);
                byte[] bb = new byte[12];
                System.arraycopy(Longs.toByteArray(buildTime), 0, bb, 0, 8);
                System.arraycopy(Ints.toByteArray(id), 0, bb, 8, 4);
                queue.add(bb);
            }

            RedisTemplate<String, Object> redisTemplate = pools.getRedisTemplate(poolIndex);

            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                connection.hashCommands().hMSet(tasksKey, taskMap);
                connection.keyCommands().expire(tasksKey, TaskConfig.getTaskExpireTime(type));
                queue.forEach(queueItem -> connection.listCommands().lPush(queueKey, queueItem));
                connection.keyCommands().expire(queueKey, TaskConfig.getQueueExpireTime(type));
                return null;
            });
        }
    }

    private byte[] serializeTask(Task task) {
        return GzipUtils.compress(JsonUtils.toJson(task));
    }

    private Task deserializeTask(byte[] taskData) {
        return JsonUtils.fromJson(GzipUtils.uncompressToString(taskData), Task.class);
    }

    private String getQueueKey(String typeName) {
        return "stack3mpty.queue:" + typeName;
    }

    private String getTaskKey(String typeName, Long buildTime) {
        return "stack3mpty.task:" + typeName + ":" + buildTime;
    }

    private String getJobKey(String typeName, long buildTime) {
        return "stack3mpty.job:" + typeName + ":" + buildTime;
    }

    private Long updateJobTaskNum(String jobKey, int taskNum, int poolIndex) {
        RedisTemplate<String, Object> redisTemplate = pools.getRedisTemplate(poolIndex);

        // 使用Lua脚本保证原子性操作并返回新值
        String luaScript =
                "local current = redis.call('GET', KEYS[1]) " +
                        "if current == false then " +
                        "  current = 0 " +
                        "else " +
                        "  current = tonumber(current) " +
                        "end " +
                        "local newValue = current + ARGV[1] " +
                        "redis.call('SET', KEYS[1], newValue) " +
                        "redis.call('EXPIRE', KEYS[1], ARGV[2]) " +
                        "return newValue";

        // 执行Lua脚本
        Long result = redisTemplate.execute((RedisCallback<Long>) connection -> {
            return (Long) connection.eval(
                    luaScript.getBytes(),
                    org.springframework.data.redis.connection.ReturnType.INTEGER,
                    1,
                    jobKey.getBytes(),
                    String.valueOf(taskNum).getBytes(),
                    String.valueOf(TaskConfig.getJobExpireTime()).getBytes()
            );
        });

        log.debug("Updated job {} task count to: {}", jobKey, result);
        return result;
    }

    public Task pollTask(int type, String clientCode) {

        TaskAssembler assembler = TaskAssemblerLoadUtil.getAssembler(type);
        if (assembler != null && !assembler.isAvailable(type, clientCode)) {
            return null;
        }
        Task task = pollTask(type);
        if (task != null) {
            task = assembler == null ? task : assembler.assemble(task, clientCode);
        }
        return task;
    }

    private Task pollTask(int type) {
        String typeName = TaskTypes.toString(type);
        String queueKey = getQueueKey(typeName);
        String taskPrefix = getTaskPrefix(typeName);
        AtomicReference<Task> task = new AtomicReference<>(null);
        int randomInt = new Random().nextInt() & 0x7FFFFFFF;
        int pollTaskMaxRepeatCount = TaskConfig.getPollTaskMaxRepeatCount();
        AtomicInteger queueEmptyCount = new AtomicInteger(0);
        for (int i = 0; i < pollTaskMaxRepeatCount && queueEmptyCount.get() < pools.getPoolNum(); i++) {
            int poolIndex = (randomInt + i) % pools.getPoolNum();
            RedisTemplate<String, Object> redisTemplate = pools.getRedisTemplate(poolIndex);
            byte[] queueData = redisTemplate.execute((RedisCallback<byte[]>) connection ->
                    connection.listCommands().lPop(queueKey.getBytes()));

            if (queueData == null || queueData.length != 12) {
                queueEmptyCount.incrementAndGet();
                continue;
            }

            long buildTime = Longs.fromByteArray(Arrays.copyOfRange(queueData, 0, 8));
            int taskId = Ints.fromByteArray(Arrays.copyOfRange(queueData, 8, 12));

            byte[] taskKey = (taskPrefix + buildTime).getBytes();
            byte[] taskFieId = Ints.toByteArray(taskId);
            byte[] taskData = redisTemplate.execute((RedisCallback<byte[]>) connection ->
                    connection.hashCommands().hGet(taskKey, taskFieId));
            if (taskData == null) {
                continue;
            }
            task.set(deserializeTask(taskData));
            if (task.get() != null) {
                break;
            }
        }
        return task.get();
    }

    private String getTaskPrefix(String typeName) {
        return "stack3mpty.task:" + typeName + ":";
    }
}
