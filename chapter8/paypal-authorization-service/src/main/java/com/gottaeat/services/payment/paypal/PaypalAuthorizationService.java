package com.gottaeat.services.payment.paypal;

import java.io.IOException;
import java.util.function.Consumer;

import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.gottaeat.domain.payment.PayPal;

import io.vavr.CheckedFunction0;
import io.vavr.control.Try;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @see https://developer.paypal.com/docs/api/get-an-access-token-curl/
 */
public class PaypalAuthorizationService implements Function<PayPal, String> {

	private String clientId;
	private String secret;
	private String accessToken;
	
	@Override
	public String process(PayPal pay, Context ctx) throws Exception {

		return Try.of(getAuthorizationFunction(pay))
			.onFailure(UnauthorizedException.class, refreshToken())
			.recover(UnauthorizedException.class, 
					(exc) -> Try.of(getAuthorizationFunction(pay)).get())
			.getOrNull();

	}

	/**
	 * @see https://developer.paypal.com/docs/integration/direct/payments/authorize-and-capture-payments/#authorize-the-payment
	 * @param pay
	 * @return
	 */
	private CheckedFunction0<String> getAuthorizationFunction(PayPal pay) {
		CheckedFunction0<String> fn = () -> {
			OkHttpClient client = new OkHttpClient();
			MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
			RequestBody body = RequestBody.create(buildRequestBody(pay), mediaType);
			
			Request request = new Request.Builder()
				.url("https://api.sandbox.paypal.com/v1/payments/payment")
				.addHeader("Authorization", accessToken)
				.post(body)
				.build();

			try (Response response = client.newCall(request).execute()) {
				if (response.isSuccessful()) {
					return response.body().string();
				} else {
					// determine error type
					if (response.code() == 500) {
						throw new UnauthorizedException();
					}
				}
				return null;
			}

		};
		
		return fn;
	}
	
	private Consumer<UnauthorizedException> refreshToken() {
		Consumer<UnauthorizedException> a = (ex) -> {
			// Refresh to access token
			OkHttpClient client = new OkHttpClient();
			MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
			RequestBody body = RequestBody.create("", mediaType);

			Request request = new Request.Builder()
				.url("https://api.sandbox.paypal.com/v1/oauth2/token?grant_type=client_credentials")
				.addHeader("Accept-Language", "en_US")
				.addHeader("Authorization", Credentials.basic(clientId, secret))
				.post(body)
				.build();

			try (Response response = client.newCall(request).execute()) {
				if (response.isSuccessful()) {
				   parseToken(response.body().string());
				} 	
			} catch (IOException e) {
				e.printStackTrace();
			}	
		};
		return a ;
	}
	
	private void parseToken(String json) {
		JsonParser parser = new JsonParser();
		JsonElement jsonTree = parser.parse(json);
		JsonObject jsonObject = jsonTree.getAsJsonObject();
	    JsonElement token = jsonObject.get("access_token");
	    accessToken = token.getAsString();
	}
	
	private String buildRequestBody(PayPal pay) {
		StringBuilder sb = new StringBuilder();
			sb.append("{\"intent\": \"authorize\",");
			sb.append("\"payer\":\n" + 
					"  {\n" + 
					"    \"payment_method\": \"paypal\"\n" + 
					"  },");
			sb.append("\"transactions\": [{");
			sb.append("\"amount\": {");
			
			sb.append("}],}\n" + 
					"}");
		return sb.toString();
	}

}
