package com.manning.pulsar.chapter8.dataflow.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.common.functions.ConsumerConfig;
import org.apache.pulsar.common.functions.FunctionConfig;
import org.apache.pulsar.functions.LocalRunner;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;
import org.apache.pulsar.functions.api.Record;

import com.manning.pulsar.chapter8.LocalRunnerFactory;

public class DynamicRoutingFunction implements Function<String, Void> {

	boolean initalized = false;
	String controlTopic;
	
	@Override
	public Void process(String input, Context context) throws Exception {
		if (!initalized) {
			init(context);
		}
		
		if (context.getCurrentRecord().getTopicName().get().equals(controlTopic)) {
			handleControlMsg(context.getCurrentRecord());
		}
		
		return null;
	}
	
	private void handleControlMsg(Record<?> currentRecord) {
		// TODO Auto-generated method stub
		
	}

	private void init(Context context) {
		controlTopic = context.getUserConfigValue("control-topic").toString();
		initalized = true;
	}
	
	public static void main(String[] args) throws Exception {
		
		Map<String, ConsumerConfig> inputSpecs = new HashMap<String, ConsumerConfig> ();
		inputSpecs.put("persistent://patterns/dataflow/dynamic-source", 
				ConsumerConfig.builder().serdeClassName(Schema.STRING.getClass().getName()).build());
		
		inputSpecs.put("persistent://patterns/dataflow/dynamic-control", 
				ConsumerConfig.builder().serdeClassName(Schema.STRING.getClass().getName()).build());
		
		List<String> topics = new ArrayList<String>();
		Collections.addAll(topics, 
				"persistent://patterns/dataflow/dynamic-source", 
				"persistent://patterns/dataflow/dynamic-control");
		
		Map<String, Object> userConfig = new HashMap<String, Object> ();
		userConfig.put("control-topic", "persistent://patterns/dataflow/dynamic-control");
		
		FunctionConfig functionConfig = 
				FunctionConfig.builder()
				.className(ContentBasedRoutingFunction.class.getName())
				.inputs(topics)
				.inputSpecs(inputSpecs)
				.name("DynamicRouter")
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
