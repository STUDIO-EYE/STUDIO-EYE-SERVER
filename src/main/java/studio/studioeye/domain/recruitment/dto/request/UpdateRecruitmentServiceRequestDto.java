package studio.studioeye.domain.recruitment.dto.request;

import java.util.Date;

public record UpdateRecruitmentServiceRequestDto(
        Long id,
        String title,
        Date startDate,
        Date deadline,
        String link
) {
}
