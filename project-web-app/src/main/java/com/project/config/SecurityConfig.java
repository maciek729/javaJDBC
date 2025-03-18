package com.project.config;

import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
public class SecurityConfig {
	@Value("${rest.base.url}")
	private String restBaseUrl;
	
	@Value("${rest.user.name}")
	private String restUserName;
	
	@Value("${rest.user.password}")
	private String restUserPassword;
	
	@Bean
	public RestClient customRestClient() {
		return RestClient.builder().baseUrl(restBaseUrl)
				.defaultHeader(HttpHeaders.AUTHORIZATION,
						getBasicAuthenticationHeader(restUserName, restUserPassword))
				.build();
	}
	
	private String getBasicAuthenticationHeader(String username, String password) {
		return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
	}
}
