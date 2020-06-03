package com.gottaeat.services.customer.simulator;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import org.apache.pulsar.common.io.SourceConfig;
import org.apache.pulsar.functions.LocalRunner;
import org.apache.pulsar.functions.api.Record;
import org.apache.pulsar.io.core.Source;
import org.apache.pulsar.io.core.SourceContext;

import com.gottaeat.domain.order.FoodOrder;

public class CustomerSimulatorSource implements Source<FoodOrder> {

	private DataGenerator<FoodOrder> generator = new FoodOrderGenerator();
	
	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void open(Map<String, Object> map, SourceContext ctx) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public Record<FoodOrder> read() throws Exception {
		Thread.sleep(500);
		return new CustomerRecord<FoodOrder>(generator.generate());
	}
	
	static private class CustomerRecord<V> implements Record<FoodOrder> {

		private FoodOrder foodOrder;
		private Long eventTime = System.currentTimeMillis();
		
		public CustomerRecord(FoodOrder food) {
			this.foodOrder = food;
		}
		
		@Override
		public FoodOrder getValue() {
			return foodOrder;
		}
		
		public Optional<Long> getEventTime() {
			return Optional.of(eventTime);
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		
		SourceConfig sourceConfig = 
			SourceConfig.builder()
				.className(CustomerSimulatorSource.class.getName())
				.name("mobile-app-simulator")
				.topicName("persistent://orders/inbound/food-orders")
				.schemaType("avro")
				.build();
	    
	    // Assumes you started docker container with --volume=${HOME}/exchange:/pulsar/manning/dropbox 
	    String credentials_path = System.getProperty("user.home") + File.separator 
	    		+ "exchange" + File.separator;

		LocalRunner localRunner = 
	    	LocalRunner.builder()
	    		.brokerServiceUrl("pulsar+ssl://localhost:6651")
	    		.clientAuthPlugin("org.apache.pulsar.client.impl.auth.AuthenticationTls")
	    		.clientAuthParams("tlsCertFile:" + credentials_path + "admin.cert.pem,tlsKeyFile:"
	    				+ credentials_path + "admin-pk8.pem")
	    		.tlsTrustCertFilePath(credentials_path + "ca.cert.pem")
	    		.useTls(true)
	    		.sourceConfig(sourceConfig)
	    		.build();
	    
	    localRunner.start(false);
	    
	    Thread.sleep(30 * 1000);
	    localRunner.stop();
	    System.exit(0);
	}

}
