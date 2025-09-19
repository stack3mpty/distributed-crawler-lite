package com.stack3mpty.crawler.client.util;

import com.stack3mpty.crawler.common.business.business.crawler.Crawler;
import com.stack3mpty.crawler.common.business.common.model.TaskType;
import com.stack3mpty.crawler.common.business.common.util.Conf;
import com.stack3mpty.crawler.common.business.common.util.Pool;

import java.util.concurrent.ConcurrentHashMap;

public class CrawlerMapHolder {
    private static final ConcurrentHashMap<Integer, Pool<Crawler, ReflectiveOperationException>> INSTANCE = new ConcurrentHashMap<>();

    static {
        initCrawlerMap(INSTANCE);
    }

    private static void initCrawlerMap(ConcurrentHashMap<Integer, Pool<Crawler, ReflectiveOperationException>> instance) {
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
            INSTANCE.put(Integer.valueOf(annot.value()),
                    new Pool<Crawler, ReflectiveOperationException>() {
                        @Override
                        protected Crawler makeObject() throws ReflectiveOperationException {
                            Crawler crawler = (Crawler) clazz.newInstance();
//                            if (crawler instanceof RedialLockAware) {
//                                ((RedialLockAware) crawler).setRedialLock(LockFactory
//                                        .redialLock().readLock());
//                            }
                            return crawler;
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
            INSTANCE.put(Integer.valueOf(annot.value()),
                    new Pool<Crawler, ReflectiveOperationException>() {
                        @Override
                        protected Crawler makeObject() throws ReflectiveOperationException {
                            Crawler crawler = (Crawler) clazz.newInstance();
//                            if (crawler instanceof RedialLockAware) {
//                                ((RedialLockAware) crawler).setRedialLock(LockFactory
//                                        .redialLock().readLock());
//                            }
                            return crawler;
                        }

                        @Override
                        protected void destroyObject(Crawler coreWorker) {/**/
                        }
                    });
        }
    }
}
