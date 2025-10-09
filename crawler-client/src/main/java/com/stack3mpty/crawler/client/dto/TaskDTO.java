package com.stack3mpty.crawler.client.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author ckai
 * @date 2025/9/30 17:15
 */
@Data
public class TaskDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String taskType;

    private Integer type;

    private Integer id;

    private Long buildTime;
}
