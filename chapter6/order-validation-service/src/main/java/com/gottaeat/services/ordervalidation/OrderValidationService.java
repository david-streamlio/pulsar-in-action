package com.gottaeat.services.ordervalidation;

import java.io.File;
import java.util.Collections;

import org.apache.pulsar.common.functions.FunctionConfig;
import org.apache.pulsar.functions.LocalRunner;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;

import com.gottaeat.domain.order.FoodOrder;

public class OrderValidationService implements Function<FoodOrder, Boolean> {

	@Override
	public Boolean process(FoodOrder order, Context ctx) throws Exception {
		System.out.println(order.toString());
		return Boolean.TRUE;
	}
	
	public static void main(String[] args) throws Exception {
		
	    FunctionConfig functionConfig = new FunctionConfig();
	    functionConfig.setName("validation");
	    functionConfig.setInputs(Collections.singleton("persistent://orders/inbound/food-orders"));
	    functionConfig.setClassName(OrderValidationService.class.getName());
	    functionConfig.setRuntime(FunctionConfig.Runtime.JAVA);
	    functionConfig.setOutput("persistent://orders/inbound/valid-food-orders");
	    
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
