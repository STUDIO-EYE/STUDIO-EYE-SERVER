package studio.studioeye.domain.company_information.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record UpdateCompanyDetailInformationRequestDto(
        @Schema(description = "회사 상세 정보, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "회사 상세 정보는 필수 값입니다.")
        List<DetailInformationDTO> detailInformation

) {
    public UpdateCompanyDetailInformationServiceRequestDto toServiceRequest() {
        return new UpdateCompanyDetailInformationServiceRequestDto(detailInformation);
    }
}
