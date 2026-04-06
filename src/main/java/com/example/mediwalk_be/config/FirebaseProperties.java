package com.example.mediwalk_be.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "firebase")
public class FirebaseProperties {

	private boolean enabled = false;

	private String projectId;

	private String credentialsResource;

	/**
	 * 서비스 계정 JSON 절대 경로 (로컬 전용). {@code application-local.yaml} 권장.
	 * 예: /Users/me/keys/firebase-adminsdk.json
	 */
	private String credentialsPath;
}
