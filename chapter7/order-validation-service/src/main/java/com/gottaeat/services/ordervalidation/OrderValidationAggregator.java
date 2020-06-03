package com.gottaeat.services.ordervalidation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.Map;

import org.apache.pulsar.client.impl.schema.AvroSchema;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;

import com.gottaeat.domain.order.ValidatedFoodOrder;

public class OrderValidationAggregator implements Function<ValidatedFoodOrder, Void> {

	@Override
	public Void process(ValidatedFoodOrder in, Context ctx) throws Exception {
		
		Map<String, String> props = ctx.getCurrentRecord().getProperties();
		String correlationId = props.get("order-id");
		
		ValidatedFoodOrder order;
		if (ctx.getState(correlationId.toString()) == null) {
			order = new ValidatedFoodOrder();
		} else {
			order = deserialize(ctx.getState(correlationId.toString()));
		}
		
		updateOrder(order, in);
		
		if (isComplete(order)) {
			ctx.newOutputMessage(ctx.getOutputTopic(), AvroSchema.of(ValidatedFoodOrder.class))
				.properties(props)
				.value(order)
				.sendAsync();
			
			ctx.putState(correlationId.toString(), null);
		} else {
			ctx.putState(correlationId.toString(), serialize(order));
		}
		
		return null;
	}
	
	private boolean isComplete(ValidatedFoodOrder order) {
		return (order != null && order.getDeliveryLocation() != null 
				&& order.getFood() != null && order.getPayment() != null
				&& order.getMeta() != null);
	}
	
	private void updateOrder(ValidatedFoodOrder val, ValidatedFoodOrder response) {
		if (response.getDeliveryLocation() != null && val.getDeliveryLocation() == null) {
			val.setDeliveryLocation(response.getDeliveryLocation());
		}
		
		if (response.getFood() != null && val.getFood() == null) {
			val.setFood(response.getFood());
		}
		
		if (response.getMeta() != null && val.getMeta() == null) {
			val.setMeta(response.getMeta());
		}
		
		if (response.getPayment() != null && val.getPayment() == null) {
			val.setPayment(response.getPayment());
		}
		
	}
	
	private ByteBuffer serialize(ValidatedFoodOrder order) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(order);
        out.close();
        baos.close();
		ByteBuffer buffer = ByteBuffer.allocate(baos.size());
		buffer.put(baos.toByteArray());
		return buffer;
	}
	
	private ValidatedFoodOrder deserialize(ByteBuffer buffer) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bais = new ByteArrayInputStream(buffer.array());
		ObjectInputStream in = new ObjectInputStream(bais);
		return (ValidatedFoodOrder) in.readObject();
	}

}
