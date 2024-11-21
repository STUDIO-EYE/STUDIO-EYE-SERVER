package studio.studioeye.domain.request.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import studio.studioeye.domain.email.service.EmailService;
import studio.studioeye.domain.notification.application.NotificationService;
import studio.studioeye.domain.request.dao.AnswerRepository;
import studio.studioeye.domain.request.dao.RequestRepository;
import studio.studioeye.domain.request.domain.Request;
import studio.studioeye.domain.request.domain.State;
import studio.studioeye.domain.request.dto.request.UpdateRequestStateServiceDto;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
	private EmailService emailService;
	@Mock
	private NotificationService notificationService;
	private Request request;
	@BeforeEach
	void setUp() {
		request = Request.builder()
				.category("category")
				.projectName("projectName")
				.clientName("clientName")
				.organization("organization")
				.contact("010-1234-5678")
				.email("test@example.com")
				.position("Developer")
				.description("description")
				.state(State.WAITING)
				.build();
	}

	@Test
	@DisplayName("접수 대기 중인 문의 수 조회 성공 테스트")
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

	@Test
	@DisplayName("접수 대기 중인 문의 수 조회 실패 테스트 - 예외 발생")
	void retrieveWaitingRequestCountFail() {
		// given
		when(requestRepository.countByState(State.WAITING)).thenThrow(new RuntimeException("DB 오류"));
		// when
		RuntimeException exception = assertThrows(RuntimeException.class, () ->
				requestService.retrieveWaitingRequestCount()
		);
		// then
		assertEquals("DB 오류", exception.getMessage());
		verify(requestRepository, times(1)).countByState(State.WAITING);
	}

	@Test
	@DisplayName("페이지네이션된 문의 목록 조회 성공 테스트")
	void retrieveRequestPageSuccess() {
		// given
		Pageable pageable = PageRequest.of(0, 10);
		Page<Request> mockPage = mock(Page.class);
		when(requestRepository.findAll(pageable)).thenReturn(mockPage);
		// when
		Page<Request> response = requestService.retrieveRequestPage(0, 10);
		// then
		assertNotNull(response);
		verify(requestRepository, times(1)).findAll(pageable);
	}
	@Test
	@DisplayName("페이지네이션된 문의 목록 조회 실패 테스트 - 잘못된 페이지 요청")
	void retrieveRequestPageFail() {
		// given
		int invalidPage = -1;
		// when
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
				requestService.retrieveRequestPage(invalidPage, 10)
		);
		// then
		assertEquals("Page index must not be less than zero", exception.getMessage());
	}

	@Test
	@DisplayName("전체 문의 수 조회 성공 테스트")
	void retrieveRequestCountSuccess() {
		// given
		when(requestRepository.count()).thenReturn(100L);
		// when
		ApiResponse<Long> response = requestService.retrieveRequestCount();
		// then
		assertEquals(200, response.getStatus().value());
		assertEquals(100L, response.getData());
		verify(requestRepository, times(1)).count();
	}

	@Test
	@DisplayName("전체 문의 수 조회 실패 테스트 - DB 오류")
	void retrieveRequestCountFail() {
		// given
		when(requestRepository.count()).thenThrow(new RuntimeException("DB 오류"));
		// when
		RuntimeException exception = assertThrows(RuntimeException.class, () ->
				requestService.retrieveRequestCount()
		);
		// then
		assertEquals("DB 오류", exception.getMessage());
		verify(requestRepository, times(1)).count();
	}

	@Test
	@DisplayName("문의 상태 업데이트 성공 테스트")
	void updateRequestStateSuccess() {
		// given
		Long requestId = 1L;
		UpdateRequestStateServiceDto dto = new UpdateRequestStateServiceDto(State.APPROVED);
		when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));
		when(requestRepository.save(any(Request.class))).thenReturn(request); // 추가 설정
		// when
		ApiResponse<String> response = requestService.updateRequestState(requestId, dto);
		// then
		assertEquals(200, response.getStatus().value());
		assertEquals("상태를 성공적으로 수정했습니다.", response.getMessage());
		verify(requestRepository, times(1)).findById(requestId);
		verify(requestRepository, times(1)).save(request);
	}

	@Test
	@DisplayName("문의 상태 업데이트 실패 테스트 - 잘못된 ID")
	void updateRequestStateFail() {
		// given
		Long requestId = 999L;
		UpdateRequestStateServiceDto dto = new UpdateRequestStateServiceDto(State.APPROVED);
		when(requestRepository.findById(requestId)).thenReturn(Optional.empty());
		// when
		ApiResponse<String> response = requestService.updateRequestState(requestId, dto);
		// then
		assertEquals(ErrorCode.INVALID_REQUEST_ID.getStatus(), response.getStatus());
		verify(requestRepository, times(1)).findById(requestId);
		verify(requestRepository, times(0)).save(any());
	}

	@Test
	@DisplayName("문의 삭제 성공 테스트")
	void deleteRequestSuccess() {
		// given
		Long requestId = 1L;
		when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));
		doNothing().when(requestRepository).delete(request);
		// when
		ApiResponse<String> response = requestService.deleteRequest(requestId);
		// then
		assertEquals(200, response.getStatus().value());
		assertEquals("문의를 성공적으로 삭제했습니다.", response.getMessage());
		verify(requestRepository, times(1)).findById(requestId);
		verify(requestRepository, times(1)).delete(request);
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
}