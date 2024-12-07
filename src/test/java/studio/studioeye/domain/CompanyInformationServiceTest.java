package studio.studioeye.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import studio.studioeye.domain.company_information.application.CompanyInformationService;
import studio.studioeye.domain.company_information.dao.CompanyBasicInformation;
import studio.studioeye.domain.company_information.dao.CompanyInformationRepository;
import studio.studioeye.domain.company_information.dao.CompanyIntroductionInformation;
import studio.studioeye.domain.company_information.dao.CompanyIntroductionInformationImpl;
import studio.studioeye.domain.company_information.domain.CompanyInformation;
import studio.studioeye.domain.company_information.domain.CompanyInformationDetailInformation;
import studio.studioeye.domain.company_information.dto.request.*;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;
import studio.studioeye.infrastructure.s3.S3Adapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyInformationServiceTest {

    @InjectMocks
    private CompanyInformationService companyInformationService;

    @Mock
    private CompanyInformationRepository companyInformationRepository;
    @Mock
    private S3Adapter s3Adapter;

    private final MockMultipartFile mockFile = new MockMultipartFile(
            "file",
            "testLogo.jpg",
            "image/jpeg",
            "Test Logo Content".getBytes()
    );


    @Test
    @DisplayName("회사 정보 등록 성공 테스트")
    void createCompanyInformationSuccess() throws IOException {
        // given
        String mainOverview = "Test mainOverview";
        String commitment = "Test commitment";
        String address = "Test address";
        String addressEnglish = "Test addressEnglish";
        String phone = "Test phone";
        String fax = "Test fax";
        String introduction = "Test introduction";

        List<DetailInformationDTO> detailInformation = new ArrayList<>();

        DetailInformationDTO dto1 = new DetailInformationDTO();
        dto1.setKey("Test Key1");
        dto1.setValue("Test Value1");
        detailInformation.add(dto1);

        DetailInformationDTO dto2 = new DetailInformationDTO();
        dto2.setKey("Test Key2");
        dto2.setValue("Test Value2");
        detailInformation.add(dto2);

        DetailInformationDTO dto3 = new DetailInformationDTO();
        dto3.setKey("Test Key3");
        dto3.setValue("Test Value3");
        detailInformation.add(dto3);

        CreateCompanyInformationServiceRequestDto requestDto = new CreateCompanyInformationServiceRequestDto(
                mainOverview, commitment, address, addressEnglish, phone, fax, introduction, detailInformation
        );

        // stub
        // Mock S3 upload 동작 설정
        when(s3Adapter.uploadFile(any(MultipartFile.class)))
                .thenReturn(ApiResponse.ok("S3 버킷에 이미지 업로드를 성공하였습니다.", "http://example.com/testImage.jpg"));

        // when
        ApiResponse<CompanyInformation> response = companyInformationService.createCompanyInformation(requestDto, mockFile, mockFile, mockFile);
        CompanyInformation findCompanyInformation = response.getData();

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("회사 정보를 성공적으로 등록하였습니다.", response.getMessage());
        Mockito.verify(companyInformationRepository, times(1)).save(any(CompanyInformation.class));
    }

    @Test
    @DisplayName("회사 정보 등록 실패 테스트")
    void createCompanyInformationFail() throws IOException {
        // given
        String mainOverview = "Test mainOverview";
        String commitment = "Test commitment";
        String address = "Test address";
        String addressEnglish = "Test addressEnglish";
        String phone = "Test phone";
        String fax = "Test fax";
        String introduction = "Test introduction";

        List<DetailInformationDTO> detailInformation = new ArrayList<>();

        DetailInformationDTO dto1 = new DetailInformationDTO();
        dto1.setKey("Test Key1");
        dto1.setValue("Test Value1");
        detailInformation.add(dto1);

        DetailInformationDTO dto2 = new DetailInformationDTO();
        dto2.setKey("Test Key2");
        dto2.setValue("Test Value2");
        detailInformation.add(dto2);

        DetailInformationDTO dto3 = new DetailInformationDTO();
        dto3.setKey("Test Key3");
        dto3.setValue("Test Value3");
        detailInformation.add(dto3);

        CreateCompanyInformationServiceRequestDto requestDto = new CreateCompanyInformationServiceRequestDto(
                mainOverview, commitment, address, addressEnglish, phone, fax, introduction, detailInformation
        );

        // when
        ApiResponse<CompanyInformation> response = companyInformationService.createCompanyInformation(requestDto, null, mockFile, mockFile);

        // then
        assertNotNull(response);
        assertEquals(ErrorCode.NOT_EXIST_IMAGE_FILE.getStatus(), response.getStatus());
        assertEquals(ErrorCode.NOT_EXIST_IMAGE_FILE.getMessage(), response.getMessage());
        Mockito.verify(companyInformationRepository, never()).save(any(CompanyInformation.class));
    }

    @Test
    @DisplayName("회사 전체 정보 수정 성공 테스트")
    void updateAllCompanyInformationSuccess() throws IOException {
        // given
        String mainOverview = "Test mainOverview";
        String commitment = "Test commitment";
        String address = "Test address";
        String addressEnglish = "Test addressEnglish";
        String phone = "Test phone";
        String fax = "Test fax";
        String introduction = "Test introduction";

        List<DetailInformationDTO> detailInformation = new ArrayList<>();

        DetailInformationDTO dto1 = new DetailInformationDTO();
        dto1.setKey("Test Key1");
        dto1.setValue("Test Value1");
        detailInformation.add(dto1);

        DetailInformationDTO dto2 = new DetailInformationDTO();
        dto2.setKey("Test Key2");
        dto2.setValue("Test Value2");
        detailInformation.add(dto2);

        DetailInformationDTO dto3 = new DetailInformationDTO();
        dto3.setKey("Test Key3");
        dto3.setValue("Test Value3");
        detailInformation.add(dto3);

        UpdateAllCompanyInformationServiceRequestDto requestDto = new UpdateAllCompanyInformationServiceRequestDto(
                mainOverview, commitment, address, addressEnglish, phone, fax, introduction, detailInformation
        );

        List<CompanyInformation> savedCompanyInformationList = new ArrayList<>();

        CompanyInformation savedCompanyInformation = CompanyInformation.builder()
                .mainOverview("Test")
                .commitment("Test")
                .address("Test")
                .addressEnglish("Test")
                .phone("Test")
                .fax("Test")
                .introduction("Test")
                .lightLogoImageFileName("Test")
                .lightLogoImageUrl("Test")
                .darkLogoImageFileName("Test")
                .darkLogoImageUrl("Test")
                .sloganImageFileName("Test")
                .sloganImageUrl("Test")
                .build();

        List<CompanyInformationDetailInformation> savedDetailInformation = new ArrayList<>();

        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key1", "Test Value1"));
        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key2", "Test Value2"));
        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key3", "Test Value3"));

        savedCompanyInformation.initDetailInformation(savedDetailInformation);

        savedCompanyInformationList.add(savedCompanyInformation);

        // stub
        when(companyInformationRepository.findAll()).thenReturn(savedCompanyInformationList);
        // Mock S3 upload 동작 설정
        when(s3Adapter.uploadFile(any(MultipartFile.class)))
                .thenReturn(ApiResponse.ok("S3 버킷에 이미지 업로드를 성공하였습니다.", "http://example.com/testImage.jpg"));

        // when
        ApiResponse<CompanyInformation> response = companyInformationService.updateAllCompanyInformation(requestDto, mockFile, mockFile, mockFile);
        CompanyInformation findCompanyInformation = response.getData();

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("전체 회사 정보를 성공적으로 수정했습니다.", response.getMessage());
        Mockito.verify(companyInformationRepository, times(1)).save(any(CompanyInformation.class));
    }

    @Test
    @DisplayName("회사 전체 정보 수정 실패 테스트")
    void updateAllCompanyInformationFail() throws IOException {
        // given
        String mainOverview = "Test mainOverview";
        String commitment = "Test commitment";
        String address = "Test address";
        String addressEnglish = "Test addressEnglish";
        String phone = "Test phone";
        String fax = "Test fax";
        String introduction = "Test introduction";

        List<DetailInformationDTO> detailInformation = new ArrayList<>();

        DetailInformationDTO dto1 = new DetailInformationDTO();
        dto1.setKey("Test Key1");
        dto1.setValue("Test Value1");
        detailInformation.add(dto1);

        DetailInformationDTO dto2 = new DetailInformationDTO();
        dto2.setKey("Test Key2");
        dto2.setValue("Test Value2");
        detailInformation.add(dto2);

        DetailInformationDTO dto3 = new DetailInformationDTO();
        dto3.setKey("Test Key3");
        dto3.setValue("Test Value3");
        detailInformation.add(dto3);

        UpdateAllCompanyInformationServiceRequestDto requestDto = new UpdateAllCompanyInformationServiceRequestDto(
                mainOverview, commitment, address, addressEnglish, phone, fax, introduction, detailInformation
        );

        // stub
        when(companyInformationRepository.findAll()).thenReturn(List.of());

        // when
        ApiResponse<CompanyInformation> response = companyInformationService.updateAllCompanyInformation(requestDto, mockFile, mockFile, mockFile);

        // then
        assertNotNull(response);
        assertEquals(ErrorCode.COMPANYINFORMATION_IS_EMPTY.getStatus(), response.getStatus());
        assertEquals(ErrorCode.COMPANYINFORMATION_IS_EMPTY.getMessage(), response.getMessage());
        Mockito.verify(companyInformationRepository, times(1)).findAll();
        Mockito.verify(companyInformationRepository, never()).save(any(CompanyInformation.class));
    }

    @Test
    @DisplayName("회사 전체 텍스트 정보(이미지 제외) 수정 성공 테스트")
    void updateAllCompanyTextInformationSuccess() {
        // given
        String mainOverview = "Test mainOverview";
        String commitment = "Test commitment";
        String address = "Test address";
        String addressEnglish = "Test addressEnglish";
        String phone = "Test phone";
        String fax = "Test fax";
        String introduction = "Test introduction";

        List<DetailInformationDTO> detailInformation = new ArrayList<>();

        DetailInformationDTO dto1 = new DetailInformationDTO();
        dto1.setKey("Test Key1");
        dto1.setValue("Test Value1");
        detailInformation.add(dto1);

        DetailInformationDTO dto2 = new DetailInformationDTO();
        dto2.setKey("Test Key2");
        dto2.setValue("Test Value2");
        detailInformation.add(dto2);

        DetailInformationDTO dto3 = new DetailInformationDTO();
        dto3.setKey("Test Key3");
        dto3.setValue("Test Value3");
        detailInformation.add(dto3);

        UpdateAllCompanyInformationServiceRequestDto requestDto = new UpdateAllCompanyInformationServiceRequestDto(
                mainOverview, commitment, address, addressEnglish, phone, fax, introduction, detailInformation
        );

        List<CompanyInformation> savedCompanyInformationList = new ArrayList<>();

        CompanyInformation savedCompanyInformation = CompanyInformation.builder()
                .mainOverview("Test")
                .commitment("Test")
                .address("Test")
                .addressEnglish("Test")
                .phone("Test")
                .fax("Test")
                .introduction("Test")
                .lightLogoImageFileName("Test")
                .lightLogoImageUrl("Test")
                .darkLogoImageFileName("Test")
                .darkLogoImageUrl("Test")
                .sloganImageFileName("Test")
                .sloganImageUrl("Test")
                .build();

        List<CompanyInformationDetailInformation> savedDetailInformation = new ArrayList<>();

        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key1", "Test Value1"));
        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key2", "Test Value2"));
        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key3", "Test Value3"));

        savedCompanyInformation.initDetailInformation(savedDetailInformation);

        savedCompanyInformationList.add(savedCompanyInformation);

        // stub
        when(companyInformationRepository.findAll()).thenReturn(savedCompanyInformationList);

        // when
        ApiResponse<CompanyInformation> response = companyInformationService.updateAllCompanyTextInformation(requestDto);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("전체 회사 정보를 성공적으로 수정했습니다.", response.getMessage());
        Mockito.verify(companyInformationRepository, times(1)).save(any(CompanyInformation.class));
    }

    @Test
    @DisplayName("회사 전체 텍스트 정보(이미지 제외) 수정 실패 테스트")
    void updateAllCompanyTextInformationFail() {
        // given
        String mainOverview = "Test mainOverview";
        String commitment = "Test commitment";
        String address = "Test address";
        String addressEnglish = "Test addressEnglish";
        String phone = "Test phone";
        String fax = "Test fax";
        String introduction = "Test introduction";

        List<DetailInformationDTO> detailInformation = new ArrayList<>();

        DetailInformationDTO dto1 = new DetailInformationDTO();
        dto1.setKey("Test Key1");
        dto1.setValue("Test Value1");
        detailInformation.add(dto1);

        DetailInformationDTO dto2 = new DetailInformationDTO();
        dto2.setKey("Test Key2");
        dto2.setValue("Test Value2");
        detailInformation.add(dto2);

        DetailInformationDTO dto3 = new DetailInformationDTO();
        dto3.setKey("Test Key3");
        dto3.setValue("Test Value3");
        detailInformation.add(dto3);

        UpdateAllCompanyInformationServiceRequestDto requestDto = new UpdateAllCompanyInformationServiceRequestDto(
                mainOverview, commitment, address, addressEnglish, phone, fax, introduction, detailInformation
        );

        // stub
        when(companyInformationRepository.findAll()).thenReturn(List.of());

        // when
        ApiResponse<CompanyInformation> response = companyInformationService.updateAllCompanyTextInformation(requestDto);

        // then
        assertNotNull(response);
        assertEquals(ErrorCode.COMPANYINFORMATION_IS_EMPTY.getStatus(), response.getStatus());
        assertEquals(ErrorCode.COMPANYINFORMATION_IS_EMPTY.getMessage(), response.getMessage());
        Mockito.verify(companyInformationRepository, times(1)).findAll();
        Mockito.verify(companyInformationRepository, never()).save(any(CompanyInformation.class));
    }

    @Test
    @DisplayName("회사 로고 이미지 수정 성공 테스트")
    void updateCompanyLogoImageSuccess() throws IOException {
        // given
        List<CompanyInformation> savedCompanyInformationList = new ArrayList<>();

        CompanyInformation savedCompanyInformation = CompanyInformation.builder()
                .mainOverview("Test")
                .commitment("Test")
                .address("Test")
                .addressEnglish("Test")
                .phone("Test")
                .fax("Test")
                .introduction("Test")
                .lightLogoImageFileName("Test")
                .lightLogoImageUrl("Test")
                .darkLogoImageFileName("Test")
                .darkLogoImageUrl("Test")
                .sloganImageFileName("Test")
                .sloganImageUrl("Test")
                .build();

        List<CompanyInformationDetailInformation> savedDetailInformation = new ArrayList<>();

        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key1", "Test Value1"));
        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key2", "Test Value2"));
        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key3", "Test Value3"));

        savedCompanyInformation.initDetailInformation(savedDetailInformation);

        savedCompanyInformationList.add(savedCompanyInformation);

        // stub
        when(companyInformationRepository.findAll()).thenReturn(savedCompanyInformationList);
        // Mock S3 upload 동작 설정
        when(s3Adapter.uploadFile(any(MultipartFile.class)))
                .thenReturn(ApiResponse.ok("S3 버킷에 이미지 업로드를 성공하였습니다.", "http://example.com/testImage.jpg"));

        // when
        ApiResponse<CompanyInformation> response = companyInformationService.updateCompanyLogoImage(mockFile, mockFile);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("회사 로고 이미지를 성공적으로 수정했습니다.", response.getMessage());
        Mockito.verify(companyInformationRepository, times(1)).save(any(CompanyInformation.class));
    }

    @Test
    @DisplayName("회사 로고 이미지 수정 실패 테스트 - 로고 이미지가 없는 경우")
    void updateCompanyLogoImageFail_invalidFile() throws IOException {
        // given
        List<CompanyInformation> savedCompanyInformationList = new ArrayList<>();

        CompanyInformation savedCompanyInformation = CompanyInformation.builder()
                .mainOverview("Test")
                .commitment("Test")
                .address("Test")
                .addressEnglish("Test")
                .phone("Test")
                .fax("Test")
                .introduction("Test")
                .lightLogoImageFileName("Test")
                .lightLogoImageUrl("Test")
                .darkLogoImageFileName("Test")
                .darkLogoImageUrl("Test")
                .sloganImageFileName("Test")
                .sloganImageUrl("Test")
                .build();

        List<CompanyInformationDetailInformation> savedDetailInformation = new ArrayList<>();

        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key1", "Test Value1"));
        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key2", "Test Value2"));
        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key3", "Test Value3"));

        savedCompanyInformation.initDetailInformation(savedDetailInformation);

        savedCompanyInformationList.add(savedCompanyInformation);

        // when
        ApiResponse<CompanyInformation> response = companyInformationService.updateCompanyLogoImage(null, null);

        // then
        assertNotNull(response);
        assertEquals(ErrorCode.NOT_EXIST_IMAGE_FILE.getStatus(), response.getStatus());
        assertEquals(ErrorCode.NOT_EXIST_IMAGE_FILE.getMessage(), response.getMessage());
        Mockito.verify(companyInformationRepository, never()).findAll();
        Mockito.verify(companyInformationRepository, never()).save(any(CompanyInformation.class));
    }

    @Test
    @DisplayName("회사 로고 이미지 수정 실패 테스트 - 회사 데이터가 없는 경우")
    void updateCompanyLogoImageFail_notFound() throws IOException {
        // given
        List<CompanyInformation> savedCompanyInformationList = new ArrayList<>();

        CompanyInformation savedCompanyInformation = CompanyInformation.builder()
                .mainOverview("Test")
                .commitment("Test")
                .address("Test")
                .addressEnglish("Test")
                .phone("Test")
                .fax("Test")
                .introduction("Test")
                .lightLogoImageFileName("Test")
                .lightLogoImageUrl("Test")
                .darkLogoImageFileName("Test")
                .darkLogoImageUrl("Test")
                .sloganImageFileName("Test")
                .sloganImageUrl("Test")
                .build();

        List<CompanyInformationDetailInformation> savedDetailInformation = new ArrayList<>();

        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key1", "Test Value1"));
        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key2", "Test Value2"));
        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key3", "Test Value3"));

        savedCompanyInformation.initDetailInformation(savedDetailInformation);

        savedCompanyInformationList.add(savedCompanyInformation);

        // stub
        when(companyInformationRepository.findAll()).thenReturn(List.of());

        // when
        ApiResponse<CompanyInformation> response = companyInformationService.updateCompanyLogoImage(mockFile, mockFile);

        // then
        assertNotNull(response);
        assertEquals(ErrorCode.COMPANYINFORMATION_IS_EMPTY.getStatus(), response.getStatus());
        assertEquals(ErrorCode.COMPANYINFORMATION_IS_EMPTY.getMessage(), response.getMessage());
        Mockito.verify(companyInformationRepository, times(1)).findAll();
        Mockito.verify(companyInformationRepository, never()).save(any(CompanyInformation.class));
    }

    @Test
    @DisplayName("회사 슬로건 이미지 수정 성공 테스트")
    void updateCompanySloganImageSuccess() throws IOException {
        // given
        List<CompanyInformation> savedCompanyInformationList = new ArrayList<>();

        CompanyInformation savedCompanyInformation = CompanyInformation.builder()
                .mainOverview("Test")
                .commitment("Test")
                .address("Test")
                .addressEnglish("Test")
                .phone("Test")
                .fax("Test")
                .introduction("Test")
                .lightLogoImageFileName("Test")
                .lightLogoImageUrl("Test")
                .darkLogoImageFileName("Test")
                .darkLogoImageUrl("Test")
                .sloganImageFileName("Test")
                .sloganImageUrl("Test")
                .build();

        List<CompanyInformationDetailInformation> savedDetailInformation = new ArrayList<>();

        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key1", "Test Value1"));
        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key2", "Test Value2"));
        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key3", "Test Value3"));

        savedCompanyInformation.initDetailInformation(savedDetailInformation);

        savedCompanyInformationList.add(savedCompanyInformation);

        // stub
        when(companyInformationRepository.findAll()).thenReturn(savedCompanyInformationList);
        // Mock S3 upload 동작 설정
        when(s3Adapter.uploadFile(any(MultipartFile.class)))
                .thenReturn(ApiResponse.ok("S3 버킷에 이미지 업로드를 성공하였습니다.", "http://example.com/testImage.jpg"));

        // when
        ApiResponse<CompanyInformation> response = companyInformationService.updateCompanySloganImage(mockFile);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("회사 슬로건 이미지를 성공적으로 수정했습니다.", response.getMessage());
        Mockito.verify(companyInformationRepository, times(1)).save(any(CompanyInformation.class));
    }

    @Test
    @DisplayName("회사 슬로건 이미지 수정 실패 테스트 - 슬로건 이미지가 없는 경우")
    void updateCompanySloganImageFail_invalidFile() throws IOException {
        // given
        List<CompanyInformation> savedCompanyInformationList = new ArrayList<>();

        CompanyInformation savedCompanyInformation = CompanyInformation.builder()
                .mainOverview("Test")
                .commitment("Test")
                .address("Test")
                .addressEnglish("Test")
                .phone("Test")
                .fax("Test")
                .introduction("Test")
                .lightLogoImageFileName("Test")
                .lightLogoImageUrl("Test")
                .darkLogoImageFileName("Test")
                .darkLogoImageUrl("Test")
                .sloganImageFileName("Test")
                .sloganImageUrl("Test")
                .build();

        List<CompanyInformationDetailInformation> savedDetailInformation = new ArrayList<>();

        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key1", "Test Value1"));
        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key2", "Test Value2"));
        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key3", "Test Value3"));

        savedCompanyInformation.initDetailInformation(savedDetailInformation);

        savedCompanyInformationList.add(savedCompanyInformation);

        // when
        ApiResponse<CompanyInformation> response = companyInformationService.updateCompanySloganImage(null);

        // then
        assertNotNull(response);
        assertEquals(ErrorCode.NOT_EXIST_IMAGE_FILE.getStatus(), response.getStatus());
        assertEquals(ErrorCode.NOT_EXIST_IMAGE_FILE.getMessage(), response.getMessage());
        Mockito.verify(companyInformationRepository, never()).findAll();
        Mockito.verify(companyInformationRepository, never()).save(any(CompanyInformation.class));
    }

    @Test
    @DisplayName("회사 슬로건 이미지 수정 실패 테스트 - 회사 데이터가 없는 경우")
    void updateCompanySloganImageFail_notFound() throws IOException {
        // given
        List<CompanyInformation> savedCompanyInformationList = new ArrayList<>();

        CompanyInformation savedCompanyInformation = CompanyInformation.builder()
                .mainOverview("Test")
                .commitment("Test")
                .address("Test")
                .addressEnglish("Test")
                .phone("Test")
                .fax("Test")
                .introduction("Test")
                .lightLogoImageFileName("Test")
                .lightLogoImageUrl("Test")
                .darkLogoImageFileName("Test")
                .darkLogoImageUrl("Test")
                .sloganImageFileName("Test")
                .sloganImageUrl("Test")
                .build();

        List<CompanyInformationDetailInformation> savedDetailInformation = new ArrayList<>();

        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key1", "Test Value1"));
        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key2", "Test Value2"));
        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key3", "Test Value3"));

        savedCompanyInformation.initDetailInformation(savedDetailInformation);

        savedCompanyInformationList.add(savedCompanyInformation);

        // stub
        when(companyInformationRepository.findAll()).thenReturn(List.of());

        // when
        ApiResponse<CompanyInformation> response = companyInformationService.updateCompanySloganImage(mockFile);

        // then
        assertNotNull(response);
        assertEquals(ErrorCode.COMPANYINFORMATION_IS_EMPTY.getStatus(), response.getStatus());
        assertEquals(ErrorCode.COMPANYINFORMATION_IS_EMPTY.getMessage(), response.getMessage());
        Mockito.verify(companyInformationRepository, times(1)).findAll();
        Mockito.verify(companyInformationRepository, never()).save(any(CompanyInformation.class));
    }

    @Test
    @DisplayName("회사 로고, 슬로건 이미지 수정 성공 테스트")
    void updateCompanyLogoAndSloganSuccess() throws IOException {
        // given
        List<CompanyInformation> savedCompanyInformationList = new ArrayList<>();

        CompanyInformation savedCompanyInformation = CompanyInformation.builder()
                .mainOverview("Test")
                .commitment("Test")
                .address("Test")
                .addressEnglish("Test")
                .phone("Test")
                .fax("Test")
                .introduction("Test")
                .lightLogoImageFileName("Test")
                .lightLogoImageUrl("Test")
                .darkLogoImageFileName("Test")
                .darkLogoImageUrl("Test")
                .sloganImageFileName("Test")
                .sloganImageUrl("Test")
                .build();

        List<CompanyInformationDetailInformation> savedDetailInformation = new ArrayList<>();

        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key1", "Test Value1"));
        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key2", "Test Value2"));
        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key3", "Test Value3"));

        savedCompanyInformation.initDetailInformation(savedDetailInformation);

        savedCompanyInformationList.add(savedCompanyInformation);

        // stub
        when(companyInformationRepository.findAll()).thenReturn(savedCompanyInformationList);
        // Mock S3 upload 동작 설정
        when(s3Adapter.uploadFile(any(MultipartFile.class)))
                .thenReturn(ApiResponse.ok("S3 버킷에 이미지 업로드를 성공하였습니다.", "http://example.com/testImage.jpg"));

        // when
        ApiResponse<CompanyInformation> response = companyInformationService.updateCompanyLogoAndSlogan(mockFile, mockFile, mockFile);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("회사 로고 이미지와 슬로건 이미지를 성공적으로 수정했습니다.", response.getMessage());
        Mockito.verify(companyInformationRepository, times(1)).save(any(CompanyInformation.class));
    }

    @Test
    @DisplayName("회사 로고, 슬로건 이미지 수정 실패 테스트 - 회사 데이터가 없는 경우")
    void updateCompanyLogoAndSloganFail_notFound() throws IOException {
        // given
        List<CompanyInformation> savedCompanyInformationList = new ArrayList<>();

        CompanyInformation savedCompanyInformation = CompanyInformation.builder()
                .mainOverview("Test")
                .commitment("Test")
                .address("Test")
                .addressEnglish("Test")
                .phone("Test")
                .fax("Test")
                .introduction("Test")
                .lightLogoImageFileName("Test")
                .lightLogoImageUrl("Test")
                .darkLogoImageFileName("Test")
                .darkLogoImageUrl("Test")
                .sloganImageFileName("Test")
                .sloganImageUrl("Test")
                .build();

        List<CompanyInformationDetailInformation> savedDetailInformation = new ArrayList<>();

        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key1", "Test Value1"));
        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key2", "Test Value2"));
        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key3", "Test Value3"));

        savedCompanyInformation.initDetailInformation(savedDetailInformation);

        savedCompanyInformationList.add(savedCompanyInformation);

        // stub
        when(companyInformationRepository.findAll()).thenReturn(List.of());

        // when
        ApiResponse<CompanyInformation> response = companyInformationService.updateCompanyLogoAndSlogan(mockFile, mockFile, mockFile);

        // then
        assertNotNull(response);
        assertEquals(ErrorCode.COMPANYINFORMATION_IS_EMPTY.getStatus(), response.getStatus());
        assertEquals(ErrorCode.COMPANYINFORMATION_IS_EMPTY.getMessage(), response.getMessage());
        Mockito.verify(companyInformationRepository, times(1)).findAll();
        Mockito.verify(companyInformationRepository, never()).save(any(CompanyInformation.class));
    }

    @Test
    @DisplayName("회사 기본 정보(주소, 유선번호, 팩스번호) 수정 성공 테스트")
    void updateCompanyBasicInformationSuccess() {
        // given
        String address = "Test address";
        String addressEnglish = "Test addressEnglish";
        String phone = "Test phone";
        String fax = "Test fax";

        UpdateCompanyBasicInformationServiceRequestDto requestDto = new UpdateCompanyBasicInformationServiceRequestDto(
                address, addressEnglish, phone, fax
        );

        List<CompanyInformation> savedCompanyInformationList = new ArrayList<>();

        CompanyInformation savedCompanyInformation = CompanyInformation.builder()
                .mainOverview("Test")
                .commitment("Test")
                .address("Test")
                .addressEnglish("Test")
                .phone("Test")
                .fax("Test")
                .introduction("Test")
                .lightLogoImageFileName("Test")
                .lightLogoImageUrl("Test")
                .darkLogoImageFileName("Test")
                .darkLogoImageUrl("Test")
                .sloganImageFileName("Test")
                .sloganImageUrl("Test")
                .build();

        List<CompanyInformationDetailInformation> savedDetailInformation = new ArrayList<>();

        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key1", "Test Value1"));
        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key2", "Test Value2"));
        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key3", "Test Value3"));

        savedCompanyInformation.initDetailInformation(savedDetailInformation);

        savedCompanyInformationList.add(savedCompanyInformation);

        // stub
        when(companyInformationRepository.findAll()).thenReturn(savedCompanyInformationList);

        // when
        ApiResponse<CompanyInformation> response = companyInformationService.updateCompanyBasicInformation(requestDto);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("회사 기본 정보를 성공적으로 수정했습니다.", response.getMessage());
        Mockito.verify(companyInformationRepository, times(1)).save(any(CompanyInformation.class));
    }

    @Test
    @DisplayName("회사 기본 정보(주소, 유선번호, 팩스번호) 수정 실패 테스트")
    void updateCompanyBasicInformationFail() {
        // given
        String address = "Test address";
        String addressEnglish = "Test addressEnglish";
        String phone = "Test phone";
        String fax = "Test fax";

        List<DetailInformationDTO> detailInformation = new ArrayList<>();

        DetailInformationDTO dto1 = new DetailInformationDTO();
        dto1.setKey("Test Key1");
        dto1.setValue("Test Value1");
        detailInformation.add(dto1);

        DetailInformationDTO dto2 = new DetailInformationDTO();
        dto2.setKey("Test Key2");
        dto2.setValue("Test Value2");
        detailInformation.add(dto2);

        DetailInformationDTO dto3 = new DetailInformationDTO();
        dto3.setKey("Test Key3");
        dto3.setValue("Test Value3");
        detailInformation.add(dto3);

        UpdateCompanyBasicInformationServiceRequestDto requestDto = new UpdateCompanyBasicInformationServiceRequestDto(
                address, addressEnglish, phone, fax
        );

        // stub
        when(companyInformationRepository.findAll()).thenReturn(List.of());

        // when
        ApiResponse<CompanyInformation> response = companyInformationService.updateCompanyBasicInformation(requestDto);

        // then
        assertNotNull(response);
        assertEquals(ErrorCode.COMPANYINFORMATION_IS_EMPTY.getStatus(), response.getStatus());
        assertEquals(ErrorCode.COMPANYINFORMATION_IS_EMPTY.getMessage(), response.getMessage());
        Mockito.verify(companyInformationRepository, times(1)).findAll();
        Mockito.verify(companyInformationRepository, never()).save(any(CompanyInformation.class));
    }

    @Test
    @DisplayName("회사 소개 정보(mainOverview, commitment, introduction) 수정 성공 테스트")
    void updateCompanyIntroductionInformationSuccess() {
        // given
        String mainOverview = "Test mainOverview";
        String commitment = "Test commitment";
        String introduction = "Test introduction";

        List<DetailInformationDTO> detailInformation = new ArrayList<>();

        DetailInformationDTO dto1 = new DetailInformationDTO();
        dto1.setKey("Test Key1");
        dto1.setValue("Test Value1");
        detailInformation.add(dto1);

        DetailInformationDTO dto2 = new DetailInformationDTO();
        dto2.setKey("Test Key2");
        dto2.setValue("Test Value2");
        detailInformation.add(dto2);

        DetailInformationDTO dto3 = new DetailInformationDTO();
        dto3.setKey("Test Key3");
        dto3.setValue("Test Value3");
        detailInformation.add(dto3);

        UpdateCompanyIntroductionInformationServiceRequestDto requestDto = new UpdateCompanyIntroductionInformationServiceRequestDto(
                mainOverview, commitment, introduction
        );

        List<CompanyInformation> savedCompanyInformationList = new ArrayList<>();

        CompanyInformation savedCompanyInformation = CompanyInformation.builder()
                .mainOverview("Test")
                .commitment("Test")
                .address("Test")
                .addressEnglish("Test")
                .phone("Test")
                .fax("Test")
                .introduction("Test")
                .lightLogoImageFileName("Test")
                .lightLogoImageUrl("Test")
                .darkLogoImageFileName("Test")
                .darkLogoImageUrl("Test")
                .sloganImageFileName("Test")
                .sloganImageUrl("Test")
                .build();

        List<CompanyInformationDetailInformation> savedDetailInformation = new ArrayList<>();

        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key1", "Test Value1"));
        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key2", "Test Value2"));
        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key3", "Test Value3"));

        savedCompanyInformation.initDetailInformation(savedDetailInformation);

        savedCompanyInformationList.add(savedCompanyInformation);

        // stub
        when(companyInformationRepository.findAll()).thenReturn(savedCompanyInformationList);

        // when
        ApiResponse<CompanyInformation> response = companyInformationService.updateCompanyIntroductionInformation(requestDto);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("회사 소개 정보를 성공적으로 수정했습니다.", response.getMessage());
        Mockito.verify(companyInformationRepository, times(1)).save(any(CompanyInformation.class));
    }

    @Test
    @DisplayName("회사 소개 정보(mainOverview, commitment, introduction) 수정 실패 테스트")
    void updateCompanyIntroductionInformationFail() {
        // given
        String mainOverview = "Test mainOverview";
        String commitment = "Test commitment";
        String introduction = "Test introduction";

        List<DetailInformationDTO> detailInformation = new ArrayList<>();

        DetailInformationDTO dto1 = new DetailInformationDTO();
        dto1.setKey("Test Key1");
        dto1.setValue("Test Value1");
        detailInformation.add(dto1);

        DetailInformationDTO dto2 = new DetailInformationDTO();
        dto2.setKey("Test Key2");
        dto2.setValue("Test Value2");
        detailInformation.add(dto2);

        DetailInformationDTO dto3 = new DetailInformationDTO();
        dto3.setKey("Test Key3");
        dto3.setValue("Test Value3");
        detailInformation.add(dto3);

        UpdateCompanyIntroductionInformationServiceRequestDto requestDto = new UpdateCompanyIntroductionInformationServiceRequestDto(
                mainOverview, commitment, introduction
        );

        // stub
        when(companyInformationRepository.findAll()).thenReturn(List.of());

        // when
        ApiResponse<CompanyInformation> response = companyInformationService.updateCompanyIntroductionInformation(requestDto);

        // then
        assertNotNull(response);
        assertEquals(ErrorCode.COMPANYINFORMATION_IS_EMPTY.getStatus(), response.getStatus());
        assertEquals(ErrorCode.COMPANYINFORMATION_IS_EMPTY.getMessage(), response.getMessage());
        Mockito.verify(companyInformationRepository, times(1)).findAll();
        Mockito.verify(companyInformationRepository, never()).save(any(CompanyInformation.class));
    }

    @Test
    @DisplayName("회사 5가지 상세 정보 수정 성공 테스트")
    void updateCompanyDetailInformationSuccess() {
        // given
        List<DetailInformationDTO> detailInformation = new ArrayList<>();

        DetailInformationDTO dto1 = new DetailInformationDTO();
        dto1.setKey("Test Key1");
        dto1.setValue("Test Value1");
        detailInformation.add(dto1);

        DetailInformationDTO dto2 = new DetailInformationDTO();
        dto2.setKey("Test Key2");
        dto2.setValue("Test Value2");
        detailInformation.add(dto2);

        DetailInformationDTO dto3 = new DetailInformationDTO();
        dto3.setKey("Test Key3");
        dto3.setValue("Test Value3");
        detailInformation.add(dto3);

        UpdateCompanyDetailInformationServiceRequestDto requestDto = new UpdateCompanyDetailInformationServiceRequestDto(
                detailInformation
        );

        List<CompanyInformation> savedCompanyInformationList = new ArrayList<>();

        CompanyInformation savedCompanyInformation = CompanyInformation.builder()
                .mainOverview("Test")
                .commitment("Test")
                .address("Test")
                .addressEnglish("Test")
                .phone("Test")
                .fax("Test")
                .introduction("Test")
                .lightLogoImageFileName("Test")
                .lightLogoImageUrl("Test")
                .darkLogoImageFileName("Test")
                .darkLogoImageUrl("Test")
                .sloganImageFileName("Test")
                .sloganImageUrl("Test")
                .build();

        List<CompanyInformationDetailInformation> savedDetailInformation = new ArrayList<>();

        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key1", "Test Value1"));
        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key2", "Test Value2"));
        savedDetailInformation.add(new CompanyInformationDetailInformation(savedCompanyInformation, "Test Key3", "Test Value3"));

        savedCompanyInformation.initDetailInformation(savedDetailInformation);

        savedCompanyInformationList.add(savedCompanyInformation);

        // stub
        when(companyInformationRepository.findAll()).thenReturn(savedCompanyInformationList);

        // when
        ApiResponse<CompanyInformation> response = companyInformationService.updateCompanyDetailInformation(requestDto);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("회사 5가지 상세 정보를 성공적으로 수정했습니다.", response.getMessage());
        Mockito.verify(companyInformationRepository, times(1)).save(any(CompanyInformation.class));
    }

    @Test
    @DisplayName("회사 5가지 상세 정보 수정 실패 테스트")
    void updateCompanyDetailInformationFail() {
        // given
        List<DetailInformationDTO> detailInformation = new ArrayList<>();

        DetailInformationDTO dto1 = new DetailInformationDTO();
        dto1.setKey("Test Key1");
        dto1.setValue("Test Value1");
        detailInformation.add(dto1);

        DetailInformationDTO dto2 = new DetailInformationDTO();
        dto2.setKey("Test Key2");
        dto2.setValue("Test Value2");
        detailInformation.add(dto2);

        DetailInformationDTO dto3 = new DetailInformationDTO();
        dto3.setKey("Test Key3");
        dto3.setValue("Test Value3");
        detailInformation.add(dto3);

        UpdateCompanyDetailInformationServiceRequestDto requestDto = new UpdateCompanyDetailInformationServiceRequestDto(
                detailInformation
        );

        // stub
        when(companyInformationRepository.findAll()).thenReturn(List.of());

        // when
        ApiResponse<CompanyInformation> response = companyInformationService.updateCompanyDetailInformation(requestDto);

        // then
        assertNotNull(response);
        assertEquals(ErrorCode.COMPANYINFORMATION_IS_EMPTY.getStatus(), response.getStatus());
        assertEquals(ErrorCode.COMPANYINFORMATION_IS_EMPTY.getMessage(), response.getMessage());
        Mockito.verify(companyInformationRepository, times(1)).findAll();
        Mockito.verify(companyInformationRepository, never()).save(any(CompanyInformation.class));
    }

    @Test
    @DisplayName("CompanyInformation 전체 조회 성공")
    void retrieveAllCompanyInformation_Success() {
        // given
        CompanyInformation companyInformation1 = new CompanyInformation("mainOverview1", "commitment1", "address1",
                "addressEng1", "lightLogoImageFileName1", "lightLogoImageUrl1", "darkLogoImageFileName1",
                "darkLogoImageUrl1", "phone1", "fax1", "introduction1", "sloganImageFileName1", "sloganImageUrl1",
                null);
        CompanyInformation companyInformation2 = new CompanyInformation("mainOverview2", "commitment2", "address2",
                "addressEng2", "lightLogoImageFileName2", "lightLogoImageUrl2", "darkLogoImageFileName2",
                "darkLogoImageUrl2", "phone2", "fax2", "introduction2", "sloganImageFileName2", "sloganImageUrl2",
                null);
        List<CompanyInformation> companyInformationList = new ArrayList<>();
        companyInformationList.add(companyInformation1);
        companyInformationList.add(companyInformation2);
        when(companyInformationRepository.findAll()).thenReturn(companyInformationList);

        // when
        ApiResponse<CompanyInformation> response = companyInformationService.retrieveAllCampanyInformation();
        CompanyInformation retrievedCompanyInformation = response.getData();

        // then
        assertNotNull(retrievedCompanyInformation);
        assertEquals("전체 회사 정보를 성공적으로 조회하였습니다.", response.getMessage());
    }

    @Test
    @DisplayName("Company Information 전체 조회 실패 - 데이터 없음")
    void retrieveAllCompanyInformation_Fail() {
        // given
        when(companyInformationRepository.findAll()).thenReturn(Collections.emptyList());

        // when
        ApiResponse<CompanyInformation> response = companyInformationService.retrieveAllCampanyInformation();

        // then
        assertNull(response.getData());
        assertEquals("회사 정보가 존재하지 않습니다.", response.getMessage());
    }

    @Test
    @DisplayName("회사 로고 이미지 조회 성공")
    void retrieveCompanyLogoImage() {
        // given
        List<String> darkLogoImageUrls = List.of("darkLogoImageUrl1?v=123456789");
        when(companyInformationRepository.findDarkLogoImageUrl()).thenReturn(darkLogoImageUrls);

        // when
        ApiResponse<String> response = companyInformationService.retrieveCampanyLogoImage(false);
        String retrievedCompanyInformation = response.getData();

        // then
        assertNotNull(retrievedCompanyInformation);
        assertEquals("회사 로고 이미지를 성공적으로 조회하였습니다.", response.getMessage());
        assertTrue(retrievedCompanyInformation.startsWith("darkLogoImageUrl1"));
    }

    @Test
    @DisplayName("회사 로고 이미지 조회 실패 - 로고 이미지 없음")
    void retrieveCompanyLogoImageWhenLogoImageNotExists() {
        // given
        when(companyInformationRepository.findDarkLogoImageUrl()).thenReturn(Collections.emptyList());

        // when
        ApiResponse<String> response = companyInformationService.retrieveCampanyLogoImage(false);
        String retrievedCompanyInformation = response.getData();

        // then
        assertNull(retrievedCompanyInformation);
        assertEquals("회사 로고 이미지가 존재하지 않습니다.", response.getMessage());
    }

    @Test
    @DisplayName("회사 기본 정보 조회 성공")
    void retrieveCompanyBasicInformationSuccess() {
        // given
        CompanyBasicInformation companyBasicInformation = mock(CompanyBasicInformation.class);
        when(companyBasicInformation.getAddress()).thenReturn("address1");
        when(companyBasicInformation.getPhone()).thenReturn("phone1");
        when(companyInformationRepository.findAddressAndPhoneAndFax()).thenReturn(List.of(companyBasicInformation));

        // when
        ApiResponse<CompanyBasicInformation> response = companyInformationService.retrieveCompanyBasicInformation();

        // then
        assertNotNull(response.getData());
        assertEquals("회사 기본 정보를 성공적으로 조회하였습니다.", response.getMessage());
        assertEquals("address1", response.getData().getAddress());
        assertEquals("phone1", response.getData().getPhone());
    }
    @Test
    @DisplayName("회사 기본 정보 조회 실패 - 정보 없음")
    void retrieveCompanyBasicInformationFailure() {
        // given
        when(companyInformationRepository.findAddressAndPhoneAndFax()).thenReturn(Collections.emptyList());

        // when
        ApiResponse<CompanyBasicInformation> response = companyInformationService.retrieveCompanyBasicInformation();

        // then
        assertNull(response.getData());
        assertEquals("회사 기본 정보가 존재하지 않습니다.", response.getMessage());
    }

    @Test
    @DisplayName("회사 소개 정보 조회 성공")
    void retrieveCompanyIntroductionInformationSuccess() {
        // given
        CompanyIntroductionInformation companyIntroductionInformation = new CompanyIntroductionInformationImpl("introduction1", "sloganImageUrl1");
        when(companyInformationRepository.findIntroductionAndSloganImageUrl()).thenReturn(List.of(companyIntroductionInformation));

        // when
        ApiResponse<CompanyIntroductionInformation> response = companyInformationService.retrieveCompanyIntroductionInformation();

        // then
        assertNotNull(response.getData());
        assertEquals("회사 소개 정보를 성공적으로 조회하였습니다.", response.getMessage());
        assertTrue(response.getData().getSloganImageUrl().startsWith("sloganImageUrl1"));
    }

    @Test
    @DisplayName("회사 소개 정보 조회 실패 - 정보 없음")
    void retrieveCompanyIntroductionInformationFailure() {
        // given
        when(companyInformationRepository.findIntroductionAndSloganImageUrl()).thenReturn(Collections.emptyList());

        // when
        ApiResponse<CompanyIntroductionInformation> response = companyInformationService.retrieveCompanyIntroductionInformation();

        // then
        assertNull(response.getData());
        assertEquals("회사 소개 정보가 존재하지 않습니다.", response.getMessage());
    }
    @Test
    @DisplayName("회사 상세 정보 조회 성공")
    void retrieveCompanyDetailInformationSuccess() {
        // given
        CompanyInformation companyInformation = CompanyInformation.builder()
                .mainOverview("Main overview")
                .commitment("Our commitment")
                .detailInformation(new ArrayList<>())
                .build();

        companyInformation.getDetailInformation().add(CompanyInformationDetailInformation.builder()
                .companyInformation(companyInformation)
                .key("Detail Key 1")
                .value("Detail Value 1")
                .build());
        companyInformation.getDetailInformation().add(CompanyInformationDetailInformation.builder()
                .companyInformation(companyInformation)
                .key("Detail Key 2")
                .value("Detail Value 2")
                .build());

        List<CompanyInformation> companyInformations = List.of(companyInformation);
        when(companyInformationRepository.findAll()).thenReturn(companyInformations);

        // when
        ApiResponse<List<CompanyInformationDetailInformation>> response = companyInformationService.retrieveCompanyDetailInformation();

        // then
        assertNotNull(response.getData());
        assertEquals("회사 상세 정보를 성공적으로 조회하였습니다.", response.getMessage());
        assertEquals(2, response.getData().size());
    }

    @Test
    @DisplayName("회사 상세 정보 조회 실패 - 정보 없음")
    void retrieveCompanyDetailInformationFailure_NoInformation() {
        // given
        when(companyInformationRepository.findAll()).thenReturn(Collections.emptyList());

        // when
        ApiResponse<List<CompanyInformationDetailInformation>> response = companyInformationService.retrieveCompanyDetailInformation();

        // then
        assertEquals("회사 정보가 존재하지 않습니다.", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("전체 회사 정보 삭제 성공")
    void deleteAllCompanyInformationSuccess() {
        // given
        List<CompanyInformation> companyInformations = List.of(
                CompanyInformation.builder().lightLogoImageFileName("light_logo.png")
                        .darkLogoImageFileName("dark_logo.png")
                        .sloganImageFileName("slogan.png").build()
        );

        when(companyInformationRepository.findAll()).thenReturn(companyInformations);

        // when
        ApiResponse<String> response = companyInformationService.deleteAllCompanyInformation();

        // then
        assertEquals("전체 회사 정보를 성공적으로 삭제했습니다.", response.getMessage());
        verify(s3Adapter, times(3)).deleteFile(anyString()); // S3 파일 삭제 호출 확인
        verify(companyInformationRepository, times(1)).delete(any(CompanyInformation.class)); // 삭제 호출 확인
    }

    @Test
    @DisplayName("전체 회사 정보 삭제 실패 - 정보 없음")
    void deleteAllCompanyInformationFailure_NoCompanyInformation() {
        // given
        when(companyInformationRepository.findAll()).thenReturn(Collections.emptyList());

        // when
        ApiResponse<String> response = companyInformationService.deleteAllCompanyInformation();

        // then
        assertEquals("전체 회사 정보를 성공적으로 삭제했습니다.", response.getMessage()); // 수정된 메시지
        verify(s3Adapter, times(0)).deleteFile(anyString()); // S3 파일 삭제 호출이 없음을 확인
        verify(companyInformationRepository, times(0)).delete(any(CompanyInformation.class)); // 삭제 호출이 없음을 확인
    }


    @Test
    @DisplayName("회사 로고 이미지 삭제 성공")
    void deleteCompanyLogoImageSuccess() {
        // given
        CompanyInformation companyInformation = CompanyInformation.builder()
                .lightLogoImageFileName("light_logo.png")
                .darkLogoImageFileName("dark_logo.png")
                .build();

        when(companyInformationRepository.findAll()).thenReturn(List.of(companyInformation));

        // when
        ApiResponse<String> response = companyInformationService.deleteCompanyLogoImage();

        // then
        assertEquals("회사 로고 이미지를 성공적으로 삭제했습니다.", response.getMessage());
        verify(s3Adapter, times(2)).deleteFile(anyString()); // S3 파일 삭제 호출 확인
        verify(companyInformationRepository, times(1)).save(any(CompanyInformation.class)); // 저장 호출 확인
    }

    @Test
    @DisplayName("회사 로고 이미지 삭제 실패 - 정보 없음")
    void deleteCompanyLogoImageFailure_NoCompanyInformation() {
        // given
        when(companyInformationRepository.findAll()).thenReturn(Collections.emptyList());

        // when
        ApiResponse<String> response = companyInformationService.deleteCompanyLogoImage();

        // then
        assertEquals("회사 로고 이미지를 성공적으로 삭제했습니다.", response.getMessage()); // 수정된 메시지
        verify(s3Adapter, times(0)).deleteFile(anyString()); // S3 파일 삭제 호출이 없음을 확인
        verify(companyInformationRepository, times(0)).save(any(CompanyInformation.class)); // 저장 호출이 없음을 확인
    }
    @Test
    @DisplayName("회사 기본 정보 삭제 성공")
    void deleteCompanyBasicInformationSuccess() {
        // given
        CompanyInformation companyInformation = CompanyInformation.builder()
                .mainOverview("Main overview")
                .commitment("Our commitment")
                .build();

        when(companyInformationRepository.findAll()).thenReturn(List.of(companyInformation));

        // when
        ApiResponse<CompanyInformation> response = companyInformationService.deleteCompanyBasicInformation();

        // then
        assertEquals("회사 기본 정보를 성공적으로 삭제했습니다.", response.getMessage());
        verify(companyInformationRepository, times(1)).save(any(CompanyInformation.class)); // 저장 호출 확인
    }
    @Test
    @DisplayName("회사 소개 정보 삭제 성공")
    void deleteCompanyIntroductionInformationSuccess() {
        // given
        CompanyInformation companyInformation = CompanyInformation.builder()
                .sloganImageFileName("slogan.png")
                .build();

        when(companyInformationRepository.findAll()).thenReturn(List.of(companyInformation));

        // when
        ApiResponse<CompanyInformation> response = companyInformationService.deleteCompanyIntroductionInformation();

        // then
        assertEquals("회사 소개 정보를 성공적으로 삭제했습니다.", response.getMessage());
        verify(s3Adapter, times(1)).deleteFile(anyString()); // S3 파일 삭제 호출 확인
        verify(companyInformationRepository, times(1)).save(any(CompanyInformation.class)); // 저장 호출 확인
    }
    @Test
    @DisplayName("회사 소개 정보 삭제 실패 - 회사 정보 없음")
    void deleteCompanyIntroductionInformation_emptyCompany_returnsErrorResponse() {
        // given
        when(companyInformationRepository.findAll()).thenReturn(Collections.emptyList());

        // when
        ApiResponse<CompanyInformation> response = companyInformationService.deleteCompanyIntroductionInformation();

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals(ErrorCode.COMPANYINFORMATION_IS_EMPTY.getMessage(), response.getMessage());
    }
    @Test
    @DisplayName("회사 상세 정보 삭제 성공")
    void deleteCompanyDetailInformationSuccess() {
        // given
        CompanyInformation companyInformation = CompanyInformation.builder()
                .detailInformation(new ArrayList<>()) // 예시 데이터
                .build();

        when(companyInformationRepository.findAll()).thenReturn(List.of(companyInformation));

        // when
        ApiResponse<CompanyInformation> response = companyInformationService.deleteCompanyDetailInformation();

        // then
        assertEquals("회사 5가지 상세 정보를 성공적으로 삭제했습니다.", response.getMessage());
        verify(companyInformationRepository, times(1)).save(any(CompanyInformation.class)); // 저장 호출 확인
    }
    @Test
    @DisplayName("회사 상세 정보 삭제 실패 - 회사 정보 없음")
    void deleteCompanyDetailInformation_emptyCompany_returnsErrorResponse() {
        // given
        when(companyInformationRepository.findAll()).thenReturn(Collections.emptyList());

        // when
        ApiResponse<CompanyInformation> response = companyInformationService.deleteCompanyDetailInformation();

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals(ErrorCode.COMPANYINFORMATION_IS_EMPTY.getMessage(), response.getMessage());
    }
}
