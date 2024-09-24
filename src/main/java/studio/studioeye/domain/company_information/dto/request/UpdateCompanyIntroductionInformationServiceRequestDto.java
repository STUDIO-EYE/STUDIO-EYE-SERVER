package studio.studioeye.domain.company_information.dto.request;

public record UpdateCompanyIntroductionInformationServiceRequestDto(
        String mainOverview, String commitment, String introduction
) {
}
