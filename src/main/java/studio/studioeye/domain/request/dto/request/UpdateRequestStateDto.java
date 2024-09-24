package studio.studioeye.domain.request.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import studio.studioeye.domain.request.domain.State;

public record UpdateRequestStateDto(
        @Schema(description = "문의(의뢰) 상태, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "상태는 필수 값입니다.")
        State state
) {
    public UpdateRequestStateServiceDto toServiceRequest() {
        return new UpdateRequestStateServiceDto(state);
    }
}
