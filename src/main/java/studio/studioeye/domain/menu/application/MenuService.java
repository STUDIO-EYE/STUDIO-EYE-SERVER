package studio.studioeye.domain.menu.application;

import studio.studioeye.domain.menu.dao.MenuRepository;
import studio.studioeye.domain.menu.domain.Menu;
import studio.studioeye.domain.menu.dto.request.CreateMenuServiceRequestDto;
import studio.studioeye.domain.menu.dto.request.UpdateMenuServiceRequestDto;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;

    public ApiResponse<Menu> createMenu(CreateMenuServiceRequestDto dto) {
        Menu menu = dto.toEntity();
        Menu savedMenu = menuRepository.save(menu);
        return ApiResponse.ok("메뉴를 성공적으로 등록하였습니다.", savedMenu);
    }
    public ApiResponse<List<Menu>> retrieveAllMenu() {
        List<Menu> menuList = menuRepository.findAll();
        if(menuList.isEmpty()) {
            return ApiResponse.ok("메뉴가 존재하지 않습니다.");
        }
        return ApiResponse.ok("메뉴 목록을 성공적으로 조회했습니다.", menuList);
    }

    public ApiResponse<List<String>> retrieveMenu() {
        List<String> menuTitleList = menuRepository.findTitleByVisibilityTrue();
        if(menuTitleList.isEmpty()) {
            return ApiResponse.ok("공개된 메뉴가 존재하지 않습니다.");
        }
        return ApiResponse.ok("공개된 메뉴 목록을 성공적으로 조회했습니다.", menuTitleList);
    }

    public ApiResponse<Menu> updateMenu(UpdateMenuServiceRequestDto dto) {
        Optional<Menu> optionalMenu = menuRepository.findById(dto.id());
        if(optionalMenu.isEmpty()) {
            return ApiResponse.withError(ErrorCode.INVALID_MENU_ID);
        }
        Menu menu = optionalMenu.get();
        menu.update(dto);
        Menu updatedMenu = menuRepository.save(menu);
        return ApiResponse.ok("메뉴를 성공적으로 수정했습니다.", updatedMenu);
    }


    public ApiResponse<Menu> deleteMenu(Long id) {
        Optional<Menu> optionalMenu = menuRepository.findById(id);
        if(optionalMenu.isEmpty()) {
            return ApiResponse.withError(ErrorCode.INVALID_MENU_ID);
        }

        Menu menu = optionalMenu.get();
        menuRepository.delete(menu);

        return ApiResponse.ok("메뉴를 성공적으로 삭제했습니다.");
    }
}
