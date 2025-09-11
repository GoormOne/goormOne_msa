package com.example.msapaymentservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.msapaymentservice.log.MdcRequestFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	@Bean
	public MdcRequestFilter mdcRequestFilter() {
		return new MdcRequestFilter();
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, MdcRequestFilter mdc) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
					"/swagger-ui.html", "/swagger-ui/**",
					"/webjars/**",
					"/v3/api-docs", "/v3/api-docs/**", "/v3/api-docs/swagger-config"
				).permitAll()
				.requestMatchers("/payments/**", "/internal/**", "/tosspayment.html/**").permitAll()
				.requestMatchers("/actuator/health").permitAll()
				.anyRequest().authenticated()
			);
		http.addFilterBefore(mdc, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
}
