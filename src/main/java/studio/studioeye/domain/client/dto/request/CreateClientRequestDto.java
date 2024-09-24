package studio.studioeye.domain.client.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreateClientRequestDto(
    @Schema(description = "클라이언트 이름, 빈 값/공백/null 을 허용하지 않습니다.")
    @NotBlank(message = "클라이언트 이름은 필수 값입니다.")
    String name,
    @Schema(description = "클라이언트 공개여부, 빈 값/공백/null 을 허용하지 않습니다.")
    @NotBlank(message = "클라이언트 공개여부는 필수 값입니다.")
    Boolean visibility
    ) {
    public CreateClientServiceRequestDto toServiceRequest() {
        return new CreateClientServiceRequestDto(name, visibility != null && visibility);
    }
}
