package com.example.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
	private final CommonCode code;
	private final String detail;

	public BusinessException(CommonCode code) {
		super(code.getMessage());
		this.code = code;
		this.detail = null;
	}
	public BusinessException(CommonCode code, String detail) {
		super(code.getMessage());
		this.code = code;
		this.detail = detail;
	}
}
