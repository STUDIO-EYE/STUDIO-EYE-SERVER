package studio.studioeye.domain.recruitment.dto.request;

public record UpdateRecruitmentServiceRequestDto(
        Long id,
        String title,
        String content
) {
}
