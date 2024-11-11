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
import studio.studioeye.domain.company_information.dao.CompanyInformationRepository;
import studio.studioeye.domain.company_information.domain.CompanyInformation;
import studio.studioeye.domain.company_information.domain.CompanyInformationDetailInformation;
import studio.studioeye.domain.company_information.dto.request.CreateCompanyInformationServiceRequestDto;
import studio.studioeye.domain.company_information.dto.request.DetailInformationDTO;
import studio.studioeye.domain.company_information.dto.request.UpdateAllCompanyInformationServiceRequestDto;
import studio.studioeye.domain.company_information.dto.request.UpdateCompanyBasicInformationServiceRequestDto;
import studio.studioeye.domain.recruitment.domain.Recruitment;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;
import studio.studioeye.infrastructure.s3.S3Adapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompanyInformationServiceTest {

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
    public void createCompanyInformationSuccess() throws IOException {
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
    public void createCompanyInformationFail() throws IOException {
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
    public void updateAllCompanyInformationSuccess() throws IOException {
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
    public void updateAllCompanyInformationFail() throws IOException {
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
    public void updateAllCompanyTextInformationSuccess() {
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
    public void updateAllCompanyTextInformationFail() {
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
    public void updateCompanyLogoImageSuccess() throws IOException {
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
    public void updateCompanyLogoImageFail_invalidFile() throws IOException {
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
    public void updateCompanyLogoImageFail_notFound() throws IOException {
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
    public void updateCompanySloganImageSuccess() throws IOException {
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
    public void updateCompanySloganImageFail_invalidFile() throws IOException {
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
    public void updateCompanySloganImageFail_notFound() throws IOException {
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
    public void updateCompanyLogoAndSloganSuccess() throws IOException {
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
    public void updateCompanyLogoAndSloganFail_notFound() throws IOException {
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
    public void updateCompanyBasicInformationSuccess() {
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
    public void updateCompanyBasicInformationFail() {
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
}
