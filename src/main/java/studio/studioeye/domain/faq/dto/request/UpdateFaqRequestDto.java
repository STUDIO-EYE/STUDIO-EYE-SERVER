package studio.studioeye.domain.faq.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UpdateFaqRequestDto (
        @Schema(description = "FAQ 식별자 (0 이하의 값 허용 x), 백엔드에서 @Valid를 통한 유효성(빈 값, 공백, null) 검사가 안되니, 프론트에서 처리해주세요.")
        @NotBlank(message = "FAQ 식별자는 양수여야 합니다.")
        Long id,
        @Schema(description = "FAQ 질문, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "FAQ 질문은 필수 값입니다.")
        String question,
        @Schema(description = "FAQ 답변, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "FAQ 답변은 필수 값입니다.")
        String answer,
        @Schema(description = "FAQ 공개여부, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "FAQ 공개여부는 필수 값입니다.")
        Boolean visibility
) {
        public UpdateFaqServiceRequestDto toServiceRequest() {
                return new UpdateFaqServiceRequestDto(id, question, answer, visibility);
        }
}
