package studio.studioeye.domain.request.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import studio.studioeye.domain.request.dao.AnswerRepository;
import studio.studioeye.domain.request.dao.RequestRepository;
import studio.studioeye.domain.request.domain.Answer;
import studio.studioeye.domain.request.domain.Request;
import studio.studioeye.domain.request.domain.State;
import studio.studioeye.domain.request.dto.request.CreateRequestServiceDto;
import studio.studioeye.domain.request.dto.request.UpdateRequestCommentServiceDto;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;
import studio.studioeye.infrastructure.s3.S3Adapter;
import studio.studioeye.domain.email.service.EmailService;
import studio.studioeye.domain.notification.application.NotificationService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

	@InjectMocks
	private RequestService requestService;

	@Mock
	private RequestRepository requestRepository;

	@Mock
	private AnswerRepository answerRepository;

	@Mock
	private S3Adapter s3Adapter;

	@Mock
	private EmailService emailService;

	@Mock
	private NotificationService notificationService;

	private CreateRequestServiceDto createRequestDto;
	private Request request;

	@BeforeEach
	void setUp() {
		createRequestDto = new CreateRequestServiceDto(
				"category",
				"projectName",
				"clientName",
				"organization",
				"valid.email@example.com",
				"010-1234-5678",
				"Developer",
				"description"
		);

		request = Request.builder()
				.id(1L)
				.category("category")
				.projectName("projectName")
				.clientName("clientName")
				.organization("organization")
				.email("valid.email@example.com")
				.contact("010-1234-5678")
				.position("Developer")
				.description("description")
				.state(State.WAITING)
				.build();
	}

	@Test
	@DisplayName("문의 등록 성공 테스트")
	void createRequestSuccess() throws IOException {
		// given
		List<MultipartFile> files = Collections.emptyList();
		when(s3Adapter.uploadFile(any())).thenReturn(ApiResponse.ok("https://example.com/file"));
		when(requestRepository.saveAndFlush(any())).thenReturn(request);

		// when
		ApiResponse<Request> response = requestService.createRequest(createRequestDto, files);

		// then
		assertEquals(200, response.getStatus().value());
		assertNotNull(response.getData());
		verify(requestRepository, times(1)).saveAndFlush(any());
		verify(emailService, times(1)).sendEmail(any(), any(), any());
		verify(notificationService, times(1)).subscribe(any());
	}

	@Test
	@DisplayName("문의 등록 실패 테스트 - 잘못된 이메일")
	void createRequestFailDueToInvalidEmail() throws IOException {
		// given
		CreateRequestServiceDto invalidDto = new CreateRequestServiceDto(
				"category", "projectName", "clientName", "organization",
				"invalid-email", "010-1234-5678", "Developer", "description"
		);

		// when
		ApiResponse<Request> response = requestService.createRequest(invalidDto, Collections.emptyList());

		// then
		assertEquals(ErrorCode.INVALID_EMAIL_FORMAT.getStatus(), response.getStatus());
		verify(requestRepository, times(0)).saveAndFlush(any());
		verify(emailService, times(0)).sendEmail(any(), any(), any());
		verify(notificationService, times(0)).subscribe(any());
	}

	@Test
	@DisplayName("문의 등록 실패 테스트 - 파일 업로드 실패")
	void createRequestFailDueToFileUploadError() throws IOException {
		// given
		List<MultipartFile> files = List.of(mock(MultipartFile.class));
		when(s3Adapter.uploadFile(any())).thenReturn(ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT));

		// when
		ApiResponse<Request> response = requestService.createRequest(createRequestDto, files);

		// then
		assertEquals(ErrorCode.ERROR_S3_UPDATE_OBJECT.getStatus(), response.getStatus());
		verify(requestRepository, times(0)).saveAndFlush(any());
		verify(emailService, times(0)).sendEmail(any(), any(), any());
		verify(notificationService, times(0)).subscribe(any());
	}

	@Test
	@DisplayName("문의 조회 성공 테스트")
	void retrieveRequestSuccess() {
		// given
		when(requestRepository.findById(anyLong())).thenReturn(Optional.of(request));

		// when
		ApiResponse<Request> response = requestService.retrieveRequest(1L);

		// then
		assertEquals(200, response.getStatus().value());
		assertNotNull(response.getData());
		verify(requestRepository, times(1)).findById(1L);
	}

	@Test
	@DisplayName("문의 조회 실패 테스트 - 잘못된 ID")
	void retrieveRequestFail() {
		// given
		when(requestRepository.findById(anyLong())).thenReturn(Optional.empty());

		// when
		ApiResponse<Request> response = requestService.retrieveRequest(999L);

		// then
		assertEquals(ErrorCode.INVALID_REQUEST_ID.getStatus(), response.getStatus());
		verify(requestRepository, times(1)).findById(999L);
	}

	@Test
	@DisplayName("문의 답변 등록 성공 테스트")
	void updateRequestCommentSuccess() {
		// given
		Long requestId = 1L;
		UpdateRequestCommentServiceDto dto = new UpdateRequestCommentServiceDto("답변 내용", State.APPROVED);
		when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));
		when(answerRepository.save(any())).thenReturn(new Answer());

		// when
		ApiResponse<String> response = requestService.updateRequestComment(requestId, dto);

		// then
		assertEquals(200, response.getStatus().value());
		verify(requestRepository, times(1)).findById(requestId);
		verify(answerRepository, times(1)).save(any());
		verify(emailService, times(1)).sendEmail(any(), any(), any());
	}

	@Test
	@DisplayName("문의 답변 등록 실패 테스트 - 잘못된 ID")
	void updateRequestCommentFail() {
		// given
		Long requestId = 999L;
		UpdateRequestCommentServiceDto dto = new UpdateRequestCommentServiceDto("답변 내용", State.APPROVED);
		when(requestRepository.findById(requestId)).thenReturn(Optional.empty());

		// when
		ApiResponse<String> response = requestService.updateRequestComment(requestId, dto);

		// then
		assertEquals(ErrorCode.INVALID_REQUEST_ID.getStatus(), response.getStatus());
		verify(requestRepository, times(1)).findById(requestId);
		verify(answerRepository, times(0)).save(any());
		verify(emailService, times(0)).sendEmail(any(), any(), any());
	}

	@Test
	@DisplayName("문의 삭제 성공 테스트")
	void deleteRequestSuccess() {
		// given
		Long requestId = 1L;
		when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));
		doNothing().when(requestRepository).delete(any());

		// when
		ApiResponse<String> response = requestService.deleteRequest(requestId);

		// then
		assertEquals(200, response.getStatus().value());
		verify(requestRepository, times(1)).findById(requestId);
		verify(requestRepository, times(1)).delete(any());
	}

	@Test
	@DisplayName("문의 삭제 실패 테스트 - 잘못된 ID")
	void deleteRequestFail() {
		// given
		Long requestId = 999L;
		when(requestRepository.findById(requestId)).thenReturn(Optional.empty());

		// when
		ApiResponse<String> response = requestService.deleteRequest(requestId);

		// then
		assertEquals(ErrorCode.INVALID_REQUEST_ID.getStatus(), response.getStatus());
		verify(requestRepository, times(1)).findById(requestId);
		verify(requestRepository, times(0)).delete(any());
	}

	@Test
	@DisplayName("대기 중 문의 수 조회 성공 테스트")
	void retrieveWaitingRequestCountSuccess() {
		// given
		when(requestRepository.countByState(State.WAITING)).thenReturn(5L);

		// when
		ApiResponse<Long> response = requestService.retrieveWaitingRequestCount();

		// then
		assertEquals(200, response.getStatus().value());
		assertEquals(5L, response.getData());
		verify(requestRepository, times(1)).countByState(State.WAITING);
	}
}
