package com.manning.pulsar.chapter7.dataflow.routing;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.common.functions.ConsumerConfig;
import org.apache.pulsar.common.functions.FunctionConfig;
import org.apache.pulsar.functions.LocalRunner;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;

import com.manning.pulsar.chapter7.LocalRunnerFactory;

public class ContentBasedRoutingFunction implements Function<String, Void> {
	
	boolean initalized = false;
	String notSupportedTopic;
	Map<String, String> warehouseMap = new HashMap<String, String> ();
	
	public Void process(String input, Context context) throws Exception {
        
		if (!initalized) {
			init(context);
		}
		
		if (warehouseMap.containsKey(input)) {
			context.newOutputMessage(warehouseMap.get(input), Schema.STRING).value(input).sendAsync();
		} 
		
        context.newOutputMessage(notSupportedTopic, Schema.STRING).value(input).sendAsync();
		return null;
    }
	
	private void init(Context context) {
		for (int idx = 1; idx < 6; idx++) {
			String topic = context.getUserConfigValue("warehouse-" + idx + "-topic").toString();
			List<String> keys = Arrays.asList(
				StringUtils.split(context.getUserConfigValue("warehouse-" + idx + "-states").toString()));
			
			keys.forEach(key -> {
				warehouseMap.put(key, topic);
			});
			
		}
		notSupportedTopic = context.getUserConfigValue("not-supported-topic").toString();
		initalized = true;
	}
	
	public static void main(String[] args) throws Exception {
		
		Map<String, ConsumerConfig> inputSpecs = new HashMap<String, ConsumerConfig> ();
		inputSpecs.put("persistent://patterns/dataflow/cbr", 
				ConsumerConfig.builder().serdeClassName(Schema.STRING.getClass().getName()).build());
		
		
		Map<String, Object> userConfig = new HashMap<String, Object> ();
		userConfig.put("warehouse-1-states", "NY, NJ, CT, MA, RI");
		userConfig.put("warehouse-2-states", "PA, MD, OH, MI, IN, IL");
		userConfig.put("warehouse-3-states", "NE, KS, ND, SD, OK, TX");
		userConfig.put("warehouse-4-states", "VA, NC, SC, GA, FL, AL, MS, LA");
		userConfig.put("warehouse-5-states", "WA, OR, CA, NV, ID, UT, AZ");

		userConfig.put("warehouse-1-topic", "persistent://patterns/dataflow/warehouse-1");
		userConfig.put("warehouse-2-topic", "persistent://patterns/dataflow/warehouse-2");
		userConfig.put("warehouse-3-topic", "persistent://patterns/dataflow/warehouse-3");
		userConfig.put("warehouse-4-topic", "persistent://patterns/dataflow/warehouse-4");
		userConfig.put("warehouse-5-topic", "persistent://patterns/dataflow/warehouse-5");
		userConfig.put("not-supported-topic", "persistent://patterns/dataflow/rejected");
		
		FunctionConfig functionConfig = 
				FunctionConfig.builder()
				.className(ContentBasedRoutingFunction.class.getName())
				.inputs(Collections.singleton("persistent://patterns/dataflow/cbr"))
				.inputSpecs(inputSpecs)
				.name("ContentBasedRouter")
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
