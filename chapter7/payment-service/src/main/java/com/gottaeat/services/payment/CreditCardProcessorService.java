package com.gottaeat.services.payment;

import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;

import com.gottaeat.domain.payment.*;

public class CreditCardProcessorService implements Function<Payment, AuthorizedPayment> {

	@Override
	public AuthorizedPayment process(Payment payment, Context ctx) throws Exception {
		
		AuthorizedPayment authorization = new AuthorizedPayment();
		
		authorization.setPayment(payment);
		authorization.setApprovalCode("123456789");
		authorization.setStatus(PaymentStatus.AUTHORIZED);
		
		return authorization;
	}

}
