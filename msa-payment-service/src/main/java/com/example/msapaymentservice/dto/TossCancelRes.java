package com.example.msapaymentservice.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TossCancelRes {
	private String status;
	private UUID orderId;
	private OffsetDateTime requestedAt;
	private OffsetDateTime approvedAt;
}
