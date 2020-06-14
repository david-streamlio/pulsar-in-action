package com.manning.pulsar.iiot.analytics.quantiles.sink;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.pulsar.client.api.SubscriptionInitialPosition;
import org.apache.pulsar.common.functions.ConsumerConfig;
import org.apache.pulsar.common.io.SinkConfig;
import org.apache.pulsar.functions.LocalRunner;

public class QuantilesSinkTest {

	public static void main(String[] args) throws Exception {
		
		Map<String, ConsumerConfig> inputSpecs = new HashMap<String, ConsumerConfig> ();
	    inputSpecs.put("persistent://public/default/quants", 
	    		ConsumerConfig.builder().schemaType("avro").build());
		
	
	    Map<String, Object> configs = new HashMap<String, Object> ();
	    configs.put("database.host", "localhost");
	    configs.put("database.port", "3306");
	    configs.put("database.name", "IIOT_ANALYTICS");
	    configs.put("database.user", "orbit");
	    configs.put("database.pass", "orbit");
	    
		SinkConfig sinkConfig = 
			SinkConfig.builder()
	    		.autoAck(false)
	    		.className(QuantilesSink.class.getName())
	    		.configs(configs)
	    		.inputs(Collections.singleton("persistent://public/default/quants"))
	    		.inputSpecs(inputSpecs)
	    		.name("Quantiles-sink")
	    		.sourceSubscriptionPosition(SubscriptionInitialPosition.Earliest)
	    		.build();
	    
		LocalRunner localRunner = 
	    	LocalRunner.builder()
	    		.brokerServiceUrl("pulsar://localhost:6650")
	    		.sinkConfig(sinkConfig)
	    		.build();
	    
	    localRunner.start(false);
	    Thread.sleep(60 * 1000);
	    localRunner.stop();
	    System.exit(0);
	}
}
