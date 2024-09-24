package studio.studioeye.domain.news.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
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
    @Column(columnDefinition = "TEXT")
    private String content;

    @NotNull
    private Boolean visibility;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "news", cascade = CascadeType.REMOVE)
    private List<NewsFile> newsFiles;

    @Builder
    public News(String title, String source, LocalDate pubDate, String content, Boolean visibility,
                List<NewsFile> newsFiles) {
        this.title = title;
        this.source = source;
        this.pubDate = pubDate;
        this.content = content;
        this.visibility = visibility;
        this.newsFiles = newsFiles;
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
    public void updateContent(String content) {
        this.content = content;
    }

    public void updateVisibility(Boolean visibility) {
        this.visibility = visibility;
    }

}
