package com.example.mediwalk_be.domain.walk.dto.request;

import com.example.mediwalk_be.domain.walk.entity.enums.CollectionLocationType;

public record CreateCollectionLocationRequest(
	String name,
	String address,
	Double latitude,
	Double longitude,
	CollectionLocationType type,
	Integer baseRewardAmount,
	Integer activationRadius
) {
}
