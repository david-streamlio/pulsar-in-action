package com.manning.retry;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.Retry;
import io.vavr.control.Try;

public class FortifiedHttpGetFunction implements Function<String, String> {
	
	private HttpGetFunction fn = new HttpGetFunction();

	public String process(String s, Context ctx) throws Exception {
		
		// Create a CircuitBreaker with default configuration
		CircuitBreaker circuitBreaker = CircuitBreaker
		  .ofDefaults("backendService");

		// Create a Retry with default configuration
		// 3 retry attempts and a fixed time interval between retries of 500ms
		Retry retry = Retry
		  .ofDefaults("backendService");

		// Create a Bulkhead with default configuration
		Bulkhead bulkhead = Bulkhead
		  .ofDefaults("backendService");

		Supplier<String> supplier = SupplierUtils.wrap(() -> fn.process(s, ctx));

		// Decorate the invocation of the function 
		// with a Bulkhead, CircuitBreaker and Retry
		Supplier<String> decoratedSupplier = Decorators.ofSupplier(supplier)
		  .withCircuitBreaker(circuitBreaker)
		  .withBulkhead(bulkhead)
		  .withRetry(retry)  
		  .decorate();

		// Execute the decorated supplier and recover from any exception
		String result = Try.ofSupplier(decoratedSupplier)
				.onFailure(t -> ctx.getCurrentRecord().fail())
				.onSuccess(a -> ctx.getCurrentRecord().ack())
				.andFinally(() -> { ctx.recordMetric("Records Processed", 1L); })
		        .get();

		return result;
	}
	
	public static final class SupplierUtils {
	    private SupplierUtils() {
	    }

	    public static <T> Supplier<T> wrap(Callable<T> callable) {
	        return () -> {
	            try {
	                return callable.call();
	            }
	            catch (RuntimeException e) {
	                throw e;
	            }
	            catch (Exception e) {
	                throw new RuntimeException(e);
	            }
	        };
	    }
	}
}
