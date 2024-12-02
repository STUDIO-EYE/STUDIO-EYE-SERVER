package studio.studioeye.domain.request.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import studio.studioeye.domain.email.service.EmailService;
import studio.studioeye.domain.notification.application.NotificationService;
import studio.studioeye.domain.request.dao.AnswerRepository;
import studio.studioeye.domain.request.dao.RequestCount;
import studio.studioeye.domain.request.dao.RequestCountImpl;
import studio.studioeye.domain.request.dao.RequestRepository;
import studio.studioeye.domain.request.domain.Answer;
import studio.studioeye.domain.request.domain.Request;
import studio.studioeye.domain.request.domain.State;
import studio.studioeye.domain.request.dto.request.CreateRequestServiceDto;
import studio.studioeye.domain.request.dto.request.UpdateRequestCommentServiceDto;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;
import studio.studioeye.infrastructure.s3.S3Adapter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RequestServiceTest {
	@InjectMocks
	private RequestService requestService;
	@Mock
	private RequestRepository requestRepository;
	@Mock
	private S3Adapter s3Adapter;
	@Mock
	private EmailService emailService;
	@Mock
	private NotificationService notificationService;
	@Mock
	private AnswerRepository answerRepository;

	MockMultipartFile mockFile = new MockMultipartFile(
			"file",
			"testImage.jpg",
			"image/jpeg",
			"Test Image Content".getBytes()
	);

	@Test
	@DisplayName("createRequest 성공 테스트 - 이메일과 파일 업로드 성공")
	public void createRequestSuccess() throws IOException {
		// given
		CreateRequestServiceDto dto = new CreateRequestServiceDto(
				"Category", "ProjectName", "ClientName",
				"Organization", "010-1234-5678",
				"test@example.com", "Position", "Description"
		);
		when(s3Adapter.uploadFile(mockFile))
				.thenReturn(ApiResponse.ok("파일 업로드 성공", "http://example.com/file.jpg"));
		when(requestRepository.saveAndFlush(any(Request.class))).thenAnswer(invocation -> {
			Request request = invocation.getArgument(0);
			Field idField = Request.class.getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(request, 1L); // ID 강제 설정
			return request;
		});
		when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);
		doAnswer(invocation -> {
			Long requestId = invocation.getArgument(0);
			System.out.println("Notification sent for request ID: " + requestId); // 테스트용 출력
			return null;
		}).when(notificationService).subscribe(anyLong());
		// when
		ApiResponse<Request> response = requestService.createRequest(dto, List.of(mockFile));
		// then
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatus());
		assertEquals("문의를 성공적으로 등록하였습니다.", response.getMessage());
		assertNotNull(response.getData());
		assertEquals(1L, response.getData().getId());
		// Mock 메서드 호출 검증
		verify(s3Adapter, times(1)).uploadFile(mockFile);
		verify(emailService, times(1)).sendEmail(anyString(), anyString(), anyString());
		verify(notificationService, times(1)).subscribe(1L);
	}

	@Test
	@DisplayName("createRequest 실패 테스트 - 잘못된 이메일 형식")
	public void createRequestFail_InvalidEmail() throws IOException {
		// given
		CreateRequestServiceDto dto = new CreateRequestServiceDto(
				"Category", "ProjectName", "ClientName",
				"Organization", "010-1234-5678",
				"invalid-email", "Position", "Description"
		);
		// when
		ApiResponse<Request> response = requestService.createRequest(dto, null);
		// then
		assertNotNull(response);
		assertEquals(ErrorCode.INVALID_EMAIL_FORMAT.getStatus(), response.getStatus());
		assertEquals(ErrorCode.INVALID_EMAIL_FORMAT.getMessage(), response.getMessage());
		verify(s3Adapter, never()).uploadFile(any(MultipartFile.class));
		verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
		verify(notificationService, never()).subscribe(anyLong());
	}

	@Test
	@DisplayName("createRequest 실패 테스트 - 파일 업로드 실패")
	public void createRequestFail_FileUploadError() throws IOException {
		// given
		CreateRequestServiceDto dto = new CreateRequestServiceDto(
				"Category", "ProjectName", "ClientName",
				"Organization", "010-1234-5678",
				"test@example.com", "Position", "Description"
		);
		when(s3Adapter.uploadFile(mockFile))
				.thenReturn(ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT));
		// when
		ApiResponse<Request> response = requestService.createRequest(dto, List.of(mockFile));
		// then
		assertNotNull(response);
		assertEquals(ErrorCode.ERROR_S3_UPDATE_OBJECT.getStatus(), response.getStatus());
		assertEquals(ErrorCode.ERROR_S3_UPDATE_OBJECT.getMessage(), response.getMessage());
		verify(s3Adapter, times(1)).uploadFile(mockFile);
		verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
		verify(notificationService, never()).subscribe(anyLong());
	}

	@Test
	@DisplayName("createRequest 실패 테스트 - 이메일 전송 실패")
	public void createRequestFail_EmailSendError() throws IOException {
		// given
		CreateRequestServiceDto dto = new CreateRequestServiceDto(
				"Category", "ProjectName", "ClientName",
				"Organization", "010-1234-5678",
				"test@example.com", "Position", "Description"
		);
		when(s3Adapter.uploadFile(mockFile))
				.thenReturn(ApiResponse.ok("파일 업로드 성공", "http://example.com/file.jpg"));
		when(requestRepository.saveAndFlush(any(Request.class))).thenAnswer(invocation -> {
			Request request = invocation.getArgument(0);
			Field idField = Request.class.getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(request, 1L);
			return request;
		});
		when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(false);
		// when
		ApiResponse<Request> response = requestService.createRequest(dto, List.of(mockFile));
		// then
		assertNotNull(response);
		assertEquals(ErrorCode.EMAIL_SIZE_EXCEEDED.getStatus(), response.getStatus());
		assertEquals(ErrorCode.EMAIL_SIZE_EXCEEDED.getMessage(), response.getMessage());
		verify(s3Adapter, times(1)).uploadFile(mockFile);
		verify(emailService, times(1)).sendEmail(anyString(), anyString(), anyString());
		verify(notificationService, never()).subscribe(anyLong());
	}

	@Test
	@DisplayName("retrieveRequestCountByCategoryAndState 성공 테스트 - 카테고리와 상태로 요청 수 조회")
	void retrieveRequestCountByCategoryAndStateSuccess() {
		// given
		List<RequestCount> mockResult = List.of(
				new RequestCountImpl(2024, 11, 5L, "CategoryA", State.WAITING),
				new RequestCountImpl(2024, 12, 10L, "CategoryB", State.APPROVED)
		);
		when(requestRepository.findReqNumByYearAndMonthBetweenWithCategoryAndState(
				2024, 11, 2024, 12, "CategoryA", State.WAITING
		)).thenReturn(mockResult);
		// when
		ApiResponse<List<Map<String, Object>>> response = requestService.retrieveRequestCountByCategoryAndState(
				"CategoryA", "WAITING", 2024, 11, 2024, 12
		);
		// then
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatus());
		assertNotNull(response.getData());
		assertEquals(2, response.getData().size());
		verify(requestRepository, times(1)).findReqNumByYearAndMonthBetweenWithCategoryAndState(
				2024, 11, 2024, 12, "CategoryA", State.WAITING
		);
	}

	@Test
	@DisplayName("retrieveRequestCountByCategoryAndState 실패 테스트 - 잘못된 기간")
	void retrieveRequestCount_InvalidPeriod() {
		// when
		ApiResponse<List<Map<String, Object>>> response = requestService.retrieveRequestCountByCategoryAndState(
				"Category", "APPROVED", 2023, 12, 2023, 10
		);
		// then
		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
		assertEquals(ErrorCode.INVALID_PERIOD_FORMAT.getMessage(), response.getMessage());
	}

	@Test
	@DisplayName("retrieveRequestCountByCategoryAndState 성공 테스트 - 상태가 all")
	void retrieveRequestCount_StateAll() {
		// given
		List<RequestCount> mockData = List.of(new RequestCountImpl(2023, 11, 5L, "Category", null));
		when(requestRepository.findReqNumByYearAndMonthBetweenWithCategoryAndState(anyInt(), anyInt(), anyInt(), anyInt(), anyString(), any())).thenReturn(mockData);
		// when
		ApiResponse<List<Map<String, Object>>> response = requestService.retrieveRequestCountByCategoryAndState(
				"Category", "all", 2023, 1, 2023, 12
		);
		// then
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatus());
		assertNotNull(response.getData());
		verify(requestRepository, times(1)).findReqNumByYearAndMonthBetweenWithCategoryAndState(anyInt(), anyInt(), anyInt(), anyInt(), anyString(), isNull());
	}

	@Test
	@DisplayName("retrieveRequest 성공 테스트 - 요청 단일 조회")
	void retrieveRequestSuccess() {
		// given
		Request mockRequest = mock(Request.class);
		when(mockRequest.getId()).thenReturn(1L);
		when(requestRepository.findById(1L)).thenReturn(Optional.of(mockRequest));
		// when
		ApiResponse<Request> response = requestService.retrieveRequest(1L);
		// then
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatus());
		assertNotNull(response.getData());
		assertEquals(1L, response.getData().getId());
		verify(requestRepository, times(1)).findById(1L);
	}

	@Test
	@DisplayName("updateRequestComment 성공 테스트 - 댓글과 상태 업데이트 성공")
	void updateRequestCommentSuccess() {
		// given
		Request mockRequest = mock(Request.class);
		lenient().when(mockRequest.getId()).thenReturn(1L);
		lenient().when(mockRequest.getClientName()).thenReturn("ClientName");
		lenient().when(mockRequest.getCategory()).thenReturn("Category");
		UpdateRequestCommentServiceDto dto = new UpdateRequestCommentServiceDto("AnswerText", State.APPROVED);
		when(requestRepository.findById(1L)).thenReturn(Optional.of(mockRequest));
		when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));
		// when
		ApiResponse<String> response = requestService.updateRequestComment(1L, dto);
		// then
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatus());
		assertEquals("답변을 성공적으로 작성했습니다.", response.getMessage());
		verify(answerRepository, times(1)).save(any(Answer.class));
		verify(requestRepository, times(1)).save(any(Request.class));
	}

	@Test
	@DisplayName("updateRequestComment 실패 테스트 - 빈 답변")
	void updateRequestComment_EmptyAnswer() {
		// given
		UpdateRequestCommentServiceDto dto = new UpdateRequestCommentServiceDto("", State.APPROVED);
		// when
		ApiResponse<String> response = requestService.updateRequestComment(1L, dto);
		// then
		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
		assertEquals(ErrorCode.INVALID_INPUT_VALUE.getMessage(), response.getMessage());
	}

	@Test
	@DisplayName("updateRequestComment 실패 테스트 - 상태가 null")
	void updateRequestComment_NullState() {
		// given
		UpdateRequestCommentServiceDto dto = new UpdateRequestCommentServiceDto("AnswerText", null);
		// when
		ApiResponse<String> response = requestService.updateRequestComment(1L, dto);
		// then
		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
		assertEquals(ErrorCode.INVALID_INPUT_VALUE.getMessage(), response.getMessage());
	}

	@Test
	@DisplayName("문의 삭제 성공 테스트")
	void deleteRequestSuccess() {
		// given
		Long id = 1L;
		Request savedRequest = Request.builder()
				.projectName("Test name")
				.category("Test category")
				.clientName("Test client name")
				.organization("Test Organization")
				.email("Test Email")
				.position("Test position")
				.description("Test description")
				.year(2024)
				.month(11)
				.state(State.WAITING)
				.build();

		// stub
		when(requestRepository.findById(id)).thenReturn(Optional.of(savedRequest));

		// when
		ApiResponse<String> response = requestService.deleteRequest(id);

		assertEquals(HttpStatus.OK, response.getStatus());
		assertEquals("문의를 성공적으로 삭제했습니다.", response.getMessage());
		Mockito.verify(requestRepository, times(1)).findById(id);
		Mockito.verify(requestRepository, times(1)).delete(savedRequest);
	}

	@Test
	@DisplayName("문의 삭제 실패 테스트")
	public void deleteRequestFail() {
		// given
		Long id = 1L;

		// stub
		when(requestRepository.findById(id)).thenReturn(Optional.empty());

		// when
		ApiResponse<String> response = requestService.deleteRequest(id);

		// then
		assertEquals(ErrorCode.INVALID_REQUEST_ID.getStatus(), response.getStatus());
		assertEquals(ErrorCode.INVALID_REQUEST_ID.getMessage(), response.getMessage());
		Mockito.verify(requestRepository, times(1)).findById(id);
		Mockito.verify(requestRepository, Mockito.never()).delete(any());
	}
}
