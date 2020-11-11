package com.manning.pulsar.chapter8.dataflow.routing;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.*;

import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.common.functions.ConsumerConfig;
import org.apache.pulsar.common.functions.FunctionConfig;
import org.apache.pulsar.functions.LocalRunner;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;

import com.manning.pulsar.chapter8.LocalRunnerFactory;

public class MessageFilterFunction implements Function<String, Void> {
	
	boolean initalized = false;
	String highPriorityTopic, lowPriorityTopic;
	Pattern pattern;

	public Void process(String input, Context context) throws Exception {
		
		if (!initalized) {
			init(context);
		}
       
        Matcher m = pattern.matcher(input);
        if (m.matches()) {
          context.newOutputMessage(highPriorityTopic, Schema.STRING).value(input).sendAsync();
        } else {
          context.newOutputMessage(lowPriorityTopic, Schema.STRING).value(input).sendAsync();
        }
        
        return null;
    }
	
	private void init(Context context) {
		String regex = context.getUserConfigValue("regex").toString();
		highPriorityTopic = context.getUserConfigValue("high-priority-topic").toString();
		lowPriorityTopic = context.getUserConfigValue("low-priority-topic").toString();
		pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		initalized = true;
	}

	public static void main(String[] args) throws Exception {
		
		Map<String, ConsumerConfig> inputSpecs = new HashMap<String, ConsumerConfig> ();
		inputSpecs.put("persistent://patterns/dataflow/cbr", 
				ConsumerConfig.builder().serdeClassName(Schema.STRING.getClass().getName()).build());
		
		Map<String, Object> userConfig = new HashMap<String, Object> ();
		userConfig.put("regex", "error");
		userConfig.put("high-priority-topic", "persistent://patterns/dataflow/high");
		userConfig.put("low-priority-topic", "persistent://patterns/dataflow/normal");
		
		FunctionConfig functionConfig = 
				FunctionConfig.builder()
				.className(ContentBasedRoutingFunction.class.getName())
				.inputs(Collections.singleton("persistent://patterns/dataflow/filter"))
				.inputSpecs(inputSpecs)
				.name("MessageFilter")
				.runtime(FunctionConfig.Runtime.JAVA)
				.userConfig(userConfig)
				.build();

		LocalRunner localRunner = LocalRunnerFactory.getLocalRunner(functionConfig);
		localRunner.start(false);
		Thread.sleep(30 * 1000);
		localRunner.stop();
		System.exit(0);
	}
}
