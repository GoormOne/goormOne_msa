package com.example.storeservice.exception;

import com.example.common.exception.BusinessException;
import com.example.common.exception.CommonCode;

public class ReservedNoEnoughException extends BusinessException {
	public ReservedNoEnoughException() {
		super(CommonCode.RESERVED_NOT_ENOUGH);
	}
}
