package com.example.mediwalk_be.domain.reward.controller;

import com.example.mediwalk_be.domain.reward.dto.request.CreateRewardTransactionRequest;
import com.example.mediwalk_be.domain.reward.dto.response.RewardTransactionCountResponse;
import com.example.mediwalk_be.domain.reward.dto.response.RewardTransactionResponse;
import com.example.mediwalk_be.domain.reward.service.RewardTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reward-transactions")
@RequiredArgsConstructor
@Tag(name = "Reward", description = "리워드 도메인: 이벤트(수거·미션), 거래(적립·환급), 요약")
public class RewardTransactionController {

	private final RewardTransactionService rewardTransactionService;

	@PostMapping
	@Operation(summary = "리워드 환급 신청", description = "리워드 환급(REFUND) 거래를 생성합니다. 최소 금액 및 계좌 정보 검증이 적용됩니다. 적립(ACCUMULATION) 내역은 이벤트 생성 시 자동으로 처리되며 이 API로는 생성할 수 없습니다.")
	public ResponseEntity<RewardTransactionResponse> create(@RequestBody CreateRewardTransactionRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(RewardTransactionResponse.from(rewardTransactionService.create(request)));
	}

	@GetMapping("/{id}")
	@Operation(summary = "리워드 거래 단건 조회", description = "거래 ID로 리워드 거래 상세 정보를 조회합니다. 연결된 이벤트가 있으면 제목·장소·유형 및 적립 완료 여부가 포함됩니다.")
	public ResponseEntity<RewardTransactionResponse> findById(@PathVariable Long id) {
		return rewardTransactionService.findByIdWithEvent(id)
				.map(RewardTransactionResponse::from)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping(params = "userId")
	@Operation(summary = "리워드 거래 목록 조회", description = "사용자 리워드 거래 목록을 정렬 기준(sort=latest|oldest)으로 조회합니다. startDateTime/endDateTime을 함께 전달하면 기간 필터링됩니다. 적립 건은 eventTitle·locationName·eventType으로 표기하고, accumulationCompleted는 폐의약품 수거(MEDICINE_COLLECTION) 적립 완료 뱃지용입니다.")
	public List<RewardTransactionResponse> findByUserId(
			@RequestParam Long userId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTime,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDateTime,
			@RequestParam(defaultValue = "latest") String sort,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return rewardTransactionService
				.findByUserIdWithOptionalPeriod(userId, startDateTime, endDateTime, sort, PageRequest.of(page, size)).stream()
				.map(RewardTransactionResponse::from)
				.toList();
	}

	@GetMapping("/medicine-collections")
	@Operation(summary = "폐의약품 수거 적립 목록 조회", description = "사용자 폐의약품 수거(MEDICINE_COLLECTION) 적립 내역만 최신순/오래된순으로 조회합니다. startDateTime/endDateTime을 함께 전달하면 이번 달 등 기간별 상세 리스트에도 사용할 수 있습니다.")
	public List<RewardTransactionResponse> findMedicineCollectionTransactions(
			@RequestParam Long userId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTime,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDateTime,
			@RequestParam(defaultValue = "latest") String sort,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return rewardTransactionService
				.findMedicineCollectionTransactions(userId, startDateTime, endDateTime, sort, PageRequest.of(page, size)).stream()
				.map(RewardTransactionResponse::from)
				.toList();
	}

	@GetMapping("/count")
	@Operation(summary = "폐의약품 수거 건수 조회", description = "사용자 폐의약품 수거 적립 건수를 조회합니다. startDateTime/endDateTime을 함께 전달하면 기간 건수(월별 등)를, 미전달 시 누적 건수를 반환합니다.")
	public RewardTransactionCountResponse countByUserId(
			@RequestParam Long userId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTime,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDateTime) {
		long totalCount = rewardTransactionService.countByUserIdWithOptionalPeriod(userId, startDateTime, endDateTime);
		return new RewardTransactionCountResponse(userId, totalCount);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "리워드 거래 삭제", description = "거래 ID 기준으로 리워드 거래를 삭제합니다.")
	public ResponseEntity<Void> deleteById(@PathVariable Long id) {
		if (rewardTransactionService.findById(id).isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		rewardTransactionService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
