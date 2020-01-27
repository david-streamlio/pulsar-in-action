package com.manning.pulsar.chapter6;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;

import com.gottaeat.customer.Address;
import com.gottaeat.customer.CardType;
import com.gottaeat.customer.CreditCard;
import com.gottaeat.customer.FoodItem;
import com.gottaeat.customer.Order;
import com.gottaeat.customer.OrderDetail;
import com.gottaeat.customer.OrderStatus;

public class FoodOrderGenerator implements DataGenerator<Order> {
	
	private Random rnd = new Random();
	
	
	public Order generate() {
		
		Pair<List<OrderDetail>, Float> orderDetails = getRandomOrderDetails(rnd.nextInt(3) + 1);
		
		return Order.newBuilder()
						.setCustomerId(rnd.nextLong())
						.setDeliveryLocation(getRandomAddress())
						.setTimePlaced(LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE))
						.setOrderId(rnd.nextLong())
						.setOrderStatus(OrderStatus.NEW)
						.setDetails(orderDetails.getLeft())
						.setPaymentMethod(getRandomCreditCard())
						.setResturantId(rnd.nextLong())
						.setTotal(orderDetails.getRight())
						.build();
	}
	
	private Address getRandomAddress() {
		return Address.newBuilder()
				.setCity("Chicago")
				.setState("IL")
				.setStreet("123 Main St")
				.setZip("66011")
				.build();
	}
	
	private CreditCard getRandomCreditCard() {
		return CreditCard.newBuilder()
				.setAccountNumber("1234 5678 9012 3456")
				.setBillingZip("66011")
				.setCardType(CardType.DISCOVER)
				.setCcv("789")
				.build();
	}

	private Pair<List<OrderDetail>, Float> getRandomOrderDetails(int i) {
		List<OrderDetail> details = new ArrayList<OrderDetail>();
		float total = 0.0f;
		for (int idx = 0; idx < i; idx++) {
			
			FoodItem foodItem = getRandomFoodItem();
			int quantity = rnd.nextInt(10)+1;
			
			OrderDetail od = OrderDetail.newBuilder()
								.setFoodItem(foodItem)
								.setQuantity(quantity)
								.setTotal(foodItem.getPrice() * quantity)
								.build();
			details.add(od);
			total = total + (foodItem.getPrice() * quantity);
		}
		return Pair.of(details, total);
	}
	
	private FoodItem getRandomFoodItem() {
		FoodItem foodItem = FoodItem.newBuilder()
								.setItemDescription("Delicious")
								.setItemId(rnd.nextLong())
								.setItemName("Burrito")
								.setPrice(4.99f)
								.build();
		return foodItem;
	}

}
