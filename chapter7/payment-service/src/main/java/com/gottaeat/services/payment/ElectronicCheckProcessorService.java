package com.gottaeat.services.payment;

import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;

import com.gottaeat.domain.payment.*;

public class ElectronicCheckProcessorService implements Function<Payment, AuthorizedPayment> {

	@Override
	public AuthorizedPayment process(Payment payment, Context ctx) throws Exception {
		
		// Call web service and await response. Populate AuthorizedPayment from response
		AuthorizedPayment authorization = new AuthorizedPayment();
		
		authorization.setPayment(payment);
		authorization.setApprovalCode("123456789");
		authorization.setStatus(PaymentStatus.AUTHORIZED);
		
		return authorization;
	}

}
