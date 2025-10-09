package com.stack3mpty.crawler.client.util;

import com.stack3mpty.crawler.common.business.business.crawler.Crawler;
import com.stack3mpty.crawler.common.business.common.model.TaskType;
import com.stack3mpty.crawler.common.business.common.util.Conf;
import com.stack3mpty.crawler.common.business.common.util.Pool;

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;

public class CrawlerMapHolder {
    private static final ConcurrentHashMap<Integer, Pool<Crawler, ReflectiveOperationException>> INSTANCE = new ConcurrentHashMap<>();

    private CrawlerMapHolder() {/**/
    }

    static {
        initCrawlerMap();
    }

    private static void initCrawlerMap() {
        for (String className : Conf
                .getClasses("com.stack3mpty.examples.crawler")) {
            final Class<?> clazz;
            try {
                clazz = Class.forName(className);
            } catch (Error | RuntimeException | ReflectiveOperationException e) {
                continue;
            }
            if (!Crawler.class.isAssignableFrom(clazz)) {
                continue;
            }
            TaskType annot = clazz.getAnnotation(TaskType.class);
            if (annot == null) {
                continue;
            }
            INSTANCE.put(annot.value(),
                    new Pool<>() {
                        @Override
                        protected Crawler makeObject() throws ReflectiveOperationException {
                            Constructor<?> constructor = clazz.getDeclaredConstructor();
                            return (Crawler) constructor.newInstance();
                        }

                        @Override
                        protected void destroyObject(Crawler coreWorker) {/**/
                        }
                    });
        }
        for (String className : Conf.getClassesByJar("crawler")) {
            final Class<?> clazz;
            try {
                clazz = Class.forName(className);
            } catch (Error | RuntimeException | ReflectiveOperationException e) {
                continue;
            }
            if (!Crawler.class.isAssignableFrom(clazz)) {
                continue;
            }
            TaskType annot = clazz.getAnnotation(TaskType.class);
            if (annot == null) {
                continue;
            }
            INSTANCE.put(annot.value(),
                    new Pool<>() {
                        @Override
                        protected Crawler makeObject() throws ReflectiveOperationException {
                            Constructor<?> constructor = clazz.getDeclaredConstructor();
                            return (Crawler) constructor.newInstance();
                        }

                        @Override
                        protected void destroyObject(Crawler coreWorker) {/**/
                        }
                    });
        }
    }

    public static Pool<Crawler, ReflectiveOperationException> getCrawler(int type) {
        return INSTANCE.get(type);
    }
}
