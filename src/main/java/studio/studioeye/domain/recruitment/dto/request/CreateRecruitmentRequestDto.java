package studio.studioeye.domain.recruitment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreateRecruitmentRequestDto(

        @Schema(description = "제목, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "제목은 필수 값입니다.")
        String title,

        @Schema(description = "내용, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "내용은 필수 값입니다.")
        String content

) {
    public CreateRecruitmentServiceRequestDto toServiceRequest() {
        return new CreateRecruitmentServiceRequestDto(
                title, content);
    }
}
