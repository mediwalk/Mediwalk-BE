package com.example.mediwalk_be.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@EnableConfigurationProperties(FirebaseProperties.class)
@RequiredArgsConstructor
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true")
public class FirebaseAdminConfig {

	private final FirebaseProperties firebaseProperties;
	private final ResourceLoader resourceLoader;

	@Bean
	public FirebaseApp firebaseApp() throws IOException {
		if (!FirebaseApp.getApps().isEmpty()) {
			return FirebaseApp.getInstance();
		}
		GoogleCredentials credentials = loadCredentials();
		FirebaseOptions.Builder builder = FirebaseOptions.builder().setCredentials(credentials);
		if (StringUtils.hasText(firebaseProperties.getProjectId())) {
			builder.setProjectId(firebaseProperties.getProjectId().trim());
		}
		return FirebaseApp.initializeApp(builder.build());
	}

	private GoogleCredentials loadCredentials() throws IOException {
		if (StringUtils.hasText(firebaseProperties.getCredentialsResource())) {
			Resource resource = resourceLoader.getResource(firebaseProperties.getCredentialsResource().trim());
			try (InputStream in = resource.getInputStream()) {
				return GoogleCredentials.fromStream(in);
			}
		}
		return GoogleCredentials.getApplicationDefault();
	}
}
