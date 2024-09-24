package studio.studioeye.domain.company_information.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UpdateCompanyBasicInformationRequestDto(
        @Schema(description = "회사 주소, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "회사 주소는 필수 값입니다.")
        String address,

        @Schema(description = "영문 회사 주소, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "영문 회사 주소는 필수 값입니다.")
        String addressEnglish,

        @Schema(description = "회사 전화번호, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "회사 전화번호 필수 값입니다.")
        String phone,

        @Schema(description = "회사 팩스번호, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "회사 팩스번호 필수 값입니다.")
        String fax

) {
    public UpdateCompanyBasicInformationServiceRequestDto toServiceRequest() {
        return new UpdateCompanyBasicInformationServiceRequestDto(address, addressEnglish, phone, fax);
    }
}
