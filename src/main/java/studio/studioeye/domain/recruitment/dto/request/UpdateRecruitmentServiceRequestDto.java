package studio.studioeye.domain.recruitment.dto.request;

public record UpdateRecruitmentServiceRequestDto(
        Long id,
        String title,
        String period,
        String qualifications,
        String preferential,
        Boolean status
) {
}
