package studio.studioeye.domain.benefit.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Benefit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String imageUrl;

    @NotNull
    private String imageFileName;

    @NotNull
    private String title;

    @NotNull
    private String content;

    @Builder
    public Benefit(String imageUrl, String imageFileName, String title, String content) {
        this.imageUrl = imageUrl;
        this.imageFileName = imageFileName;
        this.title = title;
        this.content = content;
    }
}
