package studio.studioeye.domain.views.dto.request;

import com.example.promotionpage.domain.menu.domain.MenuTitle;
import com.example.promotionpage.domain.project.domain.ArtworkCategory;
import com.example.promotionpage.domain.views.domain.Views;

import java.util.Date;

public record CreateViewsServiceRequestDto(
        Integer year,
        Integer month,
        Long views,
        MenuTitle menu,
        ArtworkCategory category
) {
    public Views toEntity(Date date) {
        return Views.builder()
                .year(year)
                .month(month)
                .views(views)
                .menu(menu)
                .category(category)
                .createdAt(date)
                .build();
    }
}
