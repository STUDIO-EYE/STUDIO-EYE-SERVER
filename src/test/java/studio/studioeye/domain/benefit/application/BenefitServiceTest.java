package studio.studioeye.domain.benefit.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import studio.studioeye.domain.benefit.dao.BenefitRepository;
import studio.studioeye.domain.benefit.domain.Benefit;
import studio.studioeye.domain.benefit.dto.request.CreateBenefitServiceRequestDto;
import studio.studioeye.domain.benefit.dto.request.UpdateBenefitServiceRequestDto;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;
import studio.studioeye.infrastructure.s3.S3Adapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BenefitServiceTest {

    @InjectMocks
    private BenefitService benefitService;

    @Mock
    private BenefitRepository benefitRepository;
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
    @DisplayName("Benefit 생성 성공")
    public void createBenefitSuccess() throws IOException {
        //given
        CreateBenefitServiceRequestDto requestDto = new CreateBenefitServiceRequestDto(
                "Test_Title",
                "Test_Content"
        );

        // Mock S3 upload 동작 설정
        when(s3Adapter.uploadFile(any(MultipartFile.class)))
                .thenReturn(ApiResponse.ok("S3 버킷에 이미지 업로드를 성공하였습니다.", "http://example.com/testImage.jpg"));

        //when
        ApiResponse<Benefit> response = benefitService.createBenefit(requestDto, mockFile);

        //then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("혜택 정보를 성공적으로 등록하였습니다.", response.getMessage());
    }

    @Test
    @DisplayName("Benefit 생성 실패 - 이미지 업로드 실패")
    public void createBenefitFailDueToImageUpload() throws IOException {
        //given
        CreateBenefitServiceRequestDto requestDto = new CreateBenefitServiceRequestDto(
                "Test_Title",
                "Test_Content"
        );

        //stub
        when(s3Adapter.uploadFile(any(MultipartFile.class)))
                .thenReturn(ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT));

        //when
        ApiResponse<Benefit> response = benefitService.createBenefit(requestDto, mockFile);

        //then
        assertNotNull(response);
        assertEquals(ErrorCode.ERROR_S3_UPDATE_OBJECT.getStatus(), response.getStatus());
        assertEquals(ErrorCode.ERROR_S3_UPDATE_OBJECT.getMessage(), response.getMessage());
    }
}
