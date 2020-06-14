package com.manning.pulsar.iiot.analytics.quantiles;

import org.apache.pulsar.functions.api.Context;

import com.manning.pulsar.iiot.oilcooler.OilCoolerSensor;

public class OilSensorQuantilesFunction extends AbstractSensorQuantilesFunction<OilCoolerSensor> {

	@Override
	public Void process(OilCoolerSensor sensor, Context ctx) throws Exception {
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
		OilCoolerSensor.getClassSchema().getFields().forEach(field -> {
			sensorFieldNames.add(field.name());
		});
		initialized = true;
	}

}
