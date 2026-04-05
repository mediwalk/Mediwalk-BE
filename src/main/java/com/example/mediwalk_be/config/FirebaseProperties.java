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

	/** 예: classpath:firebase-adminsdk.json — 비우면 GOOGLE_APPLICATION_CREDENTIALS / ADC */
	private String credentialsResource;
}
