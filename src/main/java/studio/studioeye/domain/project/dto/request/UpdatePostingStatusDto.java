package studio.studioeye.domain.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdatePostingStatusDto(
	@Schema(description = "프로젝트 식별자")
	@Positive(message = "프로젝트 식별자는 양수여야 합니다.")
	Long projectId,

	@Schema(description = "프로젝트 게시 여부")
	@NotNull(message = "null 값을 허용하지 않습니다.")
	Boolean isPosted
) {
}
