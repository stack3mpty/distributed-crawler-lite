package com.stack3mpty.crawler.common.business.common.model;

import com.stack3mpty.crawler.common.business.common.util.Conf;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskTypes {

    private static Map<String, Integer> encodeMap = new HashMap<>();
    private static Map<Integer, String> decodeMap = new HashMap<>();

    static {
        Map<String, List<String>> buTaskTypeMap = Conf.getClassMapByJar("taskType");
        for (Map.Entry<String, List<String>> entry : buTaskTypeMap.entrySet()) {
            List<String> buTaskTypes = entry.getValue();
            for (String itemClass : buTaskTypes) {
                Class<?> clazz;
                try {
                    clazz = Class.forName(itemClass);
                    Field[] field = clazz.getDeclaredFields();
                    for (Field value : field) {
                        String taskTypeName = value.getName();
                        Integer taskTypeValue = value.getInt(null);
                        encodeMap.put(taskTypeName, taskTypeValue);
                        decodeMap.put(taskTypeValue, taskTypeName);
                    }

                } catch (ClassNotFoundException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static int parseType(String typeName){
        Integer encode = encodeMap.get(typeName);
        return encode == null ? 0 : encode;
    }

    public static String toString(int type) {
        return decodeMap.getOrDefault(type, "");
    }
}
