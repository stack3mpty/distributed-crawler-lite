package com.stack3mpty.crawler.common.business.business.builder;

import java.io.Serializable;

public interface TaskBuilder<T> extends Serializable {

    T build(Object input);
}
