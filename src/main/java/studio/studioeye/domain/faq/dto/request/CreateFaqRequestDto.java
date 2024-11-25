package studio.studioeye.domain.faq.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateFaqRequestDto (
    @Schema(description = "FAQ 질문, 빈 값/공백/null 을 허용하지 않습니다.")
    @NotBlank(message = "FAQ 질문은 필수 값입니다.")
    String question,
    @Schema(description = "FAQ 답변, 빈 값/공백/null 을 허용하지 않습니다.")
    @NotBlank(message = "FAQ 답변은 필수 값입니다.")
    String answer,
    @Schema(description = "FAQ 공개여부, 빈 값/공백/null 을 허용하지 않습니다.")
    @NotNull(message = "FAQ 공개여부는 필수 값입니다.")
    Boolean visibility
) {
    public CreateFaqServiceRequestDto toServiceFaq() {
        return new CreateFaqServiceRequestDto(question, answer, visibility);
    }
}
