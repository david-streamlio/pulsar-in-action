package com.gottaeat.functions.ordervalidation.translator;

import org.apache.pulsar.client.impl.schema.AvroSchema;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;

import com.gottaeat.domain.common.Address;
import com.gottaeat.domain.order.ValidatedFoodOrder;

public class AddressAdapter implements Function<Address, Void> {

	@Override
	public Void process(Address addr, Context ctx) throws Exception {
		ValidatedFoodOrder result = new ValidatedFoodOrder();
		result.setDeliveryLocation(addr);
		
		ctx.newOutputMessage(ctx.getOutputTopic(), AvroSchema.of(ValidatedFoodOrder.class))
		.properties(ctx.getCurrentRecord().getProperties())
		.value(result)
		.send();
		
		return null;
	}

}
