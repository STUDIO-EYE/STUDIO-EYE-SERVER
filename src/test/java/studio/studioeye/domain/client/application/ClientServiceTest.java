package studio.studioeye.domain.client.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import studio.studioeye.domain.client.dao.ClientRepository;
import studio.studioeye.domain.client.domain.Client;
import studio.studioeye.domain.client.dto.request.CreateClientServiceRequestDto;
import studio.studioeye.domain.client.dto.request.UpdateClientServiceRequestDto;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;
import studio.studioeye.infrastructure.s3.S3Adapter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {

    @InjectMocks
    private ClientService clientService;

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private S3Adapter s3Adapter;

    private final MockMultipartFile mockFile = new MockMultipartFile(
            "file",
            "testLogo.jpg",
            "image/jpeg",
            "Test Logo Content".getBytes()
    );

    @Test
    @DisplayName("Client 생성 성공")
    public void createClientSuccess() throws IOException {
        // given
        CreateClientServiceRequestDto requestDto = new CreateClientServiceRequestDto("Test_Client", true);

        when(s3Adapter.uploadImage(any(MultipartFile.class)))
                .thenReturn(ApiResponse.ok("Image uploaded", "test-logo-url.png"));

        Client client = new Client("Test Name", "test-logo-url.png", true);
        client.setId(1L);
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        // When
        ApiResponse<Client> response = clientService.createClient(requestDto, mockFile);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("클라이언트를 성공적으로 등록하였습니다.", response.getMessage());
    }

    @Test
    @DisplayName("Client 생성 실패 - 이미지 업로드 실패")
    public void createClientFailDueToImageUpload() throws IOException {
        // given
        CreateClientServiceRequestDto requestDto = new CreateClientServiceRequestDto("Test_Client", true);

        when(s3Adapter.uploadImage(any(MultipartFile.class)))
                .thenReturn(ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT));

        // When
        ApiResponse<Client> response = clientService.createClient(requestDto, mockFile);

        // then
        assertNotNull(response);
        assertEquals(ErrorCode.ERROR_S3_UPDATE_OBJECT.getStatus(), response.getStatus());
        assertEquals(ErrorCode.ERROR_S3_UPDATE_OBJECT.getMessage(), response.getMessage());
    }
}
