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
class ClientServiceTest {

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
    void createClientSuccess() {
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
    void createClientFailDueToImageUpload() {
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
    @Test
    @DisplayName("Client 전체 조회 성공")
    void retrieveAllClientSuccess() {
        // given
        List<Client> clientList = List.of(
                new Client("Client1", "http://example.com/logo1.jpg", true),
                new Client("Client2", "http://example.com/logo2.jpg", false)
        );
        clientList.get(0).setId(1L);
        clientList.get(1).setId(2L);
        when(clientRepository.findAll()).thenReturn(clientList);

        // when
        ApiResponse<List<Map<String, Object>>> response = clientService.retrieveAllClient();
        List<Map<String, Object>> retrievedClients = response.getData();

        // then
        assertNotNull(retrievedClients);
        assertEquals(2, retrievedClients.size());
        assertEquals("클라이언트 목록을 성공적으로 조회했습니다.", response.getMessage());
    }

    @Test
    @DisplayName("Client 전체 조회 실패 - 데이터 없음")
    void retrieveAllClientFail_NoData() {
        // given
        when(clientRepository.findAll()).thenReturn(Collections.emptyList());

        // when
        ApiResponse<List<Map<String, Object>>> response = clientService.retrieveAllClient();

        // then
        assertNull(response.getData());
        assertEquals("클라이언트가 존재하지 않습니다.", response.getMessage());
    }

    @Test
    @DisplayName("Client 단일 조회 성공")
    void retrieveClientSuccess() {
        // given
        Long clientId = 1L;
        Client client = new Client("Client1", "http://example.com/logo1.jpg", true);
        client.setId(1L);
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        // when
        ApiResponse<Map<String, Object>> response = clientService.retrieveClient(clientId);
        Map<String, Object> retrievedClient = response.getData();

        // then
        assertNotNull(retrievedClient);
        assertEquals("클라이언트를 성공적으로 조회했습니다.", response.getMessage());
    }

    @Test
    @DisplayName("Client 단일 조회 실패 - 유효하지 않은 ID")
    void retrieveClientFail_InvalidId() {
        // given
        Long clientId = 1L;
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        // when
        ApiResponse<Map<String, Object>> response = clientService.retrieveClient(clientId);

        // then
        assertNull(response.getData());
        assertEquals(ErrorCode.INVALID_CLIENT_ID.getMessage(), response.getMessage());
        assertEquals(ErrorCode.INVALID_CLIENT_ID.getStatus(), response.getStatus());
    }

    @Test
    @DisplayName("Client 로고 이미지 리스트 조회 성공")
    void retrieveAllClientLogoImgListSuccess() {
        // given
        List<Client> clientList = List.of(
                new Client("Client1", "http://example.com/logo1.jpg", true),
                new Client("Client2", "http://example.com/logo2.jpg", false)
        );

        when(clientRepository.findAll()).thenReturn(clientList);

        // when
        ApiResponse<List<String>> response = clientService.retrieveAllClientLogoImgList();
        List<String> logoImgList = response.getData();

        // then
        assertNotNull(logoImgList);
        assertEquals(2, logoImgList.size());
        assertEquals("클라이언트 로고 이미지 리스트를 성공적으로 조회했습니다.", response.getMessage());
    }

    @Test
    @DisplayName("Client 로고 이미지 리스트 조회 실패 - 데이터 없음")
    void retrieveAllClientLogoImgListFail_NoData() {
        // given
        when(clientRepository.findAll()).thenReturn(Collections.emptyList());

        // when
        ApiResponse<List<String>> response = clientService.retrieveAllClientLogoImgList();

        // then
        assertNull(response.getData());
        assertEquals("클라이언트가 존재하지 않습니다.", response.getMessage());
    }

    @Test
    @DisplayName("Client 페이지네이션 조회 성공")
    void retrieveClientPageSuccess() {
        // given
        List<Client> clientList = List.of(
                new Client("Client1", "http://example.com/logo1.jpg", true),
                new Client("Client2", "http://example.com/logo2.jpg", false)
        );

        Page<Client> page = new PageImpl<>(clientList);
        Pageable pageable = PageRequest.of(0, 2);

        when(clientRepository.findAll(pageable)).thenReturn(page);

        // when
        Page<Client> result = clientService.retrieveClientPage(0, 2);

        // then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(clientList, result.getContent());
    }

    @Test
    @DisplayName("Client 페이지네이션 조회 실패 - 페이지 데이터 없음")
    void retrieveClientPageFail_NoData() {
        // given
        Pageable pageable = PageRequest.of(0, 2);
        Page<Client> emptyPage = new PageImpl<>(Collections.emptyList());

        when(clientRepository.findAll(pageable)).thenReturn(emptyPage);

        // when
        Page<Client> result = clientService.retrieveClientPage(0, 2);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Client 수정 성공")
    void updateClientSuccess() {
        // given
        Long clientId = 1L;
        UpdateClientServiceRequestDto requestDto = new UpdateClientServiceRequestDto(clientId, "Updated_Client", false);
        Client savedClient = new Client("Client", "http://example.com/logo.jpg", true);
        savedClient.setId(clientId);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(savedClient));

        MockMultipartFile mockFile = new MockMultipartFile("logo", "logo.png", "image/png", "test image content".getBytes());

        when(s3Adapter.uploadImage(any(MultipartFile.class)))
                .thenReturn(ApiResponse.ok("S3에 이미지 업로드 성공", "Updated Test Logo Url"));

        when(clientRepository.save(any(Client.class))).thenReturn(savedClient);

        // when
        ApiResponse<Client> response = clientService.updateClient(requestDto, mockFile);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("클라이언트를 성공적으로 수정했습니다.", response.getMessage());
        assertEquals("Updated Test Logo Url", response.getData().getLogoImg()); // 로고 URL 확인 추가
    }

    @Test
    @DisplayName("Client 수정 실패 - 유효하지 않은 ID")
    void updateClientFail_InvalidId() {
        // given
        Long clientId = 1L;
        UpdateClientServiceRequestDto requestDto = new UpdateClientServiceRequestDto(clientId, "Updated_Client", false);

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        // when
        ApiResponse<Client> response = clientService.updateClient(requestDto, mockFile);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals(ErrorCode.INVALID_CLIENT_ID.getMessage(), response.getMessage());
    }

    @Test
    @DisplayName("클라이언트 정보 수정 성공")
    void updateClientTextSuccess() {
        // given
        Long clientId = 1L;
        UpdateClientServiceRequestDto requestDto = new UpdateClientServiceRequestDto(clientId, "Updated_Client", false);
        Client existingClient = new Client("Client", "http://example.com/logo.jpg", true);
        existingClient.setId(clientId);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));

        when(clientRepository.save(any(Client.class))).thenReturn(existingClient);

        // when
        ApiResponse<Client> response = clientService.updateClientText(requestDto);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("클라이언트를 성공적으로 수정했습니다.", response.getMessage());
        assertEquals("Updated_Client", response.getData().getName());
    }

