package studio.studioeye.domain.news.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String title;
    @NotNull
    private String source;
    @NotNull
    private LocalDate pubDate;
    @NotNull
    private String url;
    @NotNull
    private Boolean visibility;

    @Builder
    public News(String title, String source, LocalDate pubDate, String url, Boolean visibility) {
        this.title = title;
        this.source = source;
        this.pubDate = pubDate;
        this.url = url;
        this.visibility = visibility;
    }

    public void updateTitle(String title) {
        this.title = title;
    }
    public void updateSource(String source) {
        this.source = source;
    }
    public void updatePubDate(LocalDate pubDate) {
        this.pubDate = pubDate;
    }
    public void updateUrl(String url) {
        this.url = url;
    }
    public void updateVisibility(Boolean visibility) {
        this.visibility = visibility;
    }

}
