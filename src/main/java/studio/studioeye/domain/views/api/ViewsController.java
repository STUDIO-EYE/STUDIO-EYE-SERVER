package studio.studioeye.domain.views.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import studio.studioeye.domain.views.application.ViewsService;
import studio.studioeye.domain.views.domain.Views;
import studio.studioeye.domain.views.dto.request.CreateViewsRequestDto;
import studio.studioeye.global.common.response.ApiResponse;

import java.util.List;


@Tag(name = "조회수 API", description = "조회수 등록 / 수정  / 조회")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ViewsController {

    private final ViewsService viewsService;

    @Operation(summary = "조회수 등록 API")
    @PostMapping("/views")
    public ApiResponse<Views> createViews(@Valid @RequestBody CreateViewsRequestDto dto) {
        return viewsService.createViews(dto.toServiceViews());
    }

    @Operation(summary = "조회수 전체 조회 API")
    @GetMapping("/views")
    public ApiResponse<List<Views>> retrieveAllViews(){
        return viewsService.retrieveAllViews();
    }

    @Operation(summary = "id로 조회수 상세 조회 API")
    @GetMapping("/views/id/{viewsId}")
    public ApiResponse<Views> retrieveViewsById(@PathVariable Long viewsId){
        return viewsService.retrieveViewsById(viewsId);
    }

    @Operation(summary = "해당 연도 조회수 전체 조회 API")
    @GetMapping("/views/{year}")
    public ApiResponse<List<Views>> retrieveViewsByYear(@PathVariable Integer year){
        return viewsService.retrieveViewsByYear(year);
    }

    @Operation(summary = "연도, 월로 조회수 상세 조회 API")
    @GetMapping("/views/{year}/{month}")
    public ApiResponse<Views> retrieveViewsByYearMonth(@PathVariable Integer year, @PathVariable Integer month){
        return viewsService.retrieveViewsByYearMonth(year, month);
    }

    @Operation(summary = "기간(시작점(연도,월), 종료점(연도,월))으로 조회수 조회 API")
    @GetMapping("/views/{startYear}/{startMonth}/{endYear}/{endMonth}")
    public ApiResponse<List<Views>> retrieveViewsByPeriod(@PathVariable Integer startYear, @PathVariable Integer startMonth,
                                             @PathVariable Integer endYear, @PathVariable Integer endMonth){
        return viewsService.retrieveViewsByPeriod(startYear, startMonth, endYear, endMonth);
    }

    @Operation(summary = "id로 특정 월 조회수 1 상승 API")
    @PutMapping("/views/increase/{viewsId}")
    public ApiResponse<Views> updateViewsById(@PathVariable Long viewsId){
        return viewsService.updateViewsById(viewsId);
    }

    @Operation(summary = "연도, 월로 특정 월 조회수 1 상승 API")
    @PutMapping("/views/increase/{year}/{month}")
    public ApiResponse<Views> updateViewsByYearMonth(@PathVariable Integer year, @PathVariable Integer month){
        return viewsService.updateViewsByYearMonth(year, month);
    }

    @Operation(summary = "이번 월 조회수 1 상승 API (해당 월이 존재하지 않을 경우에는 생성)")
    @PutMapping("/views/increase")
    public ApiResponse<Views> updateThisMonthViews(@CookieValue(name = "viewed_cookie", required = false) String cookieValue) {
        return viewsService.updateThisMonthViews(cookieValue);
    }
}