    @Test
    @DisplayName("클라이언트 정보 수정 실패 - 클라이언트가 존재하지 않음")
    void updateClientTextClientNotFound() {
        // given
        Long clientId = 1L;
        UpdateClientServiceRequestDto requestDto = new UpdateClientServiceRequestDto(clientId, "Updated_Client", false);

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        // when
        ApiResponse<Client> response = clientService.updateClientText(requestDto);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals(ErrorCode.INVALID_CLIENT_ID.getMessage(), response.getMessage());
    }

    @Test
    @DisplayName("클라이언트 로고 이미지 수정 성공")
    void updateClientLogoImgSuccess() {
        // given
        Long clientId = 1L;
        MultipartFile mockFile = new MockMultipartFile("logo", "logo.png", "image/png", "test image content".getBytes());
        Client existingClient = new Client("Client", "http://example.com/logo.jpg", true);
        existingClient.setId(clientId);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));

        when(s3Adapter.uploadImage(any(MultipartFile.class)))
                .thenReturn(ApiResponse.ok("S3에 이미지 업로드 성공", "http://example.com/updated_logo.jpg"));

        when(clientRepository.save(any(Client.class))).thenReturn(existingClient);

        // when
        ApiResponse<Client> response = clientService.updateClientLogoImg(clientId, mockFile);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("클라이언트 로고 이미지를 성공적으로 수정했습니다.", response.getMessage());
        assertEquals("http://example.com/updated_logo.jpg", response.getData().getLogoImg()); // 로고 URL 확인
    }

    @Test
    @DisplayName("클라이언트 로고 이미지 수정 실패 - 클라이언트가 존재하지 않음")
    void updateClientLogoImgClientNotFound() {
        // given
        Long clientId = 1L;
        MultipartFile mockFile = new MockMultipartFile("logo", "logo.png", "image/png", "test image content".getBytes());

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        // when
        ApiResponse<Client> response = clientService.updateClientLogoImg(clientId, mockFile);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals(ErrorCode.INVALID_CLIENT_ID.getMessage(), response.getMessage());
    }

    @Test
    @DisplayName("Client 삭제 성공")
    void deleteClientSuccess() {
        // given
        Long clientId = 1L;
        Client clientToDelete = new Client("Client", "http://example.com/logo.jpg", true);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(clientToDelete));
//        when(s3Adapter.deleteFile(clientToDelete.getLogoImg())).thenReturn(ApiResponse.ok("S3에서 파일 삭제 성공"));

        // when
        ApiResponse<String> response = clientService.deleteClient(clientId);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("클라이언트를 성공적으로 삭제했습니다.", response.getMessage());

        verify(clientRepository).delete(clientToDelete);
    }

    @Test
    @DisplayName("Client 삭제 실패 - 유효하지 않은 ID")
    void deleteClientFail() {
        // given
        Long clientId = 1L;

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        // when
        ApiResponse<String> response = clientService.deleteClient(clientId);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals(ErrorCode.INVALID_CLIENT_ID.getMessage(), response.getMessage());

        verify(clientRepository, never()).delete(any(Client.class));
    }
}
