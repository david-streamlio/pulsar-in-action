package com.manning.pulsar.iiot.pump;

import org.apache.avro.message.BinaryMessageEncoder;

import com.manning.pulsar.iiot.SensorSimulator;

public class PumpSensorSimulator extends SensorSimulator<PumpSensor> {

	public PumpSensorSimulator(String host, int port, String topic) {
		super(host, port, topic);
	}

	@Override
	protected PumpSensor generateEvent() {
		return PumpSensor.newBuilder()
				.setOutputWaterPressure(14.0f)
				.setSensorId(2)
				.setSupplyWaterPressure(0.66f)
				.setTempAtDischargeNozzle(148)
				.setWaterFlowRate(246)
				.build();
	}

	@Override
	protected BinaryMessageEncoder<PumpSensor> getEncoder() {
		return PumpSensor.getEncoder();
	}

}
