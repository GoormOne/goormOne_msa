package com.example.msaorderservice.service;

import java.util.UUID;

import com.example.msaorderservice.dto.CartItemAddReq;
import com.example.msaorderservice.entity.CartEntity;
import com.example.msaorderservice.entity.CartItemEntity;

public interface CartService {
	CartItemEntity addItem(CartItemAddReq req);
}
