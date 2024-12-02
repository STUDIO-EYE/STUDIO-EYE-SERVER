package studio.studioeye.domain.news.dto;

import studio.studioeye.domain.news.domain.News;

import java.time.LocalDate;

public record CreateNewsServiceRequestDto(
        String title,
        String source,
        LocalDate pubDate,
        String url,
        Boolean visibility
) {
    // 기본 생성자는 제거
    public News toEntity() {
        return News.builder()
                .title(title)
                .source(source)
                .pubDate(pubDate)
                .url(url)
                .visibility(visibility)
                .build();
    }
}
