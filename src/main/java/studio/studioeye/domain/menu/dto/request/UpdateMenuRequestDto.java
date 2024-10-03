package studio.studioeye.domain.menu.dto.request;

public record UpdateMenuRequestDto(
        Long id,
        String title,
        Boolean visibility
) {
    public UpdateMenuServiceRequestDto toServiceRequest() {
        return new UpdateMenuServiceRequestDto(id, title, visibility);
    }
}
