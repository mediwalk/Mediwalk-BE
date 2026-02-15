package com.example.mediwalk_be.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI mediwalkOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("MediWalk API")
						.description("폐의약품 수거 + 운동 리워드 앱 백엔드 API. 에러 시 모든 API는 ErrorResponse 형식으로 응답합니다.")
						.version("0.0.1"));
	}

	/** 에러 응답 스키마(ErrorResponse, FieldErrorDetail)를 OpenAPI components에 추가 */
	@Bean
	public OpenApiCustomizer errorResponseSchemaCustomizer() {
		return openApi -> {
			var schemas = openApi.getComponents().getSchemas();
			if (schemas == null) return;

			// FieldErrorDetail: validation 시 필드별 오류
			Schema<?> fieldErrorDetail = new ObjectSchema()
					.name("FieldErrorDetail")
					.addProperty("field", new StringSchema().description("필드명"))
					.addProperty("reason", new StringSchema().description("오류 사유"));
			schemas.put("FieldErrorDetail", fieldErrorDetail);

			// ErrorResponse: 공통 에러 body 형식
			Schema<?> errorsArray = new ArraySchema()
					.items(new Schema<>().$ref("#/components/schemas/FieldErrorDetail"))
					.description("필드별 검증 오류 (VALIDATION_ERROR 시에만 존재)");
			Schema<?> errorResponse = new ObjectSchema()
					.name("ErrorResponse")
					.description("에러 시 공통 응답 body. 4xx/5xx 모두 이 형식으로 반환됩니다.")
					.addProperty("code", new StringSchema()
							.description("에러 코드: NOT_FOUND, BAD_REQUEST, VALIDATION_ERROR, INTERNAL_ERROR"))
					.addProperty("message", new StringSchema().description("에러 메시지"))
					.addProperty("status", new IntegerSchema().description("HTTP 상태 코드"))
					.addProperty("errors", errorsArray);
			schemas.put("ErrorResponse", errorResponse);
		};
	}
}
