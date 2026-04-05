package com.example.mediwalk_be.domain.walk.client;

import com.example.mediwalk_be.domain.walk.dto.request.RouteGenerationRequest;
import com.example.mediwalk_be.domain.walk.dto.response.AiRouteGenerationResponse;

public interface AiRouteClient {

	AiRouteGenerationResponse generate(RouteGenerationRequest request);
}
