package studio.studioeye.domain.menu.dto.request;

public record UpdateMenuRequestDto(
        Long id,
        Boolean visibility,
        Integer sequence
) {
//    public UpdateMenuServiceRequestDto toServiceRequest() {
//        return new UpdateMenuServiceRequestDto(id, visibility, order);
//    }
}
