package com.manning.circuitbreaker;

import org.junit.Before;
import org.junit.Test;

import com.gottaeat.domain.payment.AuthorizedPayment;
import com.gottaeat.domain.payment.CreditCard;

public class CreditCardAuthorizationServiceTest {

	private CreditCardAuthorizationService service;
	
	@Before
	public final void init() {
		service = new CreditCardAuthorizationService();
	}
	
	@Test
	public final void simpleTest() throws Exception {
		CreditCard pay = new CreditCard();
		pay.setAccountNumber("4242424242424242");
		pay.setBillingZip("90210");
		pay.setCcv("123");
		pay.setExpMonth("08");
		pay.setExpYear("2020");
		AuthorizedPayment auth = service.process(pay, null);
	}
}
