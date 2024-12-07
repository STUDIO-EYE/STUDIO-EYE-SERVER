package studio.studioeye.domain.menu.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
    @DisplayName("메뉴 생성 성공 테스트")
    void createMenuSuccess() {
        // given
        List<CreateMenuServiceRequestDto> dtoList = new ArrayList<>();
        dtoList.add(new CreateMenuServiceRequestDto(MenuTitle.ABOUT, true));
        dtoList.add(new CreateMenuServiceRequestDto(MenuTitle.ARTWORK, true));
        dtoList.add(new CreateMenuServiceRequestDto(MenuTitle.CONTACT, true));
        dtoList.add(new CreateMenuServiceRequestDto(MenuTitle.FAQ, true));
        dtoList.add(new CreateMenuServiceRequestDto(MenuTitle.RECRUITMENT, true));
        dtoList.add(new CreateMenuServiceRequestDto(MenuTitle.NEWS, true));

        // stub
        when(menuRepository.existsByMenuTitle(MenuTitle.ABOUT)).thenReturn(false);
        when(menuRepository.save(any(Menu.class)))
                .thenReturn(dtoList.get(0).toEntity(0))
                .thenReturn(dtoList.get(1).toEntity(1))
                .thenReturn(dtoList.get(2).toEntity(2))
                .thenReturn(dtoList.get(3).toEntity(3))
                .thenReturn(dtoList.get(4).toEntity(4))
                .thenReturn(dtoList.get(5).toEntity(5));

        // when
        ApiResponse<List<Menu>> response = menuService.createMenu(dtoList);
        List<Menu> menuList = response.getData();

        // then
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("메뉴를 성공적으로 등록하였습니다.", response.getMessage());
        assertNotNull(menuList);
        assertEquals(dtoList.size(), menuList.size());
        assertEquals(dtoList.isEmpty(), menuList.isEmpty());
        for(int i=0; i<menuList.size(); i++) {
            assertEquals(dtoList.get(i).menuTitle(), menuList.get(i).getMenuTitle());
            assertEquals(dtoList.get(i).visibility(), menuList.get(i).getVisibility());
        }

        // verify
        Mockito.verify(menuRepository, times(dtoList.size())).existsByMenuTitle(any(MenuTitle.class));
        Mockito.verify(menuRepository, times(dtoList.size())).save(any(Menu.class));
    }

    @Test
    @DisplayName("메뉴 생성 실패 테스트 - 입력된 메뉴가 없는 경우")
    void createMenuFail_EmptyInput() {
        // given
        List<CreateMenuServiceRequestDto> dtoList = new ArrayList<>();

        // when
        ApiResponse<List<Menu>> response = menuService.createMenu(dtoList);

        // then
        assertNotNull(response);
        assertEquals(ErrorCode.MENU_IS_EMPTY.getStatus(), response.getStatus());
        assertEquals(ErrorCode.MENU_IS_EMPTY.getMessage(), response.getMessage());

        // verify
        Mockito.verify(menuRepository, never()).existsByMenuTitle(any(MenuTitle.class));
        Mockito.verify(menuRepository, never()).save(any(Menu.class));
    }

    @Test
    @DisplayName("메뉴 생성 실패 테스트 - 이미 존재하는 메뉴")
    void createMenuFail_AlreadyExisted() {
        // given
        List<CreateMenuServiceRequestDto> dtoList = new ArrayList<>();
        dtoList.add(new CreateMenuServiceRequestDto(MenuTitle.ABOUT, true));
        dtoList.add(new CreateMenuServiceRequestDto(MenuTitle.ARTWORK, true));
        dtoList.add(new CreateMenuServiceRequestDto(MenuTitle.CONTACT, true));
        dtoList.add(new CreateMenuServiceRequestDto(MenuTitle.FAQ, true));
        dtoList.add(new CreateMenuServiceRequestDto(MenuTitle.RECRUITMENT, true));
        dtoList.add(new CreateMenuServiceRequestDto(MenuTitle.NEWS, true));

        // stub
        when(menuRepository.existsByMenuTitle(MenuTitle.ABOUT)).thenReturn(true);

        // when
        ApiResponse<List<Menu>> response = menuService.createMenu(dtoList);

        // then
        assertNotNull(response);
        assertEquals(ErrorCode.ALREADY_EXISTED_MENU.getStatus(), response.getStatus());
        assertEquals(ErrorCode.ALREADY_EXISTED_MENU.getMessage(), response.getMessage());

        // verify
        Mockito.verify(menuRepository, times(1)).existsByMenuTitle(any(MenuTitle.class));
        Mockito.verify(menuRepository, never()).save(any(Menu.class));
    }

