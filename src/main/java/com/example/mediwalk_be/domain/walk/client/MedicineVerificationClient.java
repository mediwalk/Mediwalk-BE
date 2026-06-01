package com.example.mediwalk_be.domain.walk.client;

import com.example.mediwalk_be.domain.walk.client.dto.AiVerificationPrediction;

import java.util.List;

public interface MedicineVerificationClient {

	List<AiVerificationPrediction> verify(String base64Image);
}
