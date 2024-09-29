package studio.studioeye.domain.news.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record UpdateNewsRequestDto(
        @Schema(description = "News 식별자 (0 이하의 값 허용 x), 백엔드에서 @Valid를 통한 유효성(빈 값, 공백, null) 검사가 안되니, 프론트에서 처리해주세요.")
        @NotBlank(message = "News 식별자는 양수여야 합니다.")
        Long id,
        @Schema(description = "News 제목, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "News 제목은 필수 값입니다.")
        String title,
        @Schema(description = "News 출처, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "News 출처는 필수 값입니다.")
        String source,
        @Schema(description = "News 날짜, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "News 날짜은 필수 값입니다.")
        LocalDate pubDate,
        @Schema(description = "News 링크, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "News 링크는 필수 값입니다.")
        String url,
        @Schema(description = "News 공개여부, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "News 공개여부는 필수 값입니다.")
        Boolean visibility
) {
        public UpdateNewsServiceRequestDto toServiceRequest() {
                return new UpdateNewsServiceRequestDto(id, title, source, pubDate, url, visibility);
        }
}
