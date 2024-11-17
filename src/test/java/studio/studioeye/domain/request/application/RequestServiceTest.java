package studio.studioeye.domain.request.application;

import studio.studioeye.domain.request.api.RequestController;
import studio.studioeye.domain.request.domain.Request;
import studio.studioeye.domain.request.dto.request.CreateRequestDto;
import studio.studioeye.domain.request.dto.request.UpdateRequestCommentDto;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RequestServiceTest {

	@InjectMocks
	private RequestController requestController;
	@Mock
	private RequestService requestService;
	private CreateRequestDto createRequestDto;
	private UpdateRequestCommentDto updateRequestCommentDto;
	private Request request;

	@BeforeEach
	void setUp() {
		createRequestDto = new CreateRequestDto(
				"category",
				"projectName",
				"clientName",
				"organization",
				"010-1234-5678",
				"test@example.com",
				"Developer",
				"description"
		);
		updateRequestCommentDto = new UpdateRequestCommentDto(
				"This is a comment",
				studio.studioeye.domain.request.domain.State.APPROVED
		);
		request = Request.builder()
				.category("category")
				.projectName("projectName")
				.clientName("clientName")
				.organization("organization")
				.contact("010-1234-5678")
				.email("test@example.com")
				.position("Developer")
				.description("description")
				.build();
	}
	@Test
	@DisplayName("문의 등록 성공 테스트")
	void createRequestSuccess() throws IOException {
		// given
		List<MultipartFile> files = Collections.emptyList();
		when(requestService.createRequest(any(), any())).thenReturn(ApiResponse.ok(request));
		// when
		ApiResponse<Request> response = requestController.createRequest(createRequestDto, files);
		// then
		assertEquals(HttpStatus.OK, response.getStatus());
		assertNotNull(response.getData());
		verify(requestService, times(1)).createRequest(any(), any());
	}

	@Test
	@DisplayName("문의 등록 실패 테스트 - 잘못된 입력")
	void createRequestFailDueToInvalidInput() throws IOException {
		// given
		CreateRequestDto invalidDto = new CreateRequestDto(
				"", // 잘못된 category 입력
				"projectName",
				"clientName",
				"organization",
				"010-1234-5678",
				"test@example.com",
				"Developer",
				"description"
		);
		when(requestService.createRequest(any(), any())).thenReturn(ApiResponse.withError(ErrorCode.INVALID_INPUT_VALUE));
		// when
		ApiResponse<Request> response = requestController.createRequest(invalidDto, Collections.emptyList());
		// then
		assertEquals(ErrorCode.INVALID_INPUT_VALUE.getStatus(), response.getStatus());
		verify(requestService, times(1)).createRequest(any(), any());
	}

	@Test
	@DisplayName("문의 전체 조회 성공 테스트")
	void retrieveAllRequestSuccess() {
		// given
		when(requestService.retrieveAllRequest()).thenReturn(ApiResponse.ok(List.of(request)));
		// when
		ApiResponse<List<Request>> response = requestController.retrieveAllRequest();
		// then
		assertEquals(HttpStatus.OK, response.getStatus());
		assertNotNull(response.getData());
		verify(requestService, times(1)).retrieveAllRequest();
	}

	@Test
	@DisplayName("문의 전체 조회 실패 테스트 - 데이터 없음")
	void retrieveAllRequestFailDueToNoData() {
		// given
		when(requestService.retrieveAllRequest()).thenReturn(ApiResponse.ok("문의가 존재하지 않습니다."));
		// when
		ApiResponse<List<Request>> response = requestController.retrieveAllRequest();
		// then
		assertEquals(HttpStatus.OK, response.getStatus());
		assertNull(response.getData());
		verify(requestService, times(1)).retrieveAllRequest();
	}

	@Test
	@DisplayName("문의 상세 조회 성공 테스트")
	void retrieveRequestSuccess() {
		// given
		Long requestId = 1L;
		when(requestService.retrieveRequest(requestId)).thenReturn(ApiResponse.ok(request));
		// when
		ApiResponse<Request> response = requestController.retrieveRequest(requestId);
		// then
		assertEquals(HttpStatus.OK, response.getStatus());
		assertNotNull(response.getData());
		verify(requestService, times(1)).retrieveRequest(requestId);
	}

	@Test
	@DisplayName("문의 상세 조회 실패 테스트 - 유효하지 않은 ID")
	void retrieveRequestFailDueToInvalidId() {
		// given
		Long requestId = 1L;
		when(requestService.retrieveRequest(requestId)).thenReturn(ApiResponse.withError(ErrorCode.INVALID_REQUEST_ID));
		// when
		ApiResponse<Request> response = requestController.retrieveRequest(requestId);
		// then
		assertEquals(ErrorCode.INVALID_REQUEST_ID.getStatus(), response.getStatus());
		verify(requestService, times(1)).retrieveRequest(requestId);
	}

	@Test
	@DisplayName("기간 및 상태에 따른 문의 수 조회 성공 테스트")
	void retrieveRequestCountByCategoryAndStateSuccess() {
		// given
		String category = "category";
		String state = "APPROVED";
		Integer startYear = 2023;
		Integer startMonth = 1;
		Integer endYear = 2023;
		Integer endMonth = 12;
		when(requestService.retrieveRequestCountByCategoryAndState(category, state, startYear, startMonth, endYear, endMonth))
				.thenReturn(ApiResponse.ok(List.of(Map.of("year", 2023, "month", 1, "RequestCount", 10L))));
		// when
		ApiResponse<List<Map<String, Object>>> response = requestController.retrieveStateRequestCountByPeriod(
				category, state, startYear, startMonth, endYear, endMonth);
		// then
		assertEquals(HttpStatus.OK, response.getStatus());
		assertNotNull(response.getData());
		verify(requestService, times(1)).retrieveRequestCountByCategoryAndState(category, state, startYear, startMonth, endYear, endMonth);
	}
}