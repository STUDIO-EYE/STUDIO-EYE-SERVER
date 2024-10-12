package studio.studioeye.domain.views.dto.request;

import studio.studioeye.domain.menu.domain.MenuTitle;
import studio.studioeye.domain.project.domain.ArtworkCategory;
import studio.studioeye.domain.views.domain.Views;

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
