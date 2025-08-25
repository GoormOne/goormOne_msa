package com.example.storeservice.exception;

import com.example.common.exception.BusinessException;
import com.example.common.exception.CommonCode;

public class ReservedNotEnoughException extends BusinessException {
	public ReservedNotEnoughException() {
		super(CommonCode.RESERVED_NOT_ENOUGH);
	}
}
