package org.jz.demo.mapreduce;

import lombok.Data;

/**
 * @author jz
 * @date 2020/12/10
 */
@Data
public class Response {

    public static final int MAP_TASK = 1;
    public static final int REDUCE_TASK = 2;

    private int taskType;

    private TaskObject taskObject;

    private boolean isDone;
}
