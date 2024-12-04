package studio.studioeye.domain.menu.api;

import studio.studioeye.domain.menu.application.MenuService;
import studio.studioeye.domain.menu.domain.Menu;
import studio.studioeye.domain.menu.domain.MenuTitle;
import studio.studioeye.domain.menu.dto.request.CreateMenuRequestDto;
import studio.studioeye.domain.menu.dto.request.UpdateMenuRequestDto;
import studio.studioeye.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "메뉴 관리 API", description = "메뉴 등록 / 수정 / 삭제 / 등록")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;

//    @Operation(summary = "PA용 메뉴 생성 API", description = "menuTitle은 MAIN, ABOUT, ARTWORK, FAQ, CONTACT, NEWS, RECRUITMENT 중에서 입력")
//    @PostMapping("/menu")
//    public ApiResponse<Menu> createMenu(@RequestBody CreateMenuRequestDto dto) {
//        return menuService.createMenu(dto.toServiceRequest());
//    }

    @Operation(summary = "PA용 메뉴 다중 생성 API", description = "menuTitle은 MAIN, ABOUT, ARTWORK, FAQ, CONTACT, NEWS, RECRUITMENT 중에서 입력")
    @PostMapping("/menu")
    public ApiResponse<List<Menu>> createMenu(@RequestBody List<CreateMenuRequestDto> dtoList) {
        return menuService.createMenu(dtoList.stream()
                .map(CreateMenuRequestDto::toServiceRequest)
                .toList());
    }

    @Operation(summary = "PA용 메뉴 전체 조회 API")
    @GetMapping("/menu/all")
    public ApiResponse<List<Menu>> retrieveAllMenu(){
        return menuService.retrieveAllMenu();
    }

    @Operation(summary = "PP용 메뉴 제목 목록 조회 API")
    @GetMapping("/menu")
    public ApiResponse<List<MenuTitle>> retrieveMenu() {
        return menuService.retrieveMenu();
    }

//    @Operation(summary = "PA용 메뉴 수정 API")
//    @PutMapping("/menu")
//    public ApiResponse<Menu> updateMenu(@RequestBody UpdateMenuRequestDto dto) {
//        return menuService.updateMenu(dto.toServiceRequest());
//    }

    @Operation(summary = "PA용 메뉴 수정 API")
    @PutMapping("/menu")
    public ApiResponse<List<Menu>> updateMenu(@RequestBody List<UpdateMenuRequestDto> dtos) {
        return menuService.updateMenu(dtos);
    }

    @Operation(summary = "PA용 메뉴 삭제 API")
    @DeleteMapping("/menu/{id}")
    public ApiResponse<Menu> deleteMenu(@PathVariable Long id) {
        return menuService.deleteMenu(id);
    }
}
