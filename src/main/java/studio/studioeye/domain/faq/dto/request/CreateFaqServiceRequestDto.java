package studio.studioeye.domain.faq.dto.request;

import studio.studioeye.domain.faq.domain.Faq;

public record CreateFaqServiceRequestDto (
    String question,
    String answer,
    Boolean visibility
) {
    public Faq toEntity() {
        return Faq.builder()
                .question(question)
                .answer(answer)
                .visibility(visibility)
                .build();
    }
}
