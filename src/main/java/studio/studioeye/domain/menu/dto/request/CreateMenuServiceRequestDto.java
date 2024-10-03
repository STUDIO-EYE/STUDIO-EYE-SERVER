package studio.studioeye.domain.menu.dto.request;

import studio.studioeye.domain.menu.domain.Menu;

public record CreateMenuServiceRequestDto(
        String title,
        Boolean visibility
) {
    public Menu toEntity() {
        return Menu.builder()
                .title(title)
                .visibility(visibility)
                .build();
    }
}
