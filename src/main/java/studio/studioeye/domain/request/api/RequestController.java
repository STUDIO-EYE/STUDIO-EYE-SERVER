package studio.studioeye.domain.request.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import studio.studioeye.domain.request.application.RequestService;
import studio.studioeye.domain.request.dao.RequestCount;
import studio.studioeye.domain.request.domain.Request;
import studio.studioeye.domain.request.dto.request.CreateRequestDto;
import studio.studioeye.domain.request.dto.request.UpdateRequestCommentDto;
import studio.studioeye.domain.request.dto.request.UpdateRequestStateDto;
import studio.studioeye.global.common.response.ApiResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Tag(name = "문의 API", description = "문의 등록 / 수정 / 삭제 / 조회")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RequestController {

	private final RequestService requestService;

	@Operation(summary = "문의 등록 API")
	@PostMapping("/requests")
	public ApiResponse<Request> createRequest(@Valid @RequestPart("request") CreateRequestDto dto, @RequestPart(value = "files", required = false) List<MultipartFile> files) throws
		IOException {
		return requestService.createRequest(dto.toServiceRequest(), files);
	}

	@Operation(summary = "문의 삭제 API")
	@DeleteMapping("/requests/{requestId}")
	public ApiResponse<String> deleteRequest(@PathVariable Long requestId){
		return requestService.deleteRequest(requestId);
	}

	@Operation(summary = "문의 전체 조회 API")
	@GetMapping("/requests")
	public ApiResponse<List<Request>> retrieveAllRequest(){
		return requestService.retrieveAllRequest();
	}

	@Operation(summary = "문의 상세 조회 API")
	@GetMapping("/requests/{requestId}")
	public ApiResponse<Request> retrieveRequest(@PathVariable Long requestId){
		return requestService.retrieveRequest(requestId);
	}

	@Operation(summary = "전체 문의 수 조회 API")
	@GetMapping("/requests/count")
	public ApiResponse<Long> retrieveRequestCount() {
		return requestService.retrieveRequestCount();
	}

	@Operation(summary = "기간(시작점(연도, 월)~종료점(연도, 월))으로 문의 수 조회 API")
	@GetMapping("/requests/{startYear}/{startMonth}/{endYear}/{endMonth}")
	public ApiResponse<List<RequestCount>> retrieveRequestCountByPeriod(@PathVariable Integer startYear, @PathVariable Integer startMonth,
																		@PathVariable Integer endYear, @PathVariable Integer endMonth) {
		return requestService.retrieveRequestCountByPeriod(startYear, startMonth, endYear, endMonth);
	}

	@Operation(summary = "기간(시작점(연도, 월)~종료점(연도, 월))으로 카테고리별 문의 수 조회 API")
	@GetMapping("/requests/category/{startYear}/{startMonth}/{endYear}/{endMonth}")
	public ApiResponse<List<Map<String, Object>>> retrieveCategoryRequestCountByPeriod(@PathVariable Integer startYear, @PathVariable Integer startMonth,
																					   @PathVariable Integer endYear, @PathVariable Integer endMonth) {
		return requestService.retrieveCategoryRequestCountByPeriod(startYear, startMonth, endYear, endMonth);
	}

	@Operation(summary = "접수 대기 중인 문의 수 조회 API")
	@GetMapping("/requests/waiting/count")
	public ApiResponse<Long> retrieveWaitingRequestCount() {
		return requestService.retrieveWaitingRequestCount();
	}

	@Operation(summary = "접수 대기 중인 문의 목록 조회 API")
	@GetMapping("/requests/waiting")
	public ApiResponse<List<Request>> retrieveWaitingRequest() {
		return requestService.retrieveWaitingRequest();
	}

	@Operation(summary = "문의 목록 페이지네이션 조회 API")
	@GetMapping("/requests/page")
	public Page<Request> retrieveRequestPage(@RequestParam(defaultValue = "0") int page,
											 @RequestParam(defaultValue = "10") int size) {
		return requestService.retrieveRequestPage(page, size);
	}

	@Operation(summary = "문의 답변 등록 API")
	@PutMapping("/requests/{requestId}/comment")
	public ApiResponse<String> updateRequestComment(@PathVariable Long requestId, @Valid @RequestBody UpdateRequestCommentDto dto) {
		return requestService.updateRequestComment(requestId, dto.toServiceRequest());
	}

	@Operation(summary = "문의 상태 변경 API")
	@PutMapping("/requests/{requestId}/state")
	public ApiResponse<String> updateRequestComment(@PathVariable Long requestId, @Valid @RequestBody UpdateRequestStateDto dto) {
		return requestService.updateRequestState(requestId, dto.toServiceRequest());
	}
}
