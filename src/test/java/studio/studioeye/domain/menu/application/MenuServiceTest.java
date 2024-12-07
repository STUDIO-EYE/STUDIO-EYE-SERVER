package studio.studioeye.domain.menu.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import studio.studioeye.domain.menu.dao.MenuRepository;
import studio.studioeye.domain.menu.domain.Menu;
import studio.studioeye.domain.menu.domain.MenuTitle;
import studio.studioeye.domain.menu.dto.request.CreateMenuServiceRequestDto;
import studio.studioeye.domain.menu.dto.request.UpdateMenuRequestDto;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @InjectMocks
    private MenuService menuService;

    @Mock
    private MenuRepository menuRepository;

    @Test
    @DisplayName("PA 메뉴 생성 성공 테스트")
    void createMenuSuccess() {
        // given
        List<CreateMenuServiceRequestDto> dtos = List.of(
                new CreateMenuServiceRequestDto(MenuTitle.MAIN, true),
                new CreateMenuServiceRequestDto(MenuTitle.ABOUT, true)
        );

        when(menuRepository.existsByMenuTitle(any())).thenReturn(false);
        when(menuRepository.count()).thenReturn(1L);
        when(menuRepository.save(any(Menu.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        ApiResponse<List<Menu>> response = menuService.createMenu(dtos);

        // then
        assertNotNull(response);
        assertEquals(2, response.getData().size());
        verify(menuRepository, times(2)).save(any(Menu.class));
    }

    @Test
    @DisplayName("PA 메뉴 생성 실패 테스트 - 중복된 메뉴")
    void createMenuFailDuplicate() {
        // given
        List<CreateMenuServiceRequestDto> dtos = List.of(
                new CreateMenuServiceRequestDto(MenuTitle.MAIN, true)
        );

        when(menuRepository.existsByMenuTitle(MenuTitle.MAIN)).thenReturn(true);

        // when
        ApiResponse<List<Menu>> response = menuService.createMenu(dtos);

        // then
        assertEquals(ErrorCode.ALREADY_EXISTED_MENU.getStatus(), response.getStatus());
        verify(menuRepository, never()).save(any(Menu.class));
    }

    @Test
    @DisplayName("PA 메뉴 목록 조회 성공 테스트")
    void retrieveAllMenuSuccess() {
        // given
        List<Menu> menuList = List.of(
                new Menu(MenuTitle.ALL, true, 1),
                new Menu(MenuTitle.MAIN, true, 2)
        );

        when(menuRepository.findAll()).thenReturn(menuList);

        // when
        ApiResponse<List<Menu>> response = menuService.retrieveAllMenu();

        // then
        Assertions.assertThat(response.getData()).isEqualTo(menuList);
        Assertions.assertThat(response.getData()).hasSize(2);
    }

    @Test
    @DisplayName("PA 메뉴 목록 조회 실패 테스트")
    void retrieveAllMenuFail() {
        // given
        when(menuRepository.findAll()).thenReturn(new ArrayList<>());

        // when
        ApiResponse<List<Menu>> response = menuService.retrieveAllMenu();

        // then
        assertNull(response.getData());
        assertEquals("메뉴가 존재하지 않습니다.", response.getMessage());
    }

    @Test
    @DisplayName("PA 메뉴 수정 성공 테스트")
    void updateMenuSuccess() {
        // given
        UpdateMenuRequestDto dto = new UpdateMenuRequestDto(1L, false, 3);
        Menu existingMenu = new Menu(MenuTitle.MAIN, true, 2);

        when(menuRepository.findById(1L)).thenReturn(Optional.of(existingMenu));
        when(menuRepository.save(any(Menu.class))).thenReturn(existingMenu);

        // when
        ApiResponse<List<Menu>> response = menuService.updateMenu(List.of(dto));

        // then
        assertEquals(1, response.getData().size());
        verify(menuRepository, times(1)).save(existingMenu);
    }

    @Test
    @DisplayName("PA 메뉴 수정 실패 테스트")
    void updateMenuFail() {
        // given
        UpdateMenuRequestDto dto = new UpdateMenuRequestDto(1L, false, 3);

        when(menuRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        ApiResponse<List<Menu>> response = menuService.updateMenu(List.of(dto));

        // then
        assertEquals(ErrorCode.INVALID_MENU_ID.getStatus(), response.getStatus());
        verify(menuRepository, never()).save(any(Menu.class));
    }

    @Test
    @DisplayName("PP 메뉴 제목 목록 조회 성공 테스트")
    void retrieveMenuSuccess() {
        // given
        List<MenuTitle> menuTitles = List.of(MenuTitle.ALL, MenuTitle.MAIN);

        when(menuRepository.findTitleByVisibilityTrue()).thenReturn(menuTitles);

        // when
        ApiResponse<List<MenuTitle>> response = menuService.retrieveMenu();

        // then
        Assertions.assertThat(response.getData()).isEqualTo(menuTitles);
        Assertions.assertThat(response.getData()).hasSize(2);
    }

    @Test
    @DisplayName("PP 메뉴 제목 목록 조회 실패 테스트")
    void retrieveMenuFail() {
        // given
        when(menuRepository.findTitleByVisibilityTrue()).thenReturn(new ArrayList<>());

        // when
        ApiResponse<List<MenuTitle>> response = menuService.retrieveMenu();

        // then
        assertNull(response.getData());
        assertEquals("공개된 메뉴가 존재하지 않습니다.", response.getMessage());
    }

    @Test
    @DisplayName("PA 메뉴 삭제 성공 테스트")
    void deleteMenuSuccess() {
        // given
        Long id = 1L;
        Menu menu = new Menu(MenuTitle.MAIN, true, 1);

        when(menuRepository.findById(id)).thenReturn(Optional.of(menu));

        // when
        ApiResponse<Menu> response = menuService.deleteMenu(id);

        // then
        assertEquals(HttpStatus.OK, response.getStatus());
        verify(menuRepository, times(1)).delete(menu);
    }

    @Test
    @DisplayName("PA 메뉴 삭제 실패 테스트")
    void deleteMenuFail() {
        // given
        Long id = 1L;

        when(menuRepository.findById(id)).thenReturn(Optional.empty());

        // when
        ApiResponse<Menu> response = menuService.deleteMenu(id);

        // then
        assertEquals(ErrorCode.INVALID_MENU_ID.getStatus(), response.getStatus());
        verify(menuRepository, never()).delete(any(Menu.class));
    }
}
