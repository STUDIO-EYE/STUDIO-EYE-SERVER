package studio.studioeye.domain.views.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreateViewsRequestDto(
        @Schema(description = "연(ex:2024), 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "year 필수 값입니다.")
        Integer year,
        @Schema(description = "월(ex:1~12), 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "month 필수 값입니다.")
        Integer month,
        @Schema(description = "조회수, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "views 필수 값입니다.")
        Long views
) {
    public CreateViewsServiceDto toServiceViews() {
        return new CreateViewsServiceDto(year, month, views);
    }
}
