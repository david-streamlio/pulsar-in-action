package com.manning.retry;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;


public class RetryFunction implements Function<String, String> {

	public String apply(String s) {
		
		RetryConfig config = RetryConfig.custom()
				  .maxAttempts(2)
				  .waitDuration(Duration.ofMillis(1000))
				  .retryOnResult(response -> (response == null)) // Retry if result was null
				  .retryExceptions(TimeoutException.class)
				  .ignoreExceptions(RuntimeException.class) // Ignored and not retired
				  .build();
		
		// Decorate the invocation 
		CheckedFunction0<String> retryableFunction = Retry.decorateCheckedSupplier(
			RetryRegistry.of(config).retry("name"), 
			() -> {
				HttpGet request = new HttpGet("https://www.google.com/search?q=" + s);

		        try (CloseableHttpResponse response = 
		        		HttpClients.createDefault().execute(request)) {

		            HttpEntity entity = response.getEntity();
		            return (entity == null) ? null: EntityUtils.toString(entity);
		           
		        }

			}
		);
		
		Try<String> result = Try.of(retryableFunction);
		
		return result.getOrNull();
	}

}
