package studio.studioeye.domain.ceo.dto.request;

public record UpdateCeoRequestDto(
    String name,
    String introduction
) {
    public UpdateCeoServiceRequestDto toServiceRequest() {
        return new UpdateCeoServiceRequestDto(name, introduction);
    }
}
