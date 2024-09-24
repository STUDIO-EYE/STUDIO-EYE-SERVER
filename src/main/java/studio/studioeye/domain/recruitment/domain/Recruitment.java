package studio.studioeye.domain.recruitment.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import studio.studioeye.domain.recruitment.dto.request.UpdateRecruitmentServiceRequestDto;

@Data
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recruitment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String title;

    @NotNull
    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder
    public Recruitment(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void update(UpdateRecruitmentServiceRequestDto dto) {
        this.title = dto.title();
        this.content = dto.content();
    }
}
