package com.stack3mpty.crawler.common.business.common.util;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

public abstract class Pool<T, E extends Exception> implements AutoCloseable {
    protected abstract T makeObject() throws E;
    /**
     * @param obj
     * @throws E
     */
    protected void activateObject(T obj) throws E {/**/}
    /**
     * @param obj
     * @throws E
     */
    protected void passivateObject(T obj) throws E {/**/}
    /** @param obj */
    protected abstract void destroyObject(T obj);

    class Entry {
        T obj;
        long expire;

        Entry(T obj, long expire) {
            this.obj = obj;
            this.expire = expire;
        }
    }

    private int timeout, interval;
    private ConcurrentLinkedDeque<Entry> deque = new ConcurrentLinkedDeque<>();
    private AtomicLong accessed = new AtomicLong(System.currentTimeMillis());
    private volatile boolean closed = false;

    public Pool() {
        this(0, 0);
    }

    public Pool(int timeout, int interval) {
        this.timeout = timeout;
        this.interval = interval;
    }

    public T borrowObject() throws E {
        Entry entry;
        if (timeout > 0) {
            long now = System.currentTimeMillis();
            long accessed_ = accessed.get();
            if (now > accessed_ + interval &&
                    accessed.compareAndSet(accessed_, now)) {
                while ((entry = deque.pollLast()) != null) {
                    if (now < entry.expire) {
                        deque.offerLast(entry);
                        break;
                    }
                    destroyObject(entry.obj);
                }
            }
        }

        while ((entry = deque.pollFirst()) != null) {
            try {
                activateObject(entry.obj);
                return entry.obj;
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                destroyObject(entry.obj);
            }
        }
        T obj = makeObject();
        try {
            activateObject(obj);
            return obj;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            destroyObject(obj);
            throw e;
        }
    }

    public void returnObject(T obj) {
        try {
            passivateObject(obj);
            if (closed) {
                destroyObject(obj);
            } else {
                deque.offerLast(new Entry(obj, System.currentTimeMillis() + timeout));
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            destroyObject(obj);
        }
    }

    public void reopen() {
        closed = false;
    }

    @Override
    public void close() {
        closed = true;
        Entry entry;
        while ((entry = deque.pollFirst()) != null) {
            destroyObject(entry.obj);
        }
    }
}