package studio.studioeye.domain.partner_information.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdatePartnerInfoRequestDto(
	@Schema(description = "협력사 식별자 (0 이하의 값 허용 x), 백엔드에서 @Valid를 통한 유효성(빈 값, 공백, null) 검사가 안되니, 프론트에서 처리해주세요.")
	@NotBlank(message = "협력사 식별자는 양수여야 합니다.")
	Long id,
	@NotNull(message = "협력사 이름은 필수 값입니다, null / 빈 값 / 공백 허용 x")
	@NotBlank(message = "협력사 이름은 필수 값입니다.")
	String name,
	@NotNull(message = "is_main은 필수 값입니다.")
	Boolean is_main,

	@Schema(description = "협력사 링크 URL, null / 빈 값 / 공백 허용 x")
	@NotBlank(message = "link는 필수 값입니다.")
	String link
) {

	public UpdatePartnerInfoServiceRequestDto toServiceRequest() {
		return new UpdatePartnerInfoServiceRequestDto(id, name, is_main != null && is_main, link);
	}
}
