package com.manning.pulsar.chapter4.functions.sdk;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;
import org.slf4j.Logger;

public class KeywordFilterFunction implements Function<String, String> {
	
	public static final String KEYWORD_CONFIG = "keyword";
	public static final String IGNORE_CONFIG = "ignore-case";
	
	@Override
    public String process(String input, Context ctx) {
		Logger LOG = ctx.getLogger();
        Optional<Object> keyword = ctx.getUserConfigValue(KEYWORD_CONFIG);
        Optional<Object> ignoreConfig = ctx.getUserConfigValue(IGNORE_CONFIG);
        
        boolean ignoreCase = ignoreConfig.isPresent() ? (boolean) ignoreConfig.get(): false;
        LOG.info(String.format("Input {%s}, Keyword: {%s)", input, keyword.get()));
        List<String> words = Arrays.asList(input.split("\\s"));
        
        if (!keyword.isPresent()) {
        	return null;
        } else if (ignoreCase && words.stream().anyMatch(s -> s.equalsIgnoreCase((String) keyword.get()))) {
        	return input;
        } else if (words.contains(keyword.get())) {
        	return input;
        }
        return null;
	}
}
