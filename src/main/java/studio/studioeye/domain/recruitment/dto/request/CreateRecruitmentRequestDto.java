package studio.studioeye.domain.recruitment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.Date;

public record CreateRecruitmentRequestDto(

        @Schema(description = "제목, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "제목은 필수 값입니다.")
        String title,

        @Schema(description = "시작일, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "시작일은 필수 값입니다.")
        Date startDate,

        @Schema(description = "마감일, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "마감일은 필수 값입니다.")
        Date deadline,

        @Schema(description = "타사이트 링크, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "타사이트 링크는 필수 값입니다.")
        String link

) {
    public CreateRecruitmentServiceRequestDto toServiceRequest() {
        return new CreateRecruitmentServiceRequestDto(
                title, startDate, deadline, link);
    }
}
