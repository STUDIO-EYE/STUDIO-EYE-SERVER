package studio.studioeye.domain.views.dto.request;

import studio.studioeye.domain.views.domain.Views;

import java.util.Date;

public record CreateViewsServiceDto(
        Integer year,
        Integer month,
        Long views
) {
    public CreateViewsServiceDto(Integer year, Integer month, Long views) {
        this.year = year;
        this.month = month;
        this.views = views;
    }
    public Views toEntity(Date date) {
        return Views.builder()
                .year(year)
                .month(month)
                .views(views)
                .createdAt(date)
                .build();
    }
}
