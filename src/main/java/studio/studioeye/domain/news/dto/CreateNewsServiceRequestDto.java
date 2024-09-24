package studio.studioeye.domain.news.dto;

import studio.studioeye.domain.news.domain.News;
import studio.studioeye.domain.news.domain.NewsFile;

import java.time.LocalDate;
import java.util.List;

public record CreateNewsServiceRequestDto(
    String title,
    String source,
    LocalDate pubDate,
    String content,
    Boolean visibility,
    List<NewsFile> newsFiles
) {
    public CreateNewsServiceRequestDto(String title, String source, LocalDate pubDate, String content,
                                       Boolean visibility, List<NewsFile> newsFiles) {
        this.title = title;
        this.source = source;
        this.pubDate = pubDate;
        this.content = content;
        this.visibility = visibility;
        this.newsFiles = newsFiles();
    }
    public News toEntity() {
        return News.builder()
                .title(title)
                .source(source)
                .pubDate(pubDate)
                .content(content)
                .visibility(visibility)
                .newsFiles(newsFiles)
                .build();
    }
}
