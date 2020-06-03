package com.manning.circuitbreaker;

public class UnsuccessfulCallException extends Exception {

	private int code;
	
	public UnsuccessfulCallException(int c) {
		this.code = c;
	}
	
	public int getCode() {
		return code;
	}
	
}
