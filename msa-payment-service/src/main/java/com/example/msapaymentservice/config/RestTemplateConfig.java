package com.example.msapaymentservice.config;

import java.time.Duration;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.web.client.RestTemplateBuilder;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		var connManager = PoolingHttpClientConnectionManagerBuilder.create().build();
		connManager.setMaxTotal(200);
		connManager.setDefaultMaxPerRoute(50);

		var requestConfig = RequestConfig.custom()
			.setConnectTimeout(Timeout.ofMilliseconds(500))
			.setResponseTimeout(Timeout.ofMilliseconds(3000))
			.setConnectionRequestTimeout(Timeout.ofMilliseconds(1000))
			.build();

		CloseableHttpClient httpClient = HttpClients.custom()
			.setConnectionManager(connManager)
			.setDefaultRequestConfig(requestConfig)
			.evictExpiredConnections()
			.build();

		var factory = new HttpComponentsClientHttpRequestFactory(httpClient);

		// ✅ builder 타임아웃 호출( setConnectTimeout / setReadTimeout ) 금지
		return new RestTemplate(factory);
	}

	@Bean(name = "externalRestTemplate")
	public RestTemplate externalRestTemplate() {
		var connManager = PoolingHttpClientConnectionManagerBuilder.create().build();
		connManager.setMaxTotal(200);
		connManager.setDefaultMaxPerRoute(50);

		var requestConfig = RequestConfig.custom()
			.setConnectTimeout(Timeout.ofMilliseconds(500))
			.setResponseTimeout(Timeout.ofMilliseconds(3000))
			.setConnectionRequestTimeout(Timeout.ofMilliseconds(1000))
			.build();

		CloseableHttpClient httpClient = HttpClients.custom()
			.setConnectionManager(connManager)
			.setDefaultRequestConfig(requestConfig)
			.evictExpiredConnections()
			.build();

		var factory = new HttpComponentsClientHttpRequestFactory(httpClient);


		return new RestTemplate(factory);
	}
}
