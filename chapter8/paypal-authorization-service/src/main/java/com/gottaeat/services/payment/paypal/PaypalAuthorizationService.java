package com.gottaeat.services.payment.paypal;

import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;

import com.gottaeat.domain.payment.PayPal;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.decorators.Decorators;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PaypalAuthorizationService implements Function<PayPal, Void> {

	private boolean initalized = false;
	private BulkheadConfig config;
	private BulkheadRegistry registry;
	private Bulkhead bulkhead;
	
	@Override
	public Void process(PayPal pay, Context ctx) throws Exception {
		
		if (!initalized) {
			init(ctx);
		}
		
		CheckedFunction0<String> decoratedFunction = Decorators.ofCheckedSupplier(getFunction(pay))
				.withBulkhead(bulkhead)
				.decorate();
		
		String s = Try.of(decoratedFunction).getOrNull();
		ctx.getCurrentRecord().getMessage();
		
		return null;
	}

	private CheckedFunction0<String> getFunction(PayPal pay) {
		CheckedFunction0<String> fn = () -> {
			Thread.sleep(10000);
			return "";
//			OkHttpClient client = new OkHttpClient();
//			
//			StringBuilder sb = new StringBuilder();
//
//			MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
//			RequestBody body = RequestBody.create(sb.toString(), mediaType);
//			Request request = new Request.Builder()
//				.url("https://api.sandbox.paypal.com/v2/checkout/orders")
//				.post(body)
//				.addHeader("Authorization", "Bearer <Access-Token>")
//				.addHeader("content-type", "application/json")
//				.build();
//
//			try (Response response = client.newCall(request).execute()) {
//				if (!response.isSuccessful()) {
//					//
//				}
//				String token = null; // getToken(response.body().string());
//				return token;
//			}

		};
		
		return fn;
	}

	private void init(Context ctx) {
		config = BulkheadConfig.custom().maxConcurrentCalls(5).build();
		registry = BulkheadRegistry.of(config);
		bulkhead = registry.bulkhead("foo");
		initalized = true;
	}

}
