package com.manning.pulsar.chapter5.types;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ScoringEvent implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long playerId;
	private LocalDateTime ts;
	private Integer adjustment;

	public ScoringEvent(Long playerId, LocalDateTime ts, Integer adj) {
		this.playerId = playerId;
		this.ts = ts;
		this.adjustment = adj;  
	}

	public Long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(Long playerId) {
		this.playerId = playerId;
	}

	public LocalDateTime getTs() {
		return ts;
	}

	public void setTs(LocalDateTime ts) {
		this.ts = ts;
	}

	public Integer getAdjustment() {
		return adjustment;
	}

	public void setAdjustment(Integer adjustment) {
		this.adjustment = adjustment;
	}

}
