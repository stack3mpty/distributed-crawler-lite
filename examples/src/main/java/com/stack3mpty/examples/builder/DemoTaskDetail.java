package com.stack3mpty.examples.builder;

import com.stack3mpty.crawler.common.business.common.model.TaskDetail;
import lombok.Data;

@Data
public class DemoTaskDetail implements TaskDetail {
    private String url;
}
