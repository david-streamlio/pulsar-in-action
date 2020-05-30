package com.gottaeat.services.geoencoding.google;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;

import org.apache.pulsar.client.impl.schema.AvroSchema;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.gottaeat.domain.common.Address;
import com.gottaeat.domain.common.LatLon;

import io.github.resilience4j.cache.Cache;
import io.github.resilience4j.decorators.Decorators;
import io.vavr.CheckedFunction0;
import io.vavr.CheckedFunction1;
import io.vavr.control.Try;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GeoEncodingServiceWithCache implements Function<Address, Void> {
	private static List<String> TRANSIENT_ERRORS = Arrays.asList("UNKNOWN_ERROR");
	private static List<String> NON_TRANSIENT_ERRORS = Arrays.asList("OVER_DAILY_LIMIT", "OVER_QUERY_LIMIT", "REQUEST_DENIED");
	private boolean initalized = false;
	private Cache<String, String> cacheContext;

	@Override
	public Void process(Address addr, Context ctx) throws Exception {
		
		if (!initalized) {
			init(ctx);
		}
		
		CheckedFunction1<String, String> cachedFunction = Decorators
			    .ofCheckedSupplier(getFunction(addr))
			    .withCache(cacheContext)
			    .decorate();
		
		LatLon geo = getLocation(Try.of(() -> cachedFunction.apply(addr.toString())).get());

		if (geo != null) {
			addr.setGeo(geo);
			ctx.newOutputMessage(ctx.getOutputTopic(), AvroSchema.of(Address.class))
				.properties(ctx.getCurrentRecord().getProperties())
				.value(addr)
				.send();
		} else {
			// We made a valid call, but didn't get a valid geo back
		}
		
		return null;
	}

	private void init(Context ctx) {
		// Configure a cache (once)
		CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();
		cacheContext = Cache.of(cacheManager
		    .createCache("addressCache", new MutableConfiguration<>()));
		
		initalized = true;
	}
	
	private CheckedFunction0<String> getFunction(Address addr) {
		CheckedFunction0<String> fn = () -> { 
			OkHttpClient client = new OkHttpClient();
			StringBuilder sb = new StringBuilder()
				.append("https://maps.googleapis.com/maps/api/geocode/json?address=")
				.append(URLEncoder.encode(addr.getStreet().toString(), 
						StandardCharsets.UTF_8.toString())).append(",")
				.append(URLEncoder.encode(addr.getCity().toString(), 
						StandardCharsets.UTF_8.toString())).append(",")
				.append(URLEncoder.encode(addr.getState().toString(), 
						StandardCharsets.UTF_8.toString()))
				.append("&key=").append("SIGN-UP-FOR-KEY");

			Request request = new Request.Builder()
					.url(sb.toString())
					.build();

			try (Response response = client.newCall(request).execute()) {
				if (response.isSuccessful()) {
					return response.body().string();
				} else {
					String reason = getErrorStatus(response.body().string());
					/* React based on the code
					 *    Input errors: ZERO_RESULTS or INVALID_REQUEST
					 */
					if (NON_TRANSIENT_ERRORS.stream().anyMatch(s -> reason.contains(s))) {
						throw new NonTransientException();
					} else if (TRANSIENT_ERRORS.stream().anyMatch(s -> reason.contains(s))) {
						throw new TransientException();
					}
					return null;
				}
						
			}

		};
		
		return fn;
	}

	private LatLon getLocation(String json) {
		LatLon ll = null;
		try {
			JsonElement jsonTree = new JsonParser().parse(json);

			if (jsonTree.isJsonObject()) {
				JsonObject jsonObject = jsonTree.getAsJsonObject();
				JsonObject geometry = jsonObject.getAsJsonObject("geometry");
				JsonObject location = geometry.getAsJsonObject("location");
				Double lat = location.get("lat").getAsDouble();
				Double lon = location.get("lat").getAsDouble();

				ll = new LatLon();
				ll.setLatitude(lat);
				ll.setLongitude(lon);  
			}
		} catch (Throwable t) {
			// Trap all parsing errors
		}
		
		return ll;
	}
	
	private String getErrorStatus(String json) {
		JsonElement jsonTree = new JsonParser().parse(json);
		if (jsonTree.isJsonObject()) {
			JsonObject jsonObject = jsonTree.getAsJsonObject();
		    JsonElement status = jsonObject.get("status");
		    return status.getAsString();
		}
		return "UNKNOWN_ERROR";
	}

}
