package com.manning.pulsar.chapter5.source;

import java.io.Serializable;
import java.util.UUID;

public class ScoringEvent implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private UUID playerId;
	private long time;
	private int adjustment;
	
	public ScoringEvent() {
		
	}
	
	public ScoringEvent(UUID id, long t, int a) {
		this.playerId = id;
		this.time = t;
		this.adjustment = a;
	}
	
	public UUID getPlayerId() {
		return playerId;
	}
	
	public void setPlayerId(UUID playerId) {
		this.playerId = playerId;
	}
	
	public long getTime() {
		return time;
	}
	
	public void setTime(long time) {
		this.time = time;
	}
	
	public int getAdjustment() {
		return adjustment;
	}
	
	public void setAdjustment(int adjustment) {
		this.adjustment = adjustment;
	}
}
