package com.example.storeservice.exception;

import com.example.common.exception.BusinessException;
import com.example.common.exception.CommonCode;

public class InvalidQuantityException extends BusinessException {
	public InvalidQuantityException(String detail) {
		super(CommonCode.BAD_REQUEST, detail);
	}
}
