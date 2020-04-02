package com.gottaeat.services.ordervalidation;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.pulsar.client.impl.schema.AvroSchema;
import org.apache.pulsar.common.functions.ConsumerConfig;
import org.apache.pulsar.common.functions.FunctionConfig;
import org.apache.pulsar.functions.LocalRunner;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;

import com.gottaeat.domain.common.Address;
import com.gottaeat.domain.order.FoodOrder;
import com.gottaeat.domain.order.FoodOrderMeta;
import com.gottaeat.domain.payment.Payment;

/**
 * Use a Splitter to break out the composite message into a series of 
 * individual messages, each containing data related to one item.
 * 
 * @see https://www.enterpriseintegrationpatterns.com/patterns/messaging/Sequencer.html
 *
 */
public class OrderValidationService implements Function<FoodOrder, Void> {
	
	private boolean initalized;
	private String geoEncoderTopic;
	private String paymentTopic;
	private String resturantTopic;
	private String orderTopic;

	@Override
	  public Void process(FoodOrder order, Context ctx) throws Exception {
		if (!initalized) {
	  	  init(ctx);
		}
			
		ctx.newOutputMessage(geoEncoderTopic, AvroSchema.of(Address.class))
		  .property("order-id", order.getMeta().getOrderId() + "")
	       .value(order.getDeliveryLocation()).sendAsync();
		
		ctx.newOutputMessage(paymentTopic, AvroSchema.of(Payment.class))
		  .property("order-id", order.getMeta().getOrderId() + "")
		  .value(order.getPayment()).sendAsync();

		ctx.newOutputMessage(orderTopic, AvroSchema.of(FoodOrderMeta.class))
		  .property("order-id", order.getMeta().getOrderId() + "")
		  .value(order.getMeta()).sendAsync();

	     ctx.newOutputMessage(resturantTopic, AvroSchema.of(FoodOrder.class))
		  .property("order-id", order.getMeta().getOrderId() + "")
		  .value(order).sendAsync();

		return null;
	  }
	
	private void init(Context ctx) {
	  geoEncoderTopic = ctx.getUserConfigValue("geo-topic").toString();
	  paymentTopic = ctx.getUserConfigValue("payment-topic").toString();
	  resturantTopic = ctx.getUserConfigValue("restaurant-topic").toString();
	  orderTopic = ctx.getUserConfigValue("aggregator-topic").toString();
	  initalized = true;
	}

	public static void main(String[] args) throws Exception {
		
		Map<String, ConsumerConfig> inputSpecs = new HashMap<String, ConsumerConfig> ();
	    inputSpecs.put("persistent://orders/inbound/food-orders", 
	    		ConsumerConfig.builder().schemaType("avro").build());
		
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
