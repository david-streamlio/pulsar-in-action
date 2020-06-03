package com.gottaeat.services.payment;

import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.TypedMessageBuilder;
import org.apache.pulsar.client.impl.schema.AvroSchema;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Record;
import org.junit.Before;
import org.junit.Test;

import com.gottaeat.domain.payment.ApplePay;
import com.gottaeat.domain.payment.CardType;
import com.gottaeat.domain.payment.CreditCard;
import com.gottaeat.domain.payment.ElectronicCheck;
import com.gottaeat.domain.payment.PayPal;
import com.gottaeat.domain.payment.Payment;
import com.gottaeat.domain.payment.PaymentAmount;
import com.gottaeat.domain.payment.PaymentMethod;

@SuppressWarnings("rawtypes")
public class PaymentServiceTests {

	private PaymentService service;
	private Context mockContext;
	private TypedMessageBuilder mockedMessageBuilder;
	private CompletableFuture mockedFuture;
	private Payment payment;
	
	@Before
	public final void init() throws PulsarClientException {
		service = new PaymentService();
		payment = new Payment();
		
		PaymentAmount amount = new PaymentAmount();
		amount.setFoodTotal(20.00f);
		amount.setTax(5.00f);
		amount.setTotal(25.00f);
		payment.setAmount(amount);
		
		// All the mocks
		mockContext = mock(Context.class);
		mockedMessageBuilder = mock(TypedMessageBuilder.class);
		mockedFuture = mock(CompletableFuture.class);
		
		when(mockContext.newOutputMessage(anyString(), anyObject())).thenReturn(mockedMessageBuilder);
		when(mockedMessageBuilder.properties(anyObject())).thenReturn(mockedMessageBuilder);
		when(mockedMessageBuilder.value(anyObject())).thenReturn(mockedMessageBuilder);
		when(mockedMessageBuilder.sendAsync()).thenReturn(mockedFuture);
		Record mockRecord = mock(Record.class);
		when(mockContext.getCurrentRecord()).thenReturn(mockRecord);
		
		Optional<Object> appleTopic = Optional.of("apple-pay");
		when(mockContext.getUserConfigValue("apple-pay-topic")).thenReturn(appleTopic);
		
		Optional<Object> ccTopic = Optional.of("credit-card");
		when(mockContext.getUserConfigValue("credit-card-topic")).thenReturn(ccTopic);
		
		Optional<Object> eCheckTopic = Optional.of("e-Check");
		when(mockContext.getUserConfigValue("e-check-topic")).thenReturn(eCheckTopic);
		
		Optional<Object> paypalTopic = Optional.of("paypal");
		when(mockContext.getUserConfigValue("paypal-topic")).thenReturn(paypalTopic);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public final void ApplePayTest() throws Exception {
		
		PaymentMethod method = new PaymentMethod();
		ApplePay apple = new ApplePay();
		apple.setAccountNumber("123456789");
		method.setType(apple);
		payment.setMethodOfPayment(method);
		
		service.process(payment, mockContext);
		verify(mockContext, times(1)).newOutputMessage(eq("apple-pay"), anyObject());
		verify(mockedMessageBuilder).value(apple);
		verify(mockedMessageBuilder, times(1)).sendAsync();
	
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public final void CreditCardTest() throws Exception {
		
		PaymentMethod method = new PaymentMethod();
		CreditCard cc = new CreditCard();
		cc.setAccountNumber("4000123456789012");
		cc.setBillingZip("90210");
		cc.setCardType(CardType.AMEX);
		cc.setCcv("007");
		method.setType(cc);
		payment.setMethodOfPayment(method);
		
		service.process(payment, mockContext);
		verify(mockContext, times(1)).newOutputMessage(eq("credit-card"), anyObject());
		verify(mockedMessageBuilder).value(cc);
		verify(mockedMessageBuilder, times(1)).sendAsync();
	
	}

	@SuppressWarnings("unchecked")
	@Test
	public final void eCheckTest() throws Exception {
		
		PaymentMethod method = new PaymentMethod();
		ElectronicCheck check = new ElectronicCheck();
		check.setAccountNumber("35293560");
		check.setRoutingNumber("1100234509");
		method.setType(check);
		payment.setMethodOfPayment(method);
		
		service.process(payment, mockContext);
		verify(mockContext, times(1)).newOutputMessage(eq("e-Check"), anyObject());
		verify(mockedMessageBuilder).value(check);
		verify(mockedMessageBuilder, times(1)).sendAsync();
	
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public final void PayPalTest() throws Exception {
		
		PaymentMethod method = new PaymentMethod();
		PayPal pp = new PayPal();
		pp.setAccountNumber("BR549");
		method.setType(pp);
		payment.setMethodOfPayment(method);
		
		service.process(payment, mockContext);
		verify(mockContext, times(1)).newOutputMessage(eq("paypal"), anyObject());
		verify(mockedMessageBuilder).value(pp);
		verify(mockedMessageBuilder, times(1)).sendAsync();
	
	}
}
