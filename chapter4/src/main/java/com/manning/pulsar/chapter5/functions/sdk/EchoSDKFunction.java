package com.manning.pulsar.chapter5.functions.sdk;

import java.util.stream.Collectors;

import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;
import org.slf4j.Logger;

public class EchoSDKFunction implements Function<String, String> {

	public String process(String input, Context ctx) {    
	       Logger LOG = ctx.getLogger();    
	       String inputTopics = ctx.getInputTopics()
	                      .stream()
	                     .collect(Collectors.joining(", "));    

	       String functionName = ctx.getFunctionName();   

	       String logMessage = 
	           String.format("A message with a value of \"%s\" has arrived "
	           		+ "on one of the following topics: %s\n",
	                input, inputTopics);

	        LOG.info(logMessage);    
	        String metricName = 
	           String.format("function-%s-messages-received", functionName);
	        
	        ctx.recordMetric(metricName, 1);    
	        return input;
	    }

}
