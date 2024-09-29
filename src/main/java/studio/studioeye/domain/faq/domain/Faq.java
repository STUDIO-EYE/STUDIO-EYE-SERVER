package studio.studioeye.domain.faq.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Faq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String question;

    @NotNull
    @Column(columnDefinition = "TEXT")
    private String answer;

    @NotNull
    private Boolean visibility;

    @Builder
    public Faq(String question, String answer, Boolean visibility) {
        this.question = question;
        this.answer = answer;
        this.visibility = visibility;
    }

    public void updateTitle(String question) {
        this.question = question;
    }

    public void updateContent(String answer) {
        this.answer = answer;
    }

    public void updateVisibility(Boolean visibility) {
        this.visibility = visibility;
    }
}
