package com.manning.retry;

import java.time.Duration;

import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;

public class HttpGetFunction implements Function<String, String> {

	private boolean initalized = false;
	private RetryConfig config;
	private RetryRegistry registry;
	private CloseableHttpClient httpClient;
	private String url;
	
	@Override
	public String process(String s, Context ctx) throws Exception {
		if (!initalized) {
			init();
		}
		
		CheckedFunction0<String> retryableSupplier = 
			Retry.decorateCheckedSupplier(registry.retry("id"), getFunction(s));
		
		Try<String> result = Try.of(retryableSupplier);
		
		return result.get();
	}

	private final CheckedFunction0<String> getFunction(String s) {
		CheckedFunction0<String> fn = () -> {
			
			String result = null;
			HttpGet request = new HttpGet(url + s);

	        try (CloseableHttpResponse response = httpClient.execute(request)) {

	            // Get HttpResponse Status
	            System.out.println(response.getStatusLine().toString());

	            HttpEntity entity = response.getEntity();
	            Header headers = entity.getContentType();
	            System.out.println(headers);

	            if (entity != null) {
	                // return it as a String
	                result = EntityUtils.toString(entity);
	            }

	        }
			
			return result;
        };
		return fn;
	}
	
	private final void init() {
		config = RetryConfig.custom()
				  .maxAttempts(2)
				  .waitDuration(Duration.ofMillis(1000))
				  .retryOnResult(response -> response != null)
				  .build();
		
		registry = RetryRegistry.of(config);
		httpClient = HttpClients.createDefault();
		url = "https://www.google.com/search?q=";
	}
}
