package com.gottaeat.functions.ordervalidation.translator;


import org.apache.pulsar.client.impl.schema.AvroSchema;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;

import com.gottaeat.domain.order.ValidatedFoodOrder;
import com.gottaeat.domain.resturant.SolicitationResponse;

public class FoodOrderAdapter implements Function<SolicitationResponse, Void> {


	@Override
	public Void process(SolicitationResponse food, Context ctx) throws Exception {
		ValidatedFoodOrder result = new ValidatedFoodOrder();
		result.setFood(food);
		
		ctx.newOutputMessage(ctx.getOutputTopic(), AvroSchema.of(ValidatedFoodOrder.class))
		.properties(ctx.getCurrentRecord().getProperties())
		.value(result)
		.send();
	
		return null;
	}

}
