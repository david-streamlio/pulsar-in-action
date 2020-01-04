package com.manning.pulsar.chapter4.functions;

import java.util.function.Function;

public class ExclamationFunction implements Function<String, String> {
    @Override
    public String apply(String input) {
        return String.format("%s!", input);
    }
}
