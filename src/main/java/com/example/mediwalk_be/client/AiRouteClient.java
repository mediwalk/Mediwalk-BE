package com.example.mediwalk_be.client;

import com.example.mediwalk_be.dto.request.RouteGenerationRequest;
import com.example.mediwalk_be.dto.response.AiRouteGenerationResponse;

public interface AiRouteClient {

	AiRouteGenerationResponse generate(RouteGenerationRequest request);
}
