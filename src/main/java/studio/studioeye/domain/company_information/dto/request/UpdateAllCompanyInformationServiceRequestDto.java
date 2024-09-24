package studio.studioeye.domain.company_information.dto.request;

import java.util.List;

public record UpdateAllCompanyInformationServiceRequestDto(
        String mainOverview,
        String commitment,
        String address,
        String addressEnglish,
        String phone,
        String fax,
        String introduction,
        List<DetailInformationDTO> detailInformation
) {
}
