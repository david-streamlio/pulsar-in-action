package com.gottaeat.services.ordervalidation;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.pulsar.common.functions.ConsumerConfig;
import org.apache.pulsar.common.functions.FunctionConfig;
import org.apache.pulsar.functions.LocalRunner;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;

import com.gottaeat.domain.order.FoodOrder;

public class OrderValidationService implements Function<FoodOrder, FoodOrder> {

	@Override
	public FoodOrder process(FoodOrder order, Context ctx) throws Exception {
		System.out.println(order.toString());
		return order;
	}
	
	public static void main(String[] args) throws Exception {
		
		Map<String, ConsumerConfig> inputSpecs = new HashMap<String, ConsumerConfig>  ();
	    ConsumerConfig conf = new ConsumerConfig();
	    conf.setSchemaType("avro");
	    inputSpecs.put("persistent://orders/inbound/food-orders", conf);
		
	    FunctionConfig functionConfig = 
	    	FunctionConfig.builder()
	    	.className(OrderValidationService.class.getName())
	    	.inputs(Collections.singleton("persistent://orders/inbound/food-orders"))
	    	.inputSpecs(inputSpecs)
	    	.name("order-validation")
	    	.output("persistent://orders/inbound/valid-food-orders")
	    	.outputSchemaType("avro")
	    	.runtime(FunctionConfig.Runtime.JAVA)
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
	    		.functionConfig(functionConfig)
	    		.build();
	    
	    localRunner.start(false);
	    Thread.sleep(30 * 1000);
	    localRunner.stop();
	    System.exit(0);
	}

}
