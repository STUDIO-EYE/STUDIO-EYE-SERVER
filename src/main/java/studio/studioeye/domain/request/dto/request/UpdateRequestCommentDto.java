package studio.studioeye.domain.request.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import studio.studioeye.domain.request.domain.State;

public record UpdateRequestCommentDto(
        @Schema(description = "답변, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "답변은 필수 값입니다.")
        String answer,
        @Schema(description = "문의(의뢰) 상태, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "상태는 필수 값입니다.")
        State state
) {
    public UpdateRequestCommentServiceDto toServiceRequest() {
        return new UpdateRequestCommentServiceDto(answer, state);
    }
}
