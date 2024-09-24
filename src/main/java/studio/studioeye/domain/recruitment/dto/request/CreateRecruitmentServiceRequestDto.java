package studio.studioeye.domain.recruitment.dto.request;

import studio.studioeye.domain.recruitment.domain.Recruitment;

public record CreateRecruitmentServiceRequestDto(
        String title,
        String content
) {
    public Recruitment toEntity() {
        return Recruitment.builder()
                .title(title)
                .content(content)
                .build();
    }
}
