package com.manning.pulsar.chapter4.functions.sdk;

import java.util.Arrays;
import java.util.List;

import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;

public class WordCountFunction implements Function<String, Integer> {
    @Override
    public Integer process(String input, Context context) throws Exception {
        List<String> words = Arrays.asList(input.split("\\s"));
//    	words.forEach(word -> context.incrCounter(word, 1));
        return new Integer(words.size());
    }
    
}

