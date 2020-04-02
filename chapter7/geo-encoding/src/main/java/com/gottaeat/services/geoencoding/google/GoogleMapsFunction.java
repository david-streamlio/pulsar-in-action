package com.gottaeat.services.geoencoding.google;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.pulsar.client.impl.schema.AvroSchema;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.Geometry;
import com.google.maps.model.LocationType;
import com.gottaeat.domain.common.Address;
import com.gottaeat.domain.common.LatLon;

public class GoogleMapsFunction implements Function<Address, Void> {

	boolean initalized = false;
	String apiKey;
	GeoApiContext geoContext;
	String failureNotificationTopic;
	
	@Override
	public Void process(Address addr, Context context) throws Exception {
		if (!initalized) {
			init(context);
		}
		
		Address result = new Address();
		result.setCity(addr.getCity());
		result.setState(addr.getState());
		result.setStreet(addr.getStreet());
		result.setZip(addr.getZip());
		
		try {
			GeocodingResult[] results = 
				GeocodingApi.geocode(geoContext, formatAddress(addr)).await();

			Geometry geo = getMostPrecise(results);
			
			if (geo != null) {
				LatLon ll = new LatLon();
				ll.setLatitude(geo.location.lat);
				ll.setLongitude(geo.location.lng);
				result.setGeo(ll);
			}
			
			context.newOutputMessage(context.getOutputTopic(), AvroSchema.of(Address.class))
				.value(result)
				.properties(context.getCurrentRecord().getProperties())
				.send();
			
		} catch (InterruptedException | IOException | ApiException ex) {
			context.getCurrentRecord().fail();
			context.getLogger().error(ex.getMessage());
			context.newOutputMessage(failureNotificationTopic, AvroSchema.of(Address.class)).send();
		}
		
		return null;
	}

	private void init(Context context) {
		apiKey = context.getUserConfigValue("apiKey").toString();
		geoContext = new GeoApiContext.Builder()
			    .apiKey(apiKey)
			    .maxRetries(3)
			    .retryTimeout(3000, TimeUnit.MILLISECONDS)
			    .build();
		failureNotificationTopic = context.getUserConfigValue("failure-notification-topic").toString();
		initalized = true;
	}

	private String formatAddress(Address addr) {
		return new StringBuilder()
				.append(addr.getStreet())
				.append(" ")
				.append(addr.getCity())
				.append(" ")
				.append(addr.getState())
				.append(" ")
				.append(addr.getZip())
				.toString();
	}
	
	/**
	 * We will base this on the LocationType. 
	 *  ROOFTOP > RANGE_INTERPOLATED > GEOMETRIC_CENTER > APPROXIMATE > UNKNOWN
	 * 
	 * @see https://www.javadoc.io/static/com.google.maps/google-maps-services/0.11.0/com/google/maps/model/LocationType.html
	 * @param results
	 * @return
	 */
	private Geometry getMostPrecise(GeocodingResult[] results) {
		if (results == null || results.length < 1)
			return null;
		
		Geometry best = results[0].geometry;
		for (int idx = 1; idx < results.length; idx++) {
			switch (best.locationType) {
			case UNKNOWN : 
				best = results[idx].geometry;
				break;

			case APPROXIMATE: 
				if (results[idx].geometry.locationType != LocationType.UNKNOWN) {
					best = results[idx].geometry;
					break;
				}
			case GEOMETRIC_CENTER:
				if (results[idx].geometry.locationType == LocationType.RANGE_INTERPOLATED ||
					results[idx].geometry.locationType == LocationType.ROOFTOP) {
					best = results[idx].geometry;
					break;
				}
			case RANGE_INTERPOLATED: 	
				if (results[idx].geometry.locationType == LocationType.ROOFTOP) {
					best = results[idx].geometry;
					break;
				}
			case ROOFTOP: break;

			}
		}
		return null;
	}
}
