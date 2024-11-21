package studio.studioeye.domain.request.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
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
import studio.studioeye.domain.email.service.EmailService;
import studio.studioeye.domain.notification.application.NotificationService;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
		verify(emailService, times(1)).sendEmail(anyString(), anyString(), anyString()); // 호출 횟수 1회로 수정
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
		// Mock S3 파일 업로드 동작 설정
		when(s3Adapter.uploadFile(mockFile))
				.thenReturn(ApiResponse.ok("파일 업로드 성공", "http://example.com/file.jpg"));
		// Mock RequestRepository 저장 동작 설정
		when(requestRepository.saveAndFlush(any(Request.class))).thenAnswer(invocation -> {
			Request request = invocation.getArgument(0);
			Field idField = Request.class.getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(request, 1L); // ID 강제 설정
			return request;
		});
		// Mock Email 전송 동작 설정 (이메일 전송 실패 시)
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
	@DisplayName("retrieveRequestCountByCategoryAndState - 정상 테스트")
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
	@DisplayName("updateRequestComment - 정상 테스트")
	void updateRequestCommentSuccess() {
		// given
		Request mockRequest = mock(Request.class); // Mock 객체 생성
		lenient().when(mockRequest.getId()).thenReturn(1L); // lenient로 Stubbing 설정
		lenient().when(mockRequest.getClientName()).thenReturn("ClientName");
		lenient().when(mockRequest.getCategory()).thenReturn("Category");
		UpdateRequestCommentServiceDto dto = new UpdateRequestCommentServiceDto("AnswerText", State.APPROVED);
		when(requestRepository.findById(1L)).thenReturn(Optional.of(mockRequest));
		when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0)); // 저장된 객체 반환
		// when
		ApiResponse<String> response = requestService.updateRequestComment(1L, dto);
		// then
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatus());
		assertEquals("답변을 성공적으로 작성했습니다.", response.getMessage());
		// 검증
		verify(answerRepository, times(1)).save(any(Answer.class)); // AnswerRepository 호출 검증
		verify(requestRepository, times(1)).save(any(Request.class)); // RequestRepository 호출 검증
	}



	@Test
	@DisplayName("retrieveRequest - 정상 테스트")
	void retrieveRequestSuccess() {
		// given
		Request mockRequest = mock(Request.class); // Request 객체를 Mock으로 생성
		when(mockRequest.getId()).thenReturn(1L); // Mock id 설정
		when(requestRepository.findById(1L)).thenReturn(Optional.of(mockRequest));
		// when
		ApiResponse<Request> response = requestService.retrieveRequest(1L);
		// then
		assertNotNull(response); // 응답이 null이 아닌지 확인
		assertEquals(HttpStatus.OK, response.getStatus()); // 응답 상태 확인
		assertNotNull(response.getData()); // 응답 데이터가 null이 아닌지 확인
		assertEquals(1L, response.getData().getId()); // 데이터의 id 확인
		verify(requestRepository, times(1)).findById(1L); // findById 호출 횟수 검증
	}

}
