package com.gottaeat.services.payment;

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

import com.gottaeat.domain.payment.ApplePay;
import com.gottaeat.domain.payment.CreditCard;
import com.gottaeat.domain.payment.ElectronicCheck;
import com.gottaeat.domain.payment.PayPal;
import com.gottaeat.domain.payment.Payment;

/**
 * Content based routing example
 * 
 * @see https://www.enterpriseintegrationpatterns.com/patterns/messaging/ContentBasedRouter.html 
 *
 */
public class PaymentService implements Function<Payment, Void> {

	private boolean initalized = false;
	private String applePayTopic, creditCardTopic, echeckTopic, paypalTopic;
	
	public Void process(Payment pay, Context ctx) throws Exception {
		
		if (!initalized) {
			init(ctx);
		}
		
		Class paymentType = pay.getMethodOfPayment().getType().getClass();
		Object payment = pay.getMethodOfPayment().getType();
		
		if (paymentType == ApplePay.class) {
			ctx.newOutputMessage(applePayTopic, AvroSchema.of(ApplePay.class))
			    .properties(ctx.getCurrentRecord().getProperties())
			    .value((ApplePay) payment).sendAsync();			
		} else if (paymentType == CreditCard.class) {
			ctx.newOutputMessage(creditCardTopic, AvroSchema.of(CreditCard.class))
				.properties(ctx.getCurrentRecord().getProperties())
				.value((CreditCard) payment).sendAsync();
		} else if (paymentType == ElectronicCheck.class) {
			ctx.newOutputMessage(echeckTopic, AvroSchema.of(ElectronicCheck.class))
				.properties(ctx.getCurrentRecord().getProperties())
				.value((ElectronicCheck) payment).sendAsync();
		} else if (paymentType == PayPal.class) {
			ctx.newOutputMessage(paypalTopic, AvroSchema.of(PayPal.class))
			   .properties(ctx.getCurrentRecord().getProperties())
			   .value((PayPal) payment).sendAsync();
		} else {
			ctx.getCurrentRecord().fail();
		}
		
		return null;
	}
	
	private void init(Context ctx) {
		applePayTopic = (String) ctx.getUserConfigValue("apple-pay-topic").get();
		creditCardTopic = (String) ctx.getUserConfigValue("credit-card-topic").get();
		echeckTopic = (String) ctx.getUserConfigValue("e-check-topic").get();
		paypalTopic = (String) ctx.getUserConfigValue("paypal-topic").get();
		initalized = true;
	}
	
	public static void main(String[] args) throws Exception {
		
		Map<String, ConsumerConfig> inputSpecs = new HashMap<String, ConsumerConfig> ();
	    inputSpecs.put("persistent://orders/inbound/payments", 
	    		ConsumerConfig.builder().schemaType("avro").build());
		
	    FunctionConfig functionConfig = 
	    	FunctionConfig.builder()
	    	.className(PaymentService.class.getName())
	    	.inputs(Collections.singleton("persistent://orders/inbound/payments"))
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