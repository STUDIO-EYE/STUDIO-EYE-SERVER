package studio.studioeye.domain.company_information.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UpdateCompanyIntroductionInformationRequestDto(
        @Schema(description = "회사 줄글 소개, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "회사 줄글 소개 필수 값입니다.")
        String mainOverview,
        @Schema(description = "회사 줄글 소개, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "회사 줄글 소개 필수 값입니다.")
        String commitment,
        @Schema(description = "회사 줄글 소개, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "회사 줄글 소개 필수 값입니다.")
        String introduction

) {
    public UpdateCompanyIntroductionInformationServiceRequestDto toServiceRequest() {
        return new UpdateCompanyIntroductionInformationServiceRequestDto(mainOverview, commitment, introduction);
    }
}
