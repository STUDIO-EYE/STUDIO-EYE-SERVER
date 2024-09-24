package studio.studioeye.domain.company_information.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CreateCompanyInformationRequestDto(
        @Schema(description = "회사 메인 소개, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "회사 메인 소개는 필수 값입니다.")
        String mainOverview,
        @Schema(description = "회사 다짐, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "회사 다짐은 필수 값입니다.")
        String commitment,
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
        String fax,
        @Schema(description = "회사 (About) 줄글 소개, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "회사 (About) 줄글 소개 필수 값입니다.")
        String introduction,

        @Schema(description = "회사 상세 정보, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "회사 상세 정보는 필수 값입니다.")
        List<DetailInformationDTO> detailInformation

) {
    public CreateCompanyInformationServiceRequestDto toServiceRequest() {
        return new CreateCompanyInformationServiceRequestDto(mainOverview, commitment, address, addressEnglish, phone, fax, introduction, detailInformation);
    }
}
