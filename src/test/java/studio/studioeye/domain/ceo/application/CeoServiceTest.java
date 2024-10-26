package studio.studioeye.domain.ceo.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import studio.studioeye.domain.ceo.dao.CeoRepository;
import studio.studioeye.domain.ceo.domain.Ceo;
import studio.studioeye.domain.ceo.dto.request.CreateCeoServiceRequestDto;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;
import studio.studioeye.infrastructure.s3.S3Adapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class CeoServiceTest {
    @InjectMocks
    private CeoService ceoService;

    @Mock
    private CeoRepository ceoRepository;
    @Mock
    private S3Adapter s3Adapter;

    // Mock MultipartFile 생성
    MockMultipartFile mockFile = new MockMultipartFile(
            "file",
            "testImage.jpg",
            "image/jpeg",
            "Test Image Content".getBytes()
    );

    @Test
    @DisplayName("Ceo 생성 성공 테스트")
    public void createCeoInformationSuccess() throws IOException {
        // given
        String name = "mingi";
        String introduction = "verygood!!!";
        CreateCeoServiceRequestDto dto = new CreateCeoServiceRequestDto(name, introduction);
        // Mock S3 upload 동작 설정
        when(s3Adapter.uploadFile(any(MultipartFile.class)))
                .thenReturn(ApiResponse.ok("S3 버킷에 이미지 업로드를 성공하였습니다.", "http://example.com/testImage.jpg"));
        // when
        ApiResponse<Ceo> response = ceoService.createCeoInformation(dto, mockFile);
        //then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("CEO 정보를 성공적으로 등록하였습니다.", response.getMessage());
    }
    @Test
    @DisplayName("Ceo 생성 실패 - 이미지 업로드 실패")
    public void createCeoFailDueToImageUpload() throws IOException {
        //given
        CreateCeoServiceRequestDto requestDto = new CreateCeoServiceRequestDto(
                "Test_Title",
                "Test_Content"
        );
        //stub
        when(s3Adapter.uploadFile(any(MultipartFile.class)))
                .thenReturn(ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT));
        //when
        ApiResponse<Ceo> response = ceoService.createCeoInformation(requestDto, mockFile);
        //then
        assertNotNull(response);
        assertEquals(ErrorCode.ERROR_S3_UPDATE_OBJECT.getStatus(), response.getStatus());
        assertEquals(ErrorCode.ERROR_S3_UPDATE_OBJECT.getMessage(), response.getMessage());
    }
    @Test
    @DisplayName("Ceo 전체 정보 조회 성공")
    void retrieveCeoInformationSuccess() {
        // given
        Ceo ceo = new Ceo("http://example.com/testImage.jpg", "testImage.jpg", "mingi", "CEO Introduction");
        when(ceoRepository.findAll()).thenReturn(List.of(ceo));

        // when
        ApiResponse<Ceo> response = ceoService.retrieveCeoInformation();

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("CEO 정보를 성공적으로 조회했습니다.", response.getMessage());
    }
    @Test
    @DisplayName("Ceo 전체 정보 조회 실패 - 데이터 없음")
    void retrieveCeoInformationFail_NoData() {
        // given
        when(ceoRepository.findAll()).thenReturn(new ArrayList<>());

        // when
        ApiResponse<Ceo> response = ceoService.retrieveCeoInformation();

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("CEO 정보가 존재하지 않습니다.", response.getMessage());
        assertNull(response.getData());
    }
}
