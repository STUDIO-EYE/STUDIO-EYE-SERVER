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

}
