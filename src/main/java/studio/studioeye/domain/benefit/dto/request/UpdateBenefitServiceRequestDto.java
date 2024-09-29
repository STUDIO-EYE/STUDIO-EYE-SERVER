package studio.studioeye.domain.benefit.dto.request;

public record UpdateBenefitServiceRequestDto(
        Long id,
        String title,
        String content
) {
}
