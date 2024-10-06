package studio.studioeye.domain.recruitment.dto.request;

import studio.studioeye.domain.recruitment.domain.Recruitment;
import studio.studioeye.domain.recruitment.domain.Status;

import java.util.Date;

public record CreateRecruitmentServiceRequestDto(
        String title,
        Date startDate,
        Date deadline,
        String link
) {
    public Recruitment toEntity(Date date, Status status) {
        return Recruitment.builder()
                .title(title)
                .startDate(startDate)
                .deadline(deadline)
                .link(link)
                .createdAt(date)
                .status(status)
                .build();
    }
}
