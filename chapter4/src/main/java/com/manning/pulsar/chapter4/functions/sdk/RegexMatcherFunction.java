package com.manning.pulsar.chapter4.functions.sdk;

import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;

public class RegexMatcherFunction implements Function<String, String> {

	public static final String REGEX_CONFIG = "regex-pattern";

	@Override
	public String process(String input, Context ctx) throws Exception {
        Optional<Object> config = ctx.getUserConfigValue(REGEX_CONFIG);
        
        if (config.isPresent() && 
        	config.get().getClass().getName().equals(String.class.getName())) {
           Pattern pattern = Pattern.compile(config.get().toString());
           if (pattern.matcher(input).matches()) {
        	   String metricName = 
        	       String.format("function-%s-regex-matches", ctx.getFunctionName());
        	        
        	   ctx.recordMetric(metricName, 1);    
           }
        }	
		return null;	
	}
}
