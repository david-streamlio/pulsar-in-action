package com.gottaeat.services.payment.paypal;

import org.junit.Test;

import com.gottaeat.domain.payment.PayPal;

public class PaypalAuthorizationServiceTest {

	private PaypalAuthorizationService service = new PaypalAuthorizationService();
	
	@Test
	public final void concurrentTest() throws Exception {
		PayPal pay = new PayPal();
		long start = System.currentTimeMillis();
		for (int idx = 0; idx < 5; idx++) {
			service.process(pay , null);
		}
		long end = System.currentTimeMillis();
		
		System.out.println("Five calls took " + (end-start) + " milliseconds");
	}
}
