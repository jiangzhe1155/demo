package org.jz.demo.mapreduce;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author 江哲
 * @date 2020/12/12
 */
@Data
@Accessors(chain = true)
public class TaskObject {

    private int idx;
    private String filepath;
}
