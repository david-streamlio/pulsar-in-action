package com.manning.pulsar.chapter8.dataflow.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.impl.schema.AvroSchema;
import org.apache.pulsar.common.functions.ConsumerConfig;
import org.apache.pulsar.common.functions.FunctionConfig;
import org.apache.pulsar.functions.LocalRunner;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;
import org.apache.pulsar.shade.org.apache.commons.lang.StringUtils;

import com.gottaeat.domain.order.FoodOrder;
import com.manning.pulsar.chapter8.LocalRunnerFactory;

/**
 *  We may want to route an order message to a select list of restaurants to 
 *  obtain a quote for the requested item. Rather than sending the request 
 *  to all vendors, we may want to control which vendors receive the request, 
 *  possibly based on user preferences
 *
 */
public class RecipentListGeneratingFunction implements Function<FoodOrder, Void> {

	@Override
	public Void process(FoodOrder input, Context context) throws Exception {
		
		// Get list of potential drivers, etc.
		List<String> recipients = getRecipients();
		
		context.newOutputMessage("persistent://drivers/inbound/accepted-control", Schema.STRING)
			.value("")
			.property("num-recipients", recipients.size() + "")
//			.property("correlation-id", input.getOrderId() + "")
			.send();

		context.newOutputMessage(context.getOutputTopic(), AvroSchema.of(FoodOrder.class))
			.value(input)
			.property("recipients", StringUtils.join(recipients, ","))
			.property("return-addr", "persistent://drivers/inbound/accepted")  // If we want to aggregate or gather the responses
//			.property("correlation-id", input.getOrderId() + "")
			.sendAsync();
		
		return null;
	}
	
	private List<String> getRecipients() {
		List<String> rec = new ArrayList<String> ();
		Collections.addAll(rec, "topic-1", "topic-2");
		return rec;
	}
	
	public static void main(String[] args) throws Exception {
		
		Map<String, ConsumerConfig> inputSpecs = new HashMap<String, ConsumerConfig> ();
	    inputSpecs.put("persistent://orders/inbound/food-orders", 
	    		ConsumerConfig.builder().schemaType("avro").build());
		
	    FunctionConfig functionConfig = 
	    	FunctionConfig.builder()
	    	.className(RecipentListGeneratingFunction.class.getName())
	    	.inputs(Collections.singleton("persistent://orders/inbound/food-orders"))
	    	.inputSpecs(inputSpecs)
	    	.name("driver-candidates")
	    	.output("persistent://drivers/inbound/food-orders")
	    	.outputSchemaType("avro")
	    	.runtime(FunctionConfig.Runtime.JAVA)
	    	.build();
	    
		LocalRunner localRunner = LocalRunnerFactory.getLocalRunner(functionConfig);
		localRunner.start(false);
		Thread.sleep(30 * 1000);
		localRunner.stop();
		System.exit(0);
	}

}
