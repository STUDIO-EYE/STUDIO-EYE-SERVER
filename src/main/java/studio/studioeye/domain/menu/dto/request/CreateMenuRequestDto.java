package studio.studioeye.domain.menu.dto.request;

import studio.studioeye.domain.menu.domain.MenuTitle;

public record CreateMenuRequestDto(
        MenuTitle menuTitle,
        Boolean visibility
) {
    public CreateMenuServiceRequestDto toServiceRequest() {
        return new CreateMenuServiceRequestDto(menuTitle, visibility);
    }
}
