package com.example.storeservice.global.exception;

import com.example.common.exception.BusinessException;
import com.example.common.exception.CommonCode;

public class InventoryNotFoundException extends BusinessException {
	public InventoryNotFoundException() {super(CommonCode.INVENTORY_NOT_FOUND);}
}
