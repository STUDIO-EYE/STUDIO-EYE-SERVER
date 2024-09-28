package studio.studioeye.domain.recruitment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UpdateRecruitmentRequestDto(
        @Schema(description = "채용공고 식별자, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "id는 필수 값입니다.")
        Long id,

        @Schema(description = "제목, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "제목은 필수 값입니다.")
        String title,

        @Schema(description = "기간, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "기간은 필수 값입니다.")
        String period,

        @Schema(description = "자격요건, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "자격요건은 필수 값입니다.")
        String qualifications,

        @Schema(description = "우대요건, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "우대요건은 필수 값입니다.")
        String preferential,

        @Schema(description = "채용공고 상태, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "채용공고 상태는 필수 값입니다.")
        Boolean status
) {
    public UpdateRecruitmentServiceRequestDto toServiceRequest() {
        return new UpdateRecruitmentServiceRequestDto(
                id, title, period, qualifications, preferential, status);
    }
}
