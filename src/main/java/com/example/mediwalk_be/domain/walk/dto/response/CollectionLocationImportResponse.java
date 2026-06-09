package com.example.mediwalk_be.domain.walk.dto.response;

public record CollectionLocationImportResponse(
		String themeId,
		int fetchedFromApi,
		int skippedInvalid,
		int skippedDuplicate,
		int inserted,
		int updated,
		int totalInDatabase
) {
}
