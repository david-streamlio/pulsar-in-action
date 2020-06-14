package com.manning.pulsar.iiot.analytics.quantiles;

import org.apache.pulsar.functions.api.Context;

import com.manning.pulsar.iiot.clutch.ClutchSensor;

public class ClutchSensorQuantilesFunction extends AbstractSensorQuantilesFunction<ClutchSensor> {
	
	public Void process(ClutchSensor sensor, Context ctx) throws Exception {
		
		if (!initialized) {
			init(ctx);
		}
		
		if (shouldPublish()) {
			publish(ctx);
		}
		
		sensorFieldNames.parallelStream().forEach(field -> {
			getSketch(sensor.getSensorId(), field).update((double) sensor.get(field));
		});
		
		return null;
	}
	
	protected void init(Context ctx) {
		super.init(ctx);
		ClutchSensor.getClassSchema().getFields().forEach(field -> {
			sensorFieldNames.add(field.name());
		});
		initialized = true;
	}

}
