package studio.studioeye.domain.company_information.dto.request;

import studio.studioeye.domain.company_information.domain.CompanyInformation;
import studio.studioeye.domain.company_information.domain.CompanyInformationDetailInformation;

import java.util.ArrayList;
import java.util.List;

public record CreateCompanyInformationServiceRequestDto(
        String mainOverview,
        String commitment,
        String address,
        String addressEnglish,
        String phone,
        String fax,
        String introduction,
        List<DetailInformationDTO> detailInformation
) {
    public CompanyInformation toEntity(String lightLogoImageFileName, String lightLogoImageUrl,
                                       String darkLogoImageFileName, String darkLogoImageUrl,
                                       String sloganImageFileName, String sloganImageUrl) {
        CompanyInformation companyInformation = CompanyInformation.builder()
                .mainOverview(mainOverview)
                .commitment(commitment)
                .address(address)
                .addressEnglish(addressEnglish)
                .phone(phone)
                .fax(fax)
                .introduction(introduction)
                .lightLogoImageFileName(lightLogoImageFileName)
                .lightLogoImageUrl(lightLogoImageUrl)
                .darkLogoImageFileName(darkLogoImageFileName)
                .darkLogoImageUrl(darkLogoImageUrl)
                .sloganImageFileName(sloganImageFileName)
                .sloganImageUrl(sloganImageUrl)
                .build();

//        CompanyInformation.CompanyInformationBuilder builder = CompanyInformation.builder()
//                .mainOverview(mainOverview)
//                .commitment(commitment)
//                .address(address)
//                .addressEnglish(addressEnglish)
//                .phone(phone)
//                .fax(fax)
//                .introduction(introduction)
//                .logoImageFileName(logoImageFileName)
//                .logoImageUrl(logoImageUrl)
//                .sloganImageFileName(sloganImageFileName)
//                .sloganImageUrl(sloganImageUrl);

        List<CompanyInformationDetailInformation> companyInformationDetails = new ArrayList<>();
        if (!detailInformation.isEmpty()) {
            for (DetailInformationDTO dto : detailInformation) {
                companyInformationDetails.add(CompanyInformationDetailInformation.builder()
                        .companyInformation(companyInformation)
                        .key(dto.getKey())
                        .value(dto.getValue())
                        .build());
            }
//            builder.detailInformation(companyInformationDetails);
        }
        companyInformation.initDetailInformation(companyInformationDetails);
        return companyInformation;
//        return builder.build();

    }

    public UpdateAllCompanyInformationServiceRequestDto toUpdateServiceRequest() {
        return new UpdateAllCompanyInformationServiceRequestDto(mainOverview, commitment, address, addressEnglish, phone, fax, introduction, detailInformation);
    }
}
