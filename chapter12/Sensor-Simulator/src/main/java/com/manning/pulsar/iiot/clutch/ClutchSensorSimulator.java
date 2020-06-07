package com.manning.pulsar.iiot.clutch;

import org.apache.avro.message.BinaryMessageEncoder;
import com.manning.pulsar.iiot.SensorSimulator;

public class ClutchSensorSimulator extends SensorSimulator<ClutchSensor> {

	public ClutchSensorSimulator(String host, int port, String topic) {
		super(host, port, topic);
	}

	@Override
	protected ClutchSensor generateEvent() {
//		return ClutchSensor.newBuilder()
//					.setBearing1Temp(54.9f)
//					.setBearing2Temp(57.9f)
//					.setBearing3Temp(58.4f)
//					.setBearing4Temp(60.7f)
//					.setBearing5Temp(69.3f)
//					.setBearing6Temp(69.3f)
//					.setClutchAttitude(53.4f)
//					.setSensorId(1)
//					.setThrustBearingTemp(56.2f)
//					.build();
		return null;
	}

	@Override
	protected BinaryMessageEncoder<ClutchSensor> getEncoder() {
		return ClutchSensor.getEncoder();
	}

}
