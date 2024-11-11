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
import studio.studioeye.domain.company_information.dto.request.CreateCompanyInformationServiceRequestDto;
import studio.studioeye.domain.company_information.dto.request.DetailInformationDTO;
import studio.studioeye.domain.recruitment.domain.Recruitment;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;
import studio.studioeye.infrastructure.s3.S3Adapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
                .thenReturn(ApiResponse.ok("프로젝트를 성공적으로 등록하였습니다.", "http://example.com/testImage.jpg"));

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
}
