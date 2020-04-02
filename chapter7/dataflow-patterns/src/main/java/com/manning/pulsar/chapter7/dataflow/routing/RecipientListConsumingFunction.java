package com.manning.pulsar.chapter7.dataflow.routing;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.apache.pulsar.client.impl.schema.AvroSchema;
import org.apache.pulsar.common.functions.ConsumerConfig;
import org.apache.pulsar.common.functions.FunctionConfig;
import org.apache.pulsar.functions.LocalRunner;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;

import com.gottaeat.domain.order.FoodOrder;
import com.manning.pulsar.chapter7.LocalRunnerFactory;

/**
 * Receives a message with a list of recipients in one of 
 * the message properties, which it then uses to route the messages
 * 
 * @see https://www.enterpriseintegrationpatterns.com/patterns/messaging/RecipientList.html
 *
 */
public class RecipientListConsumingFunction implements Function<FoodOrder, Void> {
	
	private Random rnd = new Random();

	@Override
	public Void process(FoodOrder input, Context context) throws Exception {
		
		String[] dest = StringUtils.split(context.getCurrentRecord()
					       .getProperties().get("recipients"), ",");
		
		for (int idx = 0; idx < dest.length; idx++) {
			context.newOutputMessage(
				getTopicByRecipient(dest[idx]), 
				AvroSchema.of(FoodOrder.class))
			.value(input)
			.properties(context.getCurrentRecord().getProperties()) // Passes return-addr and correlation-id
			.property("offered-price", getOfferedPricedByDriver(input, dest[idx]) + "")
			.sendAsync();
		}
		
		return null;
	}
	
	private String getTopicByRecipient(String rec) {
		return "persistent://drivers/offers/driver-" + rec;
	}
	
	private Float getOfferedPricedByDriver(FoodOrder input, String rec) {
		int margin = rnd.nextInt(10);
		return input.getTotal() * (1 + (margin/100));
	}
	
	public static void main(String[] args) throws Exception {
		
		Map<String, ConsumerConfig> inputSpecs = new HashMap<String, ConsumerConfig> ();
	    inputSpecs.put("persistent://drivers/inbound/food-orders", 
	    		ConsumerConfig.builder().schemaType("avro").build());
		
	    FunctionConfig functionConfig = 
	    	FunctionConfig.builder()
	    	.className(RecipentListGeneratingFunction.class.getName())
	    	.inputs(Collections.singleton("persistent://drivers/inbound/food-orders"))
	    	.inputSpecs(inputSpecs)
	    	.name("")
	    	.runtime(FunctionConfig.Runtime.JAVA)
	    	.build();
	    
		LocalRunner localRunner = LocalRunnerFactory.getLocalRunner(functionConfig);
		localRunner.start(false);
		Thread.sleep(30 * 1000);
		localRunner.stop();
		System.exit(0);
	}

}
