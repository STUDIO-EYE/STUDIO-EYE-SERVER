package studio.studioeye.domain.partner_information.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import studio.studioeye.domain.partner_information.dao.PartnerInformationRepository;
import studio.studioeye.domain.partner_information.domain.PartnerInformation;
import studio.studioeye.domain.partner_information.dto.request.CreatePartnerInfoServiceRequestDto;
import studio.studioeye.domain.partner_information.dto.request.UpdatePartnerInfoServiceRequestDto;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;
import studio.studioeye.infrastructure.s3.S3Adapter;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartnerInformationServiceTest {

    @InjectMocks
    private PartnerInformationService partnerInformationService;

    @Mock
    private PartnerInformationRepository partnerInformationRepository;

    @Mock
    private S3Adapter s3Adapter;

    MockMultipartFile mockFile = new MockMultipartFile(
            "file",
            "testImage.jpg",
            "image/jpeg",
            "Test Image Content".getBytes()
    );

    @Test
    @DisplayName("파트너 정보 생성 성공 테스트")
    void createPartnerInfoSuccess() throws IOException {
        // given
        CreatePartnerInfoServiceRequestDto requestDto = new CreatePartnerInfoServiceRequestDto(
                "PartnerName",
                true,
                "http://partner-link.com"
        );
        String logoImageStr = "logoImageUrl";
        // stub
        when(partnerInformationRepository.save(any(PartnerInformation.class)))
                .thenReturn(requestDto.toEntity(logoImageStr));
        // Mock S3 upload 동작 설정
        when(s3Adapter.uploadImage(any(MultipartFile.class)))
                .thenReturn(ApiResponse.ok("S3 버킷에 이미지 업로드를 성공하였습니다.", "http://example.com/testImage.jpg"));

        // when
        ApiResponse<PartnerInformation> response = partnerInformationService.createPartnerInfo(requestDto, mockFile);
        PartnerInformation partnerInfo = response.getData();

        // then
//        assertNotNull(partnerInfo);
        Assertions.assertThat(partnerInfo.getName()).isEqualTo(requestDto.name());
        Assertions.assertThat(partnerInfo.getIs_main()).isEqualTo(requestDto.is_main());
        Assertions.assertThat(partnerInfo.getLink()).isEqualTo(requestDto.link());
        Assertions.assertThat(partnerInfo.getLogoImageUrl()).isEqualTo(logoImageStr);
        // verify
        Mockito.verify(partnerInformationRepository, times(1)).save(any(PartnerInformation.class));
    }

//    @Test
//    @DisplayName("파트너 정보 생성 실패 테스트 - S3 업로드 실패")
//    void createPartnerInfoFailDueToS3UploadFailure() throws IOException {
//        // given
//        CreatePartnerInfoServiceRequestDto requestDto = new CreatePartnerInfoServiceRequestDto(
//                "PartnerName",
//                true,
//                "http://partner-link.com"
//        );
//        // Mock S3 upload failure
//        when(s3Adapter.uploadFile(any(MultipartFile.class)))
//                .thenReturn(ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT));
//        // when
//        ApiResponse<PartnerInformation> response = partnerInformationService.createPartnerInfo(requestDto, mockFile);
//
//        // then
//        assertNull(response.getData());
//        assertEquals(ErrorCode.ERROR_S3_UPDATE_OBJECT.getStatus(), response.getStatus());
//        assertEquals(ErrorCode.ERROR_S3_UPDATE_OBJECT.getMessage(), response.getMessage());
//
//        Mockito.verify(partnerInformationRepository, never()).save(any(PartnerInformation.class));
//    }

    @Test
    @DisplayName("협력사 정보 목록 조회 성공 테스트")
    void retrieveAllPartnerInfoSuccess() {
        PartnerInformation partnerInformation1 = new PartnerInformation("Logo1", "Partner1", true, "http://link1.com");
        PartnerInformation partnerInformation2 = new PartnerInformation("Logo2", "Partner2", false, "http://link2.com");
        partnerInformation1.setId(1L);
        partnerInformation2.setId(2L);
        // given
        List<PartnerInformation> partnerInformationList = new ArrayList<>();
        partnerInformationList.add(partnerInformation1);
        partnerInformationList.add(partnerInformation2);
        when(partnerInformationRepository.findAll()).thenReturn(partnerInformationList);

        // when
        ApiResponse<List<Map<String, Object>>> response = partnerInformationService.retrieveAllPartnerInfo();

        // then
        assertNotNull(response.getData());
        assertEquals(2, response.getData().size());
        assertEquals("협력사 정보 목록을 성공적으로 조회했습니다.", response.getMessage());
        verify(partnerInformationRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("협력사 정보 목록 조회 실패 테스트 - 데이터 없음")
    void retrieveAllPartnerInfoFail() {
        // given
        when(partnerInformationRepository.findAll()).thenReturn(Collections.emptyList());

        // when
        ApiResponse<List<Map<String, Object>>> response = partnerInformationService.retrieveAllPartnerInfo();

        // then
        assertTrue(response.getData() == null);
        assertEquals("협력사 정보가 존재하지 않습니다.", response.getMessage());
        verify(partnerInformationRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("협력사 정보 조회 성공 테스트")
    void retrievePartnerInfoSuccess() {
        // given
        Long partnerId = 1L;
        PartnerInformation partnerInformation = new PartnerInformation("Logo", "Partner", true, "http://link.com");
        partnerInformation.setId(partnerId);
        when(partnerInformationRepository.findById(partnerId)).thenReturn(Optional.of(partnerInformation));

        // when
        ApiResponse<Map<String, Object>> response = partnerInformationService.retrievePartnerInfo(partnerId);

        // then
        assertNotNull(response.getData());
        assertEquals("협력사 정보를 성공적으로 조회했습니다.", response.getMessage());
        verify(partnerInformationRepository, times(1)).findById(partnerId);
    }

    @Test
    @DisplayName("협력사 정보 조회 실패 테스트 - 잘못된 ID")
    void retrievePartnerInfoFail() {
        // given
        Long invalidId = 999L;
        // stub
        when(partnerInformationRepository.findById(invalidId)).thenReturn(Optional.empty());

        // when
        ApiResponse<Map<String, Object>> response = partnerInformationService.retrievePartnerInfo(invalidId);

        // then
        assertNull(response.getData());
        assertEquals(ErrorCode.INVALID_PARTNER_INFORMATION_ID.getStatus(), response.getStatus());
        verify(partnerInformationRepository, times(1)).findById(invalidId);
    }

    @Test
    @DisplayName("협력사 로고 이미지 리스트 조회 성공 테스트")
    void retrieveAllPartnerLogoImgListSuccess() {
        // given
        List<PartnerInformation> partnerList = List.of(
                new PartnerInformation("Logo1", "Partner1", true, "http://link1.com"),
                new PartnerInformation("Logo2", "Partner2", false, "http://link2.com")
        );
        // stub
        when(partnerInformationRepository.findAll()).thenReturn(partnerList);

        // when
        ApiResponse<List<String>> response = partnerInformationService.retrieveAllPartnerLogoImgList();

        // then
        assertNotNull(response.getData());
        assertEquals(2, response.getData().size());
        assertEquals("협력사 로고 이미지 리스트를 성공적으로 조회했습니다.", response.getMessage());
        verify(partnerInformationRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("협력사 로고 이미지 리스트 조회 실패 테스트 - 데이터 없음")
    void retrieveAllPartnerLogoImgListFail() {
        // given
        when(partnerInformationRepository.findAll()).thenReturn(Collections.emptyList());

        // when
        ApiResponse<List<String>> response = partnerInformationService.retrieveAllPartnerLogoImgList();

        // then
        assertTrue(response.getData() == null);
        assertEquals("협력사 정보가 존재하지 않습니다.", response.getMessage());
        verify(partnerInformationRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("협력사 정보 페이지 조회 성공 테스트")
    void retrievePartnerInformationPageSuccess() {
        // given
        int page = 0;
        int size = 2;
        Pageable pageable = PageRequest.of(page, size);
        List<PartnerInformation> partnerList = List.of(
                new PartnerInformation("Logo1", "Partner1", true, "http://link1.com"),
                new PartnerInformation("Logo2", "Partner2", false, "http://link2.com")
        );
        Page<PartnerInformation> partnerPage = new PageImpl<>(partnerList, pageable, partnerList.size());
        // stub
        when(partnerInformationRepository.findAll(pageable)).thenReturn(partnerPage);

        // when
        Page<PartnerInformation> resultPage = partnerInformationService.retrievePartnerInformationPage(page, size);

        // then
        assertNotNull(resultPage);
        assertEquals(2, resultPage.getContent().size());
        verify(partnerInformationRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("협력사 정보 페이지 조회 실패 테스트 - 빈 페이지")
    void retrievePartnerInformationPageFail() {
        // given
        int page = 0;
        int size = 2;
        Pageable pageable = PageRequest.of(page, size);
        Page<PartnerInformation> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        // stub
        when(partnerInformationRepository.findAll(pageable)).thenReturn(emptyPage);

        // when
        Page<PartnerInformation> resultPage = partnerInformationService.retrievePartnerInformationPage(page, size);

        // then
        assertNotNull(resultPage);
        assertTrue(resultPage.isEmpty());
        verify(partnerInformationRepository, times(1)).findAll(pageable);
    }

//    @Test
//    @DisplayName("협력사 정보 업데이트 성공 테스트")
//    void updatePartnerInfoSuccess() throws IOException {
//        // given
//        PartnerInformation partnerInformation = new PartnerInformation("Logo1", "Partner1", true, "http://link1.com");
//        UpdatePartnerInfoServiceRequestDto requestDto = new UpdatePartnerInfoServiceRequestDto(1L, "UpdatedName", true, "http://updated-link.com");
//        MockMultipartFile mockFile = new MockMultipartFile("file", "testImage.jpg", "image/jpeg", "Test Image Content".getBytes());
//        // stub
//        when(partnerInformationRepository.findById(1L)).thenReturn(Optional.of(partnerInformation));
//        when(s3Adapter.uploadFile(any(MultipartFile.class)))
//                .thenReturn(ApiResponse.ok("S3에 이미지 업로드 성공", "http://example.com/newTestImage.jpg"));
//        when(s3Adapter.deleteFile(anyString())).thenReturn(ApiResponse.ok("S3에서 기존 이미지 삭제 성공"));
//
//        // when
//        ApiResponse<PartnerInformation> response = partnerInformationService.updatePartnerInfo(requestDto, mockFile);
//
//        // then
//        assertEquals("UpdatedName", response.getData().getName());
//        assertEquals("http://example.com/newTestImage.jpg", response.getData().getLogoImageUrl());
//    }

    @Test
    @DisplayName("협력사 정보 업데이트 실패 테스트 - 유효하지 않은 협력사 ID")
    void updatePartnerInfoInvalidPartnerId() {
        // given
        UpdatePartnerInfoServiceRequestDto requestDto = new UpdatePartnerInfoServiceRequestDto(99L, "Name", true, "http://link.com");

        when(partnerInformationRepository.findById(99L)).thenReturn(Optional.empty());

        // when
        ApiResponse<PartnerInformation> response = partnerInformationService.updatePartnerInfo(requestDto, mockFile);

        // then
        assertEquals(ErrorCode.INVALID_PARTNER_INFORMATION_ID.getStatus(), response.getStatus());
        assertEquals(ErrorCode.INVALID_PARTNER_INFORMATION_ID.getMessage(), response.getMessage());
    }

//    @Test
//    @DisplayName("협력사 정보 업데이트 실패 테스트 - S3 이미지 업데이트 실패")
//    void updatePartnerInfoS3UpdateFail() throws IOException {
//        // given
//        PartnerInformation partnerInformation = new PartnerInformation("Logo1", "Partner1", true, "http://link1.com");
//        UpdatePartnerInfoServiceRequestDto requestDto = new UpdatePartnerInfoServiceRequestDto(1L, "UpdatedName", true, "http://updated-link.com");
//        MockMultipartFile mockFile = new MockMultipartFile("file", "testImage.jpg", "image/jpeg", "Test Image Content".getBytes());
//
//        when(partnerInformationRepository.findById(1L)).thenReturn(Optional.of(partnerInformation));
//        when(s3Adapter.uploadFile(any(MultipartFile.class)))
//                .thenReturn(ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT));
//
//        // when
//        ApiResponse<PartnerInformation> response = partnerInformationService.updatePartnerInfo(requestDto, mockFile);
//
//        // then
//        assertEquals(ErrorCode.ERROR_S3_UPDATE_OBJECT.getStatus(), response.getStatus());
//        assertEquals(ErrorCode.ERROR_S3_UPDATE_OBJECT.getMessage(), response.getMessage());
//    }

    @Test
    @DisplayName("협력사 텍스트 정보 업데이트 성공 테스트")
    void updatePartnerInfoTextSuccess() {
        // given
        PartnerInformation partnerInformation = new PartnerInformation("Logo1", "Partner1", true, "http://link1.com");
        UpdatePartnerInfoServiceRequestDto requestDto = new UpdatePartnerInfoServiceRequestDto(1L, "UpdatedName", true, "http://updated-link.com");

        when(partnerInformationRepository.findById(1L)).thenReturn(Optional.of(partnerInformation));
        when(partnerInformationRepository.save(any(PartnerInformation.class))).thenReturn(partnerInformation);

        // when
        ApiResponse<PartnerInformation> response = partnerInformationService.updatePartnerInfoText(requestDto);

        // then
        assertEquals("UpdatedName", response.getData().getName());
        assertEquals("http://updated-link.com", response.getData().getLink());
    }

    @Test
    @DisplayName("협력사 텍스트 정보 업데이트 실패 테스트 - 유효하지 않은 협력사 ID")
    void updatePartnerInfoTextFail() {
        // given
        UpdatePartnerInfoServiceRequestDto requestDto = new UpdatePartnerInfoServiceRequestDto(99L, "Name", true, "http://link.com");

        when(partnerInformationRepository.findById(99L)).thenReturn(Optional.empty());

        // when
        ApiResponse<PartnerInformation> response = partnerInformationService.updatePartnerInfoText(requestDto);

        // then
        assertEquals(ErrorCode.INVALID_PARTNER_INFORMATION_ID.getStatus(), response.getStatus());
        assertEquals(ErrorCode.INVALID_PARTNER_INFORMATION_ID.getMessage(), response.getMessage());
    }

//    @Test
//    @DisplayName("협력사 로고 이미지 업데이트 성공 테스트")
//    void updatePartnerLogoImgSuccess() throws IOException {
//        // given
//        PartnerInformation partnerInformation = new PartnerInformation("Logo1", "Partner1", true, "http://link1.com");
//        MockMultipartFile mockFile = new MockMultipartFile("file", "newTestImage.jpg", "image/jpeg", "New Test Image Content".getBytes());
//
//        when(partnerInformationRepository.findById(1L)).thenReturn(Optional.of(partnerInformation));
//        when(s3Adapter.uploadFile(any(MultipartFile.class)))
//                .thenReturn(ApiResponse.ok("S3에 이미지 업로드 성공", "http://example.com/newTestImage.jpg"));
//        when(partnerInformationRepository.save(any(PartnerInformation.class))).thenReturn(partnerInformation);
//
//        // when
//        ApiResponse<PartnerInformation> response = partnerInformationService.updatePartnerLogoImg(1L, mockFile);
//
//        // then
//        assertEquals("http://example.com/newTestImage.jpg", response.getData().getLogoImageUrl());
//    }

    @Test
    @DisplayName("협력사 로고 이미지 업데이트 실패 테스트 - 유효하지 않은 협력사 ID")
    void updatePartnerLogoImgInvalidPartnerId() {
        // given
        when(partnerInformationRepository.findById(99L)).thenReturn(Optional.empty());

        // when
        ApiResponse<PartnerInformation> response = partnerInformationService.updatePartnerLogoImg(99L, mockFile);

        // then
        assertEquals(ErrorCode.INVALID_PARTNER_INFORMATION_ID.getStatus(), response.getStatus());
        assertEquals(ErrorCode.INVALID_PARTNER_INFORMATION_ID.getMessage(), response.getMessage());
    }

//    @Test
//    @DisplayName("협력사 로고 이미지 업데이트 실패 테스트 - S3 이미지 업데이트 실패")
//    void updatePartnerLogoImgS3UpdateFail() throws IOException {
//        // given
//        PartnerInformation partnerInformation = new PartnerInformation("Logo1", "Partner1", true, "http://link1.com");
//        when(partnerInformationRepository.findById(1L)).thenReturn(Optional.of(partnerInformation));
//        when(s3Adapter.uploadFile(any(MultipartFile.class)))
//                .thenReturn(ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT));
//
//        // when
//        ApiResponse<PartnerInformation> response = partnerInformationService.updatePartnerLogoImg(1L, mockFile);
//
//        // then
//        assertEquals(ErrorCode.ERROR_S3_UPDATE_OBJECT.getStatus(), response.getStatus());
//        assertEquals(ErrorCode.ERROR_S3_UPDATE_OBJECT.getMessage(), response.getMessage());
//    }

//    @Test
//    @DisplayName("협력사 정보 삭제 성공 테스트")
//    void deletePartnerInfoSuccess() {
//        // given
//        PartnerInformation partnerInformation = new PartnerInformation("Logo1", "Partner1", true, "http://link1.com");
//        when(partnerInformationRepository.findById(1L)).thenReturn(Optional.of(partnerInformation));
//        when(s3Adapter.deleteFile(anyString())).thenReturn(ApiResponse.ok("S3에서 이미지 삭제 성공"));
//
//        // when
//        ApiResponse<String> response = partnerInformationService.deletePartnerInfo(1L);
//
//        // then
//        assertEquals("협력사 정보를 성공적으로 삭제하였습니다.", response.getMessage());
//    }

    @Test
    @DisplayName("협력사 정보 삭제 실패 테스트 - 유효하지 않은 협력사 ID")
    void deletePartnerInfoInvalidPartnerId() {
        // given
        when(partnerInformationRepository.findById(99L)).thenReturn(Optional.empty());

        // when
        ApiResponse<String> response = partnerInformationService.deletePartnerInfo(99L);

        // then
        assertEquals(ErrorCode.INVALID_PARTNER_INFORMATION_ID.getStatus(), response.getStatus());
        assertEquals(ErrorCode.INVALID_PARTNER_INFORMATION_ID.getMessage(), response.getMessage());
    }
}