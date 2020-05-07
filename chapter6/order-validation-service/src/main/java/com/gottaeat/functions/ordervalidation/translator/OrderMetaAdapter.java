package com.gottaeat.functions.ordervalidation.translator;


import org.apache.pulsar.client.impl.schema.AvroSchema;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;

import com.gottaeat.domain.order.FoodOrderMeta;
import com.gottaeat.domain.order.ValidatedFoodOrder;

public class OrderMetaAdapter implements Function<FoodOrderMeta, Void> {


	@Override
	public Void process(FoodOrderMeta meta, Context ctx) throws Exception {
		ValidatedFoodOrder result = new ValidatedFoodOrder();
		result.setMeta(meta);
		
		ctx.newOutputMessage(ctx.getOutputTopic(), AvroSchema.of(ValidatedFoodOrder.class))
		.properties(ctx.getCurrentRecord().getProperties())
		.value(result)
		.send();
	
		return null;
	}

}
