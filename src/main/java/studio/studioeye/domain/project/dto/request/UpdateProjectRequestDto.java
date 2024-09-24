package studio.studioeye.domain.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateProjectRequestDto(
	@Schema(description = "프로젝트 식별자")
	@Positive(message = "프로젝트 식별자는 양수여야 합니다.")
	Long projectId,

	@Schema(description = "부서명, 빈 값/공백/null 을 허용하지 않습니다.")
	@NotBlank(message = "부서명은 필수 값입니다.")
	String department,

	@Schema(description = "카테고리, 빈 값/공백/null 을 허용하지 않습니다.")
	@NotBlank(message = "카테고리는 필수 값입니다.")
	String category,

	@Schema(description = "프로젝트 이름, 빈 값/공백/null 을 허용하지 않습니다.")
	@NotBlank(message = "프로젝트 이름은 필수 값입니다.")
	String name,

	@Schema(description = "클라이언트 이름, 빈 값/공백/null 을 허용하지 않습니다.")
	@NotBlank(message = "클라이언트 이름은 필수 값입니다.")
	String client,

	@Schema(description = "빈 값/공백/null 을 허용하지 않습니다.")
	@NotBlank(message = "date는 필수 값입니다.")
	String date,

	@Schema(description = "빈 값/공백/null 을 허용하지 않습니다.")
	@NotBlank(message = "link는 필수 값입니다.")
	String link,

	@Schema(description = "overView는 빈 값/공백/null 을 허용하지 않습니다.")
	@NotBlank(message = "overView는 필수 값입니다.")
	String overView,

	@Schema(description = "프로젝트 타입")
	@NotNull(message = "null 값을 허용하지 않습니다.")
	String projectType,

	@Schema(description = "프로젝트 게시 여부")
	@NotNull(message = "null 값을 허용하지 않습니다.")
	Boolean isPosted

) {
	public UpdateProjectServiceRequestDto toServiceRequest() {
		return new UpdateProjectServiceRequestDto(projectId, department,category,name,client,date,link,overView, projectType, isPosted);
	}
}
