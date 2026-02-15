package com.example.mediwalk_be.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI mediwalkOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("MediWalk API")
						.description("폐의약품 수거 + 운동 리워드 앱 백엔드 API")
						.version("0.0.1"));
	}
}
