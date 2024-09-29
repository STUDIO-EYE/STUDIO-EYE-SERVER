package studio.studioeye.domain.recruitment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.Date;

public record UpdateRecruitmentRequestDto(
        @Schema(description = "채용공고 식별자, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "id는 필수 값입니다.")
        Long id,

        @Schema(description = "제목, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "제목은 필수 값입니다.")
        String title,

        @Schema(description = "시작일, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "시작일은 필수 값입니다.")
        Date startDate,

        @Schema(description = "마감일, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "마감일은 필수 값입니다.")
        Date deadline,

        @Schema(description = "자격요건, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "자격요건은 필수 값입니다.")
        String qualifications,

        @Schema(description = "우대요건, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "우대요건은 필수 값입니다.")
        String preferential,

        @Schema(description = "타사이트 링크, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "타사이트 링크는 필수 값입니다.")
        String link
) {
    public UpdateRecruitmentServiceRequestDto toServiceRequest() {
        return new UpdateRecruitmentServiceRequestDto(
                id, title, startDate, deadline, link);
    }
}
