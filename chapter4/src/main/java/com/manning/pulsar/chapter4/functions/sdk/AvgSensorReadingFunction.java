package com.manning.pulsar.chapter4.functions.sdk;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;

public class AvgSensorReadingFunction implements Function<Double, Void> {

	private static final String VALUES_KEY = "VALUES";
	
	@Override
	public Void process(Double input, Context ctx) throws Exception {
		
		CircularFifoQueue<Double> values = getValues(ctx);
		
		if (Math.abs(input - getAverage(values)) > 10.0) {
			// trigger an alarm.
		}
		
		values.add(input);
		ctx.putState(VALUES_KEY, serialize(values));
		
		return null;
	}
	
	private Double getAverage(CircularFifoQueue<Double> values) {
		return StreamSupport.stream(values.spliterator(), false)
		 .collect(Collectors.summingDouble(Double::doubleValue)) / values.size();
	}
	
	private CircularFifoQueue<Double> getValues(Context ctx) {
		if (ctx.getState(VALUES_KEY) == null) {
			return new CircularFifoQueue<Double>(100);
		} else {
			return deserialize(ctx.getState(VALUES_KEY));
		}
	}
	
	private ByteBuffer serialize(CircularFifoQueue<Double> values) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try (ObjectOutputStream out = new ObjectOutputStream(bos); ) {
		    
		  out.writeObject(values);
		  out.flush();
		  return ByteBuffer.wrap(bos.toByteArray());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private CircularFifoQueue<Double> deserialize(ByteBuffer buffer) {
		
		try (ByteArrayInputStream bis = new ByteArrayInputStream(buffer.array());
				ObjectInput in = new ObjectInputStream(bis);) {
		  return (CircularFifoQueue<Double>) in.readObject(); 
		 
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null; 
	}
	
}
