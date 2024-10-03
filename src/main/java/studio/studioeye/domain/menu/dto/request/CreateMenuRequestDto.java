package studio.studioeye.domain.menu.dto.request;

public record CreateMenuRequestDto(
        String title,
        Boolean visibility
) {
    public CreateMenuServiceRequestDto toServiceRequest() {
        return new CreateMenuServiceRequestDto(title, visibility);
    }
}
