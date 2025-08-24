package com.example.storeservice.exception;

import com.example.common.exception.BusinessException;
import com.example.common.exception.CommonCode;

public class InvalidQuantityException extends BusinessException {
	public InvalidQuantityException() {
		super(CommonCode.INVENTORY_NOT_FOUND);
	}
}