//    @Test
//    @DisplayName("메뉴 생성 성공 테스트")
//    void createMenuSuccess() {
//        // given
//        CreateMenuServiceRequestDto requestDto = new CreateMenuServiceRequestDto(
//                MenuTitle.ALL,
//                true
//        );
//        // stub
//        when(menuRepository.save(any(Menu.class))).thenReturn(requestDto.toEntity(0));
//
//        // when
//        ApiResponse<Menu> response = menuService.createMenu(requestDto);
//        Menu menu = response.getData();
//
//        // then
//        assertNotNull(menu);
//        Assertions.assertThat(menu.getMenuTitle()).isEqualTo(requestDto.menuTitle());
//        Assertions.assertThat(menu.getVisibility()).isEqualTo(requestDto.visibility());
//        assertEquals(HttpStatus.OK, response.getStatus());
//        // verify
//        Mockito.verify(menuRepository, times(1)).save(any(Menu.class));
//    }
//
//    @Test
//    @DisplayName("메뉴 생성 실패 테스트 - 중복된 메뉴가 이미 존재")
//    void createMenuFail() {
//        // given
//        CreateMenuServiceRequestDto requestDto = new CreateMenuServiceRequestDto(
//                MenuTitle.ALL,
//                true
//        );
//        // stub
//        when(menuRepository.existsByMenuTitle(requestDto.menuTitle())).thenReturn(true);
//
//        // when, then
//        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
//            menuService.createMenu(requestDto);
//        });
//
//        // then
//        assertEquals("이미 동일한 메뉴가 존재합니다.", exception.getMessage());
//        // verify
//        verify(menuRepository, never()).save(any(Menu.class));
//    }

    @Test
    @DisplayName("PA 메뉴 목록 조회 성공 테스트")
    void retrieveAllMenuSuccess() {
        // given
        List<Menu> menuList = new ArrayList<>();
        menuList.add(new Menu(MenuTitle.ALL, true, 1));
        menuList.add(new Menu(MenuTitle.MAIN, true, 2));
        menuList.add(new Menu(MenuTitle.ABOUT, true, 3));
        // stub
        when(menuRepository.findAll()).thenReturn(menuList);

        // when
        ApiResponse<List<Menu>> response = menuService.retrieveAllMenu();
        List<Menu> findMenuList = response.getData();

        // then
        Assertions.assertThat(findMenuList).isEqualTo(menuList);
        Assertions.assertThat(findMenuList.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("PA 메뉴 목록 조회 실패 테스트")
    void retrieveAllMenuFail() {
        // given
        List<Menu> menuList = new ArrayList<>();
        // stub
        when(menuRepository.findAll()).thenReturn(menuList);

        // when
        ApiResponse<List<Menu>> response = menuService.retrieveAllMenu();
        List<Menu> findMenuList = response.getData();

        // then
        Assertions.assertThat(response.getData()).isNull();
        Assertions.assertThat(response.getMessage()).isEqualTo("메뉴가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("PP 메뉴 제목 목록 조회 성공 테스트")
    void retrieveMenuSuccess() {
        // given
        List<MenuTitle> menuTitleList = new ArrayList<>();
        menuTitleList.add(MenuTitle.ALL);
        menuTitleList.add(MenuTitle.MAIN);
        menuTitleList.add(MenuTitle.ABOUT);
        // stub
        when(menuRepository.findTitleByVisibilityTrue()).thenReturn(menuTitleList);

        // when
        ApiResponse<List<MenuTitle>> response = menuService.retrieveMenu();
        List<MenuTitle> findMenutitleList = response.getData();

        // then
        Assertions.assertThat(findMenutitleList).isEqualTo(menuTitleList);
        Assertions.assertThat(findMenutitleList.size()).isEqualTo(3);
    }
    
    @Test
    @DisplayName("PP 메뉴 제목 목록 조회 실패 테스트")
    void retrieveMenuFail() {
        // given
        List<MenuTitle> menuTitleList = new ArrayList<>();
        // stub
        when(menuRepository.findTitleByVisibilityTrue()).thenReturn(menuTitleList);

        // when
        ApiResponse<List<MenuTitle>> response = menuService.retrieveMenu();
        List<MenuTitle> findMenutitleList = response.getData();

        // then
        Assertions.assertThat(response.getData()).isNull();
        Assertions.assertThat(response.getMessage()).isEqualTo("공개된 메뉴가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("PA 메뉴 수정 성공 테스트")
    void updateMenuSuccess() {
        // given
        List<UpdateMenuRequestDto> dtos = List.of(
                new UpdateMenuRequestDto(1L, false, 2) // id, visibility, sequence
        );

        Menu existingMenu = new Menu(MenuTitle.ALL, true, 1); // 기존 메뉴: sequence 1
        // stub
        when(menuRepository.findById(1L)).thenReturn(Optional.of(existingMenu));
        when(menuRepository.save(any(Menu.class))).thenReturn(existingMenu);

        // when
        ApiResponse<List<Menu>> response = menuService.updateMenu(dtos);
        List<Menu> updatedMenus = response.getData();

        // then:
        assertNotNull(updatedMenus);
        assertEquals(1, updatedMenus.size());
        Menu updatedMenu = updatedMenus.get(0);
        assertFalse(updatedMenu.getVisibility());
        assertEquals(2, updatedMenu.getSequence());

        // verify
        verify(menuRepository, times(1)).findById(1L);
        verify(menuRepository, times(1)).save(any(Menu.class));
    }

    @Test
    @DisplayName("PA 메뉴 수정 실패 테스트")
    void updateMenuFail() {
        // given
        List<UpdateMenuRequestDto> dtos = List.of(
                new UpdateMenuRequestDto(99L, false, 2)
        );
        // stub
        when(menuRepository.findById(99L)).thenReturn(Optional.empty());

        // when, then:
        ApiResponse<List<Menu>> response = menuService.updateMenu(dtos);

        // then
        assertNotNull(response);
        assertEquals(ErrorCode.INVALID_MENU_ID.getStatus(), response.getStatus());
        assertEquals(ErrorCode.INVALID_MENU_ID.getMessage(), response.getMessage());

        // verify
        verify(menuRepository, times(1)).findById(99L);
        verify(menuRepository, never()).save(any(Menu.class));
    }

    @Test
    @DisplayName("PA 메뉴 삭제 성공 테스트")
    void deleteMenuSuccess() {
        // given
        Long menuId = 1L;
        Menu existingMenu = new Menu(MenuTitle.ALL, true, 1);
        when(menuRepository.findById(menuId)).thenReturn(Optional.of(existingMenu)); // 존재하는 메뉴를 반환
        // when
        ApiResponse<Menu> response = menuService.deleteMenu(menuId);

        // then
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("메뉴를 성공적으로 삭제했습니다.", response.getMessage());
        // verify
        Mockito.verify(menuRepository, times(1)).delete(existingMenu);
    }

    @Test
    @DisplayName("PA 메뉴 삭제 실패 테스트")
    void deleteMenuFail() {
        // given
        Long menuId = 1L;
        when(menuRepository.findById(menuId)).thenReturn(Optional.empty()); // 존재하지 않는 메뉴를 반환

        // when
        ApiResponse<Menu> response = menuService.deleteMenu(menuId);

        // then
        assertEquals(ErrorCode.INVALID_MENU_ID.getStatus(), response.getStatus()); // 오류 코드 확인
        assertEquals(ErrorCode.INVALID_MENU_ID.getMessage(), response.getMessage()); // 오류 코드 확인
        // verify
        Mockito.verify(menuRepository, never()).delete(any(Menu.class)); // delete 호출되지 않음 확인
    }


}