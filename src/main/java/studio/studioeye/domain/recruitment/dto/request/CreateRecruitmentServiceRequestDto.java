package studio.studioeye.domain.recruitment.dto.request;

import studio.studioeye.domain.recruitment.domain.Recruitment;

import java.util.Date;

public record CreateRecruitmentServiceRequestDto(
        String title,
        String period,
        String qualifications,
        String preferential
) {
    public Recruitment toEntity(Date date) {
        return Recruitment.builder()
                .title(title)
                .period(period)
                .qualifications(qualifications)
                .preferential(preferential)
                .createdAt(date)
                .build();
    }
}
