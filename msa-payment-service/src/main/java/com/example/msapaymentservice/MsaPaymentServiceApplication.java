package com.example.msapaymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example")
public class MsaPaymentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsaPaymentServiceApplication.class, args);
	}

}
