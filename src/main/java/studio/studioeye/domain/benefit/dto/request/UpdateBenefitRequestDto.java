package studio.studioeye.domain.benefit.dto.request;

public record UpdateBenefitRequestDto(
        Long id,
        String title,
        String content
) {
    public UpdateBenefitServiceRequestDto toServiceRequest() {
        return new UpdateBenefitServiceRequestDto(id, title, content);
    }
}
