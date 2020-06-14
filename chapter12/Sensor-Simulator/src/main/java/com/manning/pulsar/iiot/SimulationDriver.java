package com.manning.pulsar.iiot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.manning.pulsar.iiot.clutch.ClutchSensorSimulator;
import com.manning.pulsar.iiot.engine.EngineSensorSimulator;
import com.manning.pulsar.iiot.oilcooler.OilCoolerSensorSimulator;
import com.manning.pulsar.iiot.pump.PumpSensorSimulator;

public class SimulationDriver {

	public static void main(String[] args) throws InterruptedException {
		ExecutorService executor = Executors.newFixedThreadPool(5);
		List<SimulatorThread> sims = getSimulators();
		
		sims.forEach(sim -> {
			executor.execute(sim);
		});
		
		Thread.sleep(5 * 60 * 1000);
		
		sims.forEach(sim -> {
			sim.halt();
		});
		
	}
	
	private static List<SimulatorThread> getSimulators() {
		List<SimulatorThread> sims = new ArrayList<SimulatorThread> ();
		
		sims.add(new SimulatorThread(new ClutchSensorSimulator("localhost", 1883, "persistent://public/default/clutch_sensor")));
		sims.add(new SimulatorThread(new EngineSensorSimulator("localhost", 1883, "persistent://public/default/engine_sensor")));
		sims.add(new SimulatorThread(new OilCoolerSensorSimulator("localhost", 1883, "persistent://public/default/oil_sensor")));
		sims.add(new SimulatorThread(new PumpSensorSimulator("localhost", 1883, "persistent://public/default/pump_sensor")));
		return sims;
	}
	
	@SuppressWarnings("rawtypes")
	private static class SimulatorThread extends Thread {
		
		private SensorSimulator sim;
		private boolean halted = false;
		
		public SimulatorThread(SensorSimulator s) {
			sim = s;
		}
		
		public void run() {
			while (!halted) {
				try {
					sim.emit();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		public void halt() {
			this.halted = true;
		}
	}

}
