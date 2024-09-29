package studio.studioeye.domain.news.dto;

import com.example.promotionpage.domain.news.domain.News;

import java.time.LocalDate;

public record CreateNewsServiceRequestDto(
    String title,
    String source,
    LocalDate pubDate,
    String url,
    Boolean visibility
) {
    public CreateNewsServiceRequestDto(String title, String source, LocalDate pubDate, String url,
                                       Boolean visibility) {
        this.title = title;
        this.source = source;
        this.pubDate = pubDate;
        this.url = url;
        this.visibility = visibility;
    }
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
