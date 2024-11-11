package studio.studioeye.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import studio.studioeye.domain.company_information.application.CompanyInformationService;
import studio.studioeye.domain.company_information.dao.CompanyBasicInformation;
import studio.studioeye.domain.company_information.dao.CompanyInformationRepository;
import studio.studioeye.domain.company_information.dao.CompanyIntroductionInformation;
import studio.studioeye.domain.company_information.dao.CompanyIntroductionInformationImpl;
import studio.studioeye.domain.company_information.domain.CompanyInformation;
import studio.studioeye.domain.company_information.domain.CompanyInformationDetailInformation;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.infrastructure.s3.S3Adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompanyInformationServiceTest {

    @InjectMocks
    private CompanyInformationService companyInformationService;

    @Mock
    private CompanyInformationRepository companyInformationRepository;
    @Mock
    private S3Adapter s3Adaptor;

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

}
