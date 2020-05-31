package com.gottaeat.services.payment.creditcard;

import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Record;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

import com.gottaeat.domain.payment.CreditCard;
import com.gottaeat.services.payment.creditcard.CreditCardAuthorizationService;

public class CreditCardAuthorizationServiceTest {

	@Mock
	private Context mockContext;
	
	@Mock
	private Record mockRecord;
	
	private CreditCardAuthorizationService service;
	
	@Before
	public final void init() {
		MockitoAnnotations.initMocks(this);
		service = new CreditCardAuthorizationService();
		when(mockContext.getFunctionName()).thenReturn("test-function");
		when(mockContext.getCurrentRecord()).thenReturn(mockRecord);
	}
	
	@Test
	public final void simpleTest() throws Exception {
		CreditCard pay = new CreditCard();
		pay.setAccountNumber("4242424242424242");
		pay.setBillingZip("90210");
		pay.setCcv("123");
		pay.setExpMonth("08");
		pay.setExpYear("2020");
		
		service.process(pay, mockContext);
	}
}
