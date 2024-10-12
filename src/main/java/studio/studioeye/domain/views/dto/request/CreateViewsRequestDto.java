package studio.studioeye.domain.views.dto.request;

import com.example.promotionpage.domain.menu.domain.MenuTitle;
import com.example.promotionpage.domain.project.domain.ArtworkCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreateViewsRequestDto(
        @Schema(description = "연(ex:2024), 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "year는 필수 값입니다.")
        Integer year,
        @Schema(description = "월(ex:1~12), 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "month는 필수 값입니다.")
        Integer month,
        @Schema(description = "조회수, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "views는 필수 값입니다.")
        Long views,
        @Schema(description = "접속한 메뉴, 빈 값/공백/null 을 허용하지 않습니다.")
        @NotBlank(message = "menusms 필수 값입니다.")
        MenuTitle menu,
        @Schema(description = "Artwork일 경우의 접속한 카테고리, 빈 값/공백/null 을 허용하지 않습니다.")
        ArtworkCategory category
) {
    public CreateViewsServiceRequestDto toServiceViews() {
        return new CreateViewsServiceRequestDto(year, month, views, menu, category);
    }
}
