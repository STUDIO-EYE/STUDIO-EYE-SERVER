package studio.studioeye.domain.menu.application;

import studio.studioeye.domain.menu.dao.MenuRepository;
import studio.studioeye.domain.menu.domain.Menu;
import studio.studioeye.domain.menu.domain.MenuTitle;
import studio.studioeye.domain.menu.dto.request.CreateMenuServiceRequestDto;
import studio.studioeye.domain.menu.dto.request.UpdateMenuRequestDto;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;

    public ApiResponse<Menu> createMenu(CreateMenuServiceRequestDto dto) {
        // 중복 체크: 동일한 menuTitle이 이미 존재하는지 확인
        if (menuRepository.existsByMenuTitle(dto.menuTitle())) {
            throw new IllegalArgumentException("이미 동일한 메뉴가 존재합니다.");
        }

        Long totalCount = menuRepository.count();
        Menu menu = dto.toEntity(totalCount.intValue());
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

    public ApiResponse<List<MenuTitle>> retrieveMenu() {
        List<MenuTitle> menuTitleList = menuRepository.findTitleByVisibilityTrue();
        if(menuTitleList.isEmpty()) {
            return ApiResponse.ok("공개된 메뉴가 존재하지 않습니다.");
        }
        return ApiResponse.ok("공개된 메뉴 목록을 성공적으로 조회했습니다.", menuTitleList);
    }

    public ApiResponse<List<Menu>> updateMenu(List<UpdateMenuRequestDto> dtos) {
        List<Menu> updatedMenuList = new ArrayList<>();
        for(UpdateMenuRequestDto dto : dtos) {
            Optional<Menu> optionalMenu = menuRepository.findById(dto.id());
            if (optionalMenu.isEmpty()) {
                return ApiResponse.withError(ErrorCode.INVALID_MENU_ID);
            }
            Menu menu = optionalMenu.get();
            menu.update(dto);
            Menu updatedMenu = menuRepository.save(menu);
            updatedMenuList.add(updatedMenu);
        }
        return ApiResponse.ok("메뉴를 성공적으로 수정했습니다.", updatedMenuList);
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
