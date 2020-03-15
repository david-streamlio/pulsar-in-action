/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package com.gottaeat.domain.order;

@org.apache.avro.specific.AvroGenerated
public interface OrderProtocol {
  public static final org.apache.avro.Protocol PROTOCOL = org.apache.avro.Protocol.parse("{\"protocol\":\"OrderProtocol\",\"namespace\":\"com.gottaeat.domain.order\",\"types\":[{\"type\":\"record\",\"name\":\"Address\",\"namespace\":\"com.gottaeat.domain.common\",\"fields\":[{\"name\":\"street\",\"type\":\"string\"},{\"name\":\"city\",\"type\":\"string\"},{\"name\":\"state\",\"type\":\"string\"},{\"name\":\"zip\",\"type\":\"string\"}]},{\"type\":\"enum\",\"name\":\"CardType\",\"namespace\":\"com.gottaeat.domain.payment\",\"symbols\":[\"MASTERCARD\",\"AMEX\",\"VISA\",\"DISCOVER\"]},{\"type\":\"record\",\"name\":\"CreditCard\",\"namespace\":\"com.gottaeat.domain.payment\",\"fields\":[{\"name\":\"card_type\",\"type\":\"CardType\"},{\"name\":\"account_number\",\"type\":\"string\"},{\"name\":\"billing_zip\",\"type\":\"string\"},{\"name\":\"ccv\",\"type\":\"string\"}]},{\"type\":\"record\",\"name\":\"MenuItem\",\"namespace\":\"com.gottaeat.domain.resturant\",\"fields\":[{\"name\":\"item_id\",\"type\":\"long\"},{\"name\":\"item_name\",\"type\":\"string\"},{\"name\":\"item_description\",\"type\":\"string\"},{\"name\":\"customizations\",\"type\":{\"type\":\"array\",\"items\":\"string\"},\"default\":[\"\"]},{\"name\":\"price\",\"type\":\"float\"}]},{\"type\":\"record\",\"name\":\"FoodOrder\",\"fields\":[{\"name\":\"order_id\",\"type\":\"long\"},{\"name\":\"customer_id\",\"type\":\"long\"},{\"name\":\"resturant_id\",\"type\":\"long\"},{\"name\":\"time_placed\",\"type\":\"string\"},{\"name\":\"order_status\",\"type\":{\"type\":\"enum\",\"name\":\"OrderStatus\",\"symbols\":[\"NEW\",\"ACCEPTED\",\"READY\",\"DISPATCHED\",\"DELIVERED\"]}},{\"name\":\"details\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"OrderDetail\",\"fields\":[{\"name\":\"quantity\",\"type\":\"int\"},{\"name\":\"total\",\"type\":\"float\"},{\"name\":\"food_item\",\"type\":\"com.gottaeat.domain.resturant.MenuItem\"}]}}},{\"name\":\"delivery_location\",\"type\":\"com.gottaeat.domain.common.Address\"},{\"name\":\"payment_method\",\"type\":\"com.gottaeat.domain.payment.CreditCard\"},{\"name\":\"total\",\"type\":\"float\",\"default\":0.0}]}],\"messages\":{}}");

  @SuppressWarnings("all")
  public interface Callback extends OrderProtocol {
    public static final org.apache.avro.Protocol PROTOCOL = com.gottaeat.domain.order.OrderProtocol.PROTOCOL;
  }
}