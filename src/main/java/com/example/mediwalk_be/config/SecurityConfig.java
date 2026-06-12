package com.example.mediwalk_be.config;

import com.example.mediwalk_be.config.security.FirebaseAuthenticationFilter;
import com.example.mediwalk_be.config.security.RestAccessDeniedHandler;
import com.example.mediwalk_be.config.security.RestAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final FirebaseAuthenticationFilter firebaseAuthenticationFilter;
	private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
	private final RestAccessDeniedHandler restAccessDeniedHandler;

	/** Swagger UI / OpenAPI 문서는 시큐리티 필터를 거치지 않도록 제외 */
	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return web -> web.ignoring().requestMatchers(
				"/swagger-ui",
				"/swagger-ui/**",
				"/swagger-ui.html",
				"/v3/api-docs",
				"/v3/api-docs/**"
		);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.cors(Customizer.withDefaults())
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(exception -> exception
						.authenticationEntryPoint(restAuthenticationEntryPoint)
						.accessDeniedHandler(restAccessDeniedHandler))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers("/api/auth/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/users").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/collection-locations", "/api/collection-locations/import/xlsx").hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/collection-locations/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/routes").hasRole("ADMIN")
						.requestMatchers("/api/**").authenticated()
						.anyRequest().authenticated()
				)
				.addFilterBefore(firebaseAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
}
