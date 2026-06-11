package com.example.mediwalk_be.domain.walk.controller;

import com.example.mediwalk_be.domain.walk.dto.request.CreateCollectionLocationRequest;
import com.example.mediwalk_be.domain.walk.dto.response.CollectionLocationResponse;
import com.example.mediwalk_be.domain.walk.dto.response.CollectionLocationWithDistanceResponse;
import com.example.mediwalk_be.domain.walk.dto.response.DestinationProximityResponse;
import com.example.mediwalk_be.domain.walk.entity.CollectionLocation;
import com.example.mediwalk_be.domain.walk.dto.response.CollectionLocationImportResponse;
import com.example.mediwalk_be.domain.walk.service.CollectionLocationService;
import com.example.mediwalk_be.domain.walk.service.CollectionLocationXlsxImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/collection-locations")
@RequiredArgsConstructor
@Tag(name = "CollectionLocation", description = "폐의약품 수거함: 위치 조회 및 근처 수거함 탐색")
public class CollectionLocationController {

	private static final int NEARBY_RADIUS_METERS = 3000;
	private static final int NEARBY_ABSOLUTE_MAX_LIMIT = 500;
	private static final int SEARCH_DEFAULT_LIMIT = 20;
	private static final int SEARCH_ABSOLUTE_MAX_LIMIT = 100;

	private final CollectionLocationService collectionLocationService;
	private final CollectionLocationXlsxImportService collectionLocationXlsxImportService;

	@PostMapping(value = "/import/xlsx", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(
			summary = "스마트서울맵 엑셀 수거함 일괄 적재",
			description = "스마트서울맵에서 내려받은 contents.xlsx를 업로드해 collection_locations에 저장합니다. "
					+ "좌표가 동일한 기존 데이터는 건너뜁니다. refreshNamesOnly=true이면 좌표로 매칭해 name만 갱신합니다."
	)
	public ResponseEntity<CollectionLocationImportResponse> importFromXlsx(
			@RequestPart("file") MultipartFile file,
			@RequestParam(defaultValue = "false") boolean refreshNamesOnly) throws IOException {
		if (file.isEmpty()) {
			return ResponseEntity.badRequest().build();
		}
		return ResponseEntity.ok(collectionLocationXlsxImportService.importFromXlsx(
				file.getInputStream(),
				file.getOriginalFilename(),
				refreshNamesOnly));
	}

	@GetMapping
	@Operation(summary = "수거함 전체 조회")
	public List<CollectionLocationResponse> findAll() {
		return collectionLocationService.findAll().stream()
				.map(CollectionLocationResponse::from)
				.toList();
	}

	@GetMapping("/nearby")
	@Operation(
			summary = "근처 수거함 조회",
			description = "현재 위치 기준 3km 이내 수거함을 거리순으로 반환합니다."
	)
	public List<CollectionLocationWithDistanceResponse> findNearby(
			@RequestParam double latitude,
			@RequestParam double longitude,
			@RequestParam(required = false) Integer limit) {
		var stream = collectionLocationService
				.findWithinRadiusOrderByDistance(latitude, longitude, NEARBY_RADIUS_METERS)
				.stream()
				.map(location -> CollectionLocationWithDistanceResponse.from(location, latitude, longitude));
		if (limit != null && limit > 0) {
			return stream.limit(Math.min(limit, NEARBY_ABSOLUTE_MAX_LIMIT)).toList();
		}
		return stream.toList();
	}

	@GetMapping("/search")
	@Operation(summary = "수거함 검색", description = "장소명·주소 부분 일치 검색. 현 위치 기준 거리·도보시간·걸음수를 포함하여 반환합니다.")
	public List<CollectionLocationWithDistanceResponse> search(
			@RequestParam String q,
			@RequestParam double latitude,
			@RequestParam double longitude,
			@RequestParam(required = false) Integer limit) {
		int effectiveLimit = limit != null && limit > 0
				? Math.min(limit, SEARCH_ABSOLUTE_MAX_LIMIT)
				: SEARCH_DEFAULT_LIMIT;
		return collectionLocationService.searchByKeyword(q.trim(), effectiveLimit).stream()
				.map(location -> CollectionLocationWithDistanceResponse.from(location, latitude, longitude))
				.toList();
	}

	@GetMapping("/{id}/destination-proximity")
	@Operation(
			summary = "수거함 인증 반경 확인",
			description = "현재 위치가 수거함 인증 반경(activationRadius, 기본 20m) 이내인지 확인합니다. "
					+ "수거함 직접 선택 후 경로 인증·이미지 인식 전에 호출하세요. "
					+ "withinActivationRadius가 true일 때만 인증을 진행하면 됩니다."
	)
	public ResponseEntity<DestinationProximityResponse> checkDestinationProximity(
			@PathVariable Long id,
			@RequestParam double currentLatitude,
			@RequestParam double currentLongitude) {
		if (collectionLocationService.findById(id).isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(
				collectionLocationService.checkDestinationProximity(id, currentLatitude, currentLongitude));
	}

	@GetMapping("/{id}")
	@Operation(summary = "수거함 단건 조회")
	public ResponseEntity<CollectionLocationResponse> findById(@PathVariable Long id) {
		return collectionLocationService.findById(id)
				.map(CollectionLocationResponse::from)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	@Operation(summary = "수거함 등록")
	public ResponseEntity<CollectionLocationResponse> create(@RequestBody CreateCollectionLocationRequest request) {
		CollectionLocation entity = CollectionLocation.builder()
				.name(request.name())
				.address(request.address())
				.latitude(request.latitude())
				.longitude(request.longitude())
				.type(request.type())
				.baseRewardAmount(request.baseRewardAmount() != null ? request.baseRewardAmount() : 3000)
				.activationRadius(request.activationRadius() != null ? request.activationRadius() : 20)
				.build();
		CollectionLocation saved = collectionLocationService.save(entity);
		return ResponseEntity.status(HttpStatus.CREATED).body(CollectionLocationResponse.from(saved));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "수거함 삭제")
	public ResponseEntity<Void> deleteById(@PathVariable Long id) {
		if (collectionLocationService.findById(id).isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		collectionLocationService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
