package org.jz.demo.mapreduce;

import org.apache.commons.collections4.KeyValue;

import java.io.IOException;
import java.util.List;

/**
 * @author jz
 * @date 2020/12/10
 */
public interface MapReduce<V> {

    String reduce(String key, List<V> values);

    List<KeyValue<String, V>> map(String filepath) throws IOException;

}
