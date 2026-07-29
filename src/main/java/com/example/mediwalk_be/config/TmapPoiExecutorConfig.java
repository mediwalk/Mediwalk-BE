package com.example.mediwalk_be.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class TmapPoiExecutorConfig {

	@Bean(name = "tmapPoiExecutor", destroyMethod = "shutdown")
	public ExecutorService tmapPoiExecutor(@Value("${app.tmap.poi-parallelism:8}") int parallelism) {
		return Executors.newFixedThreadPool(Math.max(1, parallelism));
	}

	@Bean(name = "routeSuggestionOuterExecutor", destroyMethod = "shutdown")
	public ExecutorService routeSuggestionOuterExecutor() {
		return Executors.newFixedThreadPool(2);
	}
}
