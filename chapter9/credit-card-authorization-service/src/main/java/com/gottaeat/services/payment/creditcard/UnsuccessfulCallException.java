package com.gottaeat.services.payment.creditcard;

public class UnsuccessfulCallException extends Exception {

	private int code;
	
	public UnsuccessfulCallException(int c) {
		this.code = c;
	}
	
	public int getCode() {
		return code;
	}
	
}
