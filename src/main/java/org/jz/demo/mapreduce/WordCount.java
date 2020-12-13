package org.jz.demo.mapreduce;

import com.sun.javaws.exceptions.InvalidArgumentException;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jz
 * @date 2020/12/10
 */
public class WordCount implements MapReduce<Integer> {

    @Override
    public List<KeyValue<String, Integer>> map(String filepath) throws IOException {
        File file = new File(filepath);
        String words = FileUtils.readFileToString(file);
        return Arrays.stream(words.split("[^a-zA-Z']+")).map(word -> new DefaultKeyValue<>(word, 1)).collect(Collectors.toList());
    }

    @Override
    public String reduce(String key, List<Integer> values) {
        return String.valueOf(values.size());
    }


    public static void main(String[] args) throws IOException, InvalidArgumentException {
        if (args.length != 1) {
            throw new InvalidArgumentException(new String[]{"缺少文件路径"});
        }
        String filepath = args[0];
        WordCount wordCount = new WordCount();
        List<KeyValue<String, Integer>> map = wordCount.map(filepath);
    }
}
