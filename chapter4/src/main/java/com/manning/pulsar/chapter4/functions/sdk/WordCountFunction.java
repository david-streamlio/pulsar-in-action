package com.manning.pulsar.chapter4.functions.sdk;

import java.util.Arrays;

import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;

public class WordCountFunction implements Function<String, Void> {
    @Override
    public Void process(String input, Context context) throws Exception {
        Arrays.asList(input.split("\\."))
           .forEach(word -> context.incrCounter(word, 1));
        return null;
    }
}

