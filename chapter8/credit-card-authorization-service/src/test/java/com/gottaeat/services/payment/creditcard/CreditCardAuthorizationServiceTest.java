package com.gottaeat.services.payment.creditcard;

import org.junit.Before;
import org.junit.Test;

import com.gottaeat.domain.payment.CreditCard;
import com.gottaeat.services.payment.creditcard.CreditCardAuthorizationService;

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
		service.process(pay, null);
	}
}
