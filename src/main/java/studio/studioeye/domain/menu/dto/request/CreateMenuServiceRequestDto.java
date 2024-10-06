package studio.studioeye.domain.menu.dto.request;

import studio.studioeye.domain.menu.domain.Menu;
import studio.studioeye.domain.menu.domain.MenuTitle;

public record CreateMenuServiceRequestDto(
        MenuTitle menuTitle,
        Boolean visibility
) {
    public Menu toEntity(int totalCount) {
        return Menu.builder()
                .menuTitle(menuTitle)
                .visibility(visibility)
                .sequence(totalCount)
                .build();
    }
}
