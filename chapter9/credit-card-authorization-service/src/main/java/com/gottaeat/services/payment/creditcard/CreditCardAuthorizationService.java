package com.gottaeat.services.payment.creditcard;

import java.io.IOException;
import java.time.Duration;

import org.apache.pulsar.client.impl.schema.AvroSchema;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.gottaeat.domain.payment.AuthorizedPayment;
import com.gottaeat.domain.payment.CreditCard;
import com.gottaeat.domain.payment.Payment;
import com.gottaeat.domain.payment.PaymentMethod;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreditCardAuthorizationService implements Function<CreditCard, Void> {
	
	private boolean initalized = false;
	private CircuitBreakerConfig config;
	private CircuitBreakerRegistry registry;
	private CircuitBreaker circuitBreaker;
	
	@Override
	public Void process(CreditCard card, Context ctx) throws Exception {
		
		if (!initalized) {
			init(ctx);
		}
		
		CheckedFunction0<String> decoratedFunction = Decorators.ofCheckedSupplier(getFunction(card))
				.withCircuitBreaker(circuitBreaker)
				.decorate();
			
		String authorizationCode = getToken(Try.of(decoratedFunction).getOrNull());
		
		if (authorizationCode == null) {
			ctx.getCurrentRecord().fail();
		} else {
			ctx.newOutputMessage(ctx.getOutputTopic(), AvroSchema.of(AuthorizedPayment.class))
				.properties(ctx.getCurrentRecord().getProperties())
				.value(authorize(card, authorizationCode))
				.send();
		}
		return null;
	}
	
	private CheckedFunction0<String> getFunction(CreditCard card) {
		CheckedFunction0<String> fn = () -> {
			OkHttpClient client = new OkHttpClient();
			
			StringBuilder sb = new StringBuilder()
					.append("number=").append(card.getAccountNumber())
					.append("&cvc=").append(card.getCcv())
					.append("&exp_month=").append(card.getExpMonth())
					.append("&exp_year=").append(card.getExpYear());

			MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
			RequestBody body = RequestBody.create(sb.toString(), mediaType);
			Request request = new Request.Builder()
				.url("https://noodlio-pay.p.rapidapi.com/tokens/create")
				.post(body)
				.addHeader("x-rapidapi-host", "noodlio-pay.p.rapidapi.com")
				.addHeader("x-rapidapi-key", "SIGN-UP-FOR-KEY")
				.addHeader("content-type", "application/x-www-form-urlencoded")
				.build();

			try (Response response = client.newCall(request).execute()) {
				if (!response.isSuccessful()) {
					throw new UnsuccessfulCallException(response.code());
				}
				String token = getToken(response.body().string());
				return token;
			}

		};
		
		return fn;
	}
	
	private void init(Context ctx) {
		config = CircuitBreakerConfig.custom()
				  .failureRateThreshold(50)
				  .slowCallRateThreshold(50)
				  .waitDurationInOpenState(Duration.ofMillis(1000))
				  .slowCallDurationThreshold(Duration.ofSeconds(2))
				  .permittedNumberOfCallsInHalfOpenState(3)
				  .minimumNumberOfCalls(10)
				  .slidingWindowType(SlidingWindowType.TIME_BASED)
				  .slidingWindowSize(5)
				  .ignoreException(e -> e instanceof UnsuccessfulCallException && 
						  ((UnsuccessfulCallException)e).getCode() == 499 )
				  .recordExceptions(IOException.class, UnsuccessfulCallException.class)
				  .build();
		
		registry = CircuitBreakerRegistry.of(config);
		circuitBreaker = registry.circuitBreaker(ctx.getFunctionName());
		initalized = true;
	}

	private String getToken(String json) {
		JsonElement jsonTree = new JsonParser().parse(json);
		
		if (jsonTree.isJsonObject()) {
		    JsonObject jsonObject = jsonTree.getAsJsonObject();
		    JsonElement token = jsonObject.get("id");
		    return token.getAsString();
		}
		
		return null;
	}
	
	private AuthorizedPayment authorize(CreditCard card, String token) {
		AuthorizedPayment auth = new AuthorizedPayment();
		Payment payment = new Payment();
		PaymentMethod type = new PaymentMethod();
		type.setType(card);
		payment.setMethodOfPayment(type);
		auth.setPayment(payment);
		auth.setApprovalCode(token);
		return auth;
	}

}
