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

    @Test
    @DisplayName("Benefit 전체 조회 성공 테스트")
    void retrieveBenefitSuccess() {
        // given
        List<Benefit> benefitList = new ArrayList<>();
        benefitList.add(new Benefit("Test ImageUrl1", "Test ImageFileName1", "Test Title1", "Test Content1"));
        benefitList.add(new Benefit("Test ImageUrl2", "Test ImageFileName2", "Test Title2", "Test Content2"));
        benefitList.add(new Benefit("Test ImageUrl3", "Test ImageFileName3", "Test Title3", "Test Content3"));

        List<Benefit> savedBenefit = benefitList;

        // stub
        when(benefitRepository.findAll()).thenReturn(savedBenefit);

        // when
        ApiResponse<List<Benefit>> response = benefitService.retrieveBenefit();
        List<Benefit> findBenefit = response.getData();

        // then
        Assertions.assertThat(findBenefit).isEqualTo(savedBenefit);
        Assertions.assertThat(findBenefit.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("Benefit 전체 조회 실패 테스트 - 데이터 없음")
    void retrieveNewsByIdFail() {
        // given
        List<Benefit> benefitList = new ArrayList<>();

        // stub
        when(benefitRepository.findAll()).thenReturn(benefitList);

        // when
        ApiResponse<List<Benefit>> response = benefitService.retrieveBenefit();

        // then
        Assertions.assertThat(response.getData()).isNull();
        Assertions.assertThat(response.getMessage()).isEqualTo("혜택 정보가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("Benefit 수정 성공 테스트")
    void updateNewsSuccess() throws IOException {
        // given
        Long id = 1L;
        UpdateBenefitServiceRequestDto requestDto = new UpdateBenefitServiceRequestDto(
                id, "Updated_Title","Updated_Content");
        Benefit savedBenefit = new Benefit("Test ImageUrl1", "Test ImageFileName1", "Test Title1", "Test Content1");

        // stub
        when(benefitRepository.findById(requestDto.id())).thenReturn(Optional.of(savedBenefit));
        when(s3Adapter.deleteFile(savedBenefit.getImageFileName())).thenReturn(ApiResponse.ok("S3에서 파일 삭제 성공"));
        when(s3Adapter.uploadFile(any(MultipartFile.class)))
                .thenReturn(ApiResponse.ok("S3에 이미지 업로드 성공", "Updated Test ImageUrl"));
        when(benefitRepository.save(any(Benefit.class))).thenReturn(savedBenefit);

        //when
        ApiResponse<Benefit> response = benefitService.updateBenefit(requestDto, mockFile);

        //then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("혜택 정보를 성공적으로 수정했습니다.", response.getMessage());
        assertEquals("Updated Test ImageUrl", savedBenefit.getImageUrl());
    }

}
