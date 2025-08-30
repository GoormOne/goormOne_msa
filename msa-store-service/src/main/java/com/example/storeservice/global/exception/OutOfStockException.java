package com.example.storeservice.global.exception;

import com.example.common.exception.BusinessException;
import com.example.common.exception.CommonCode;

public class OutOfStockException extends BusinessException {
	public OutOfStockException() {
		super(CommonCode.OUT_OF_STOCK);
	}
}
