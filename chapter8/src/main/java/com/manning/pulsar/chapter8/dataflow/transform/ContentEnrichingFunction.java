package com.manning.pulsar.chapter8.dataflow.transform;

import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;

import com.gottaeat.domain.order.FoodOrder;

/**
 * 
 * see https://www.enterpriseintegrationpatterns.com/patterns/messaging/DataEnricher.html
 *
 */
public class ContentEnrichingFunction implements Function<FoodOrder, Void> {

	@Override
	public Void process(FoodOrder order, Context context) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
