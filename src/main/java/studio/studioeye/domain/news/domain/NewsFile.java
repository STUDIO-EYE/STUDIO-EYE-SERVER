package studio.studioeye.domain.news.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class NewsFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length=1000)
    private String fileName;

    @Column(length=1000)
    private String filePath;

//    @Column(length=1000)
//    private String s3key;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id")
    @JsonIgnore
    private News news;

    @Builder
    public NewsFile(String fileName, String filePath, /*String s3key,*/ News news) {
        this.fileName = fileName;
        this.filePath = filePath;
//        this.s3key = s3key;
        this.news = news;
    }
}
