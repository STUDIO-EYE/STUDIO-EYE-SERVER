package studio.studioeye.domain.benefit.dto.request;

public record CreateBenefitRequestDto(
        String title,
        String content
) {
    public CreateBenefitServiceRequestDto toServiceRequest() {
        return new CreateBenefitServiceRequestDto(title, content);
    }
}
