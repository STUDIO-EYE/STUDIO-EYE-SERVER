package studio.studioeye.domain.news.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import studio.studioeye.domain.news.application.NewsService;
import studio.studioeye.domain.news.domain.News;
import studio.studioeye.domain.news.dto.CreateNewsRequestDto;
import studio.studioeye.domain.news.dto.UpdateNewsRequestDto;
import studio.studioeye.global.common.response.ApiResponse;

import java.util.List;

@Tag(name = "News API", description = "News 등록 / 수정 / 삭제 / 조회")
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @Operation(summary = "News 등록 API")
    @PostMapping("")
    public ApiResponse<News> createNews(@Valid @RequestBody CreateNewsRequestDto dto) {
        return newsService.createNews(dto.toServiceNews());
    }

    @Operation(summary = "News 전체 조회 API")
    @GetMapping("/all")
    public ApiResponse<List<News>> retrieveAllNews() {
        return newsService.retrieveAllNews();
    }

    @Operation(summary = "id로 News 상세 조회 API")
    @GetMapping("/{id}")
    public ApiResponse<News> retrieveNewsById(@PathVariable Long id) {
        return newsService.retrieveNewsById(id);
    }

    @Operation(summary = "News 수정 API")
    @PutMapping("")
    public ApiResponse<News> updateNews(@Valid @RequestBody UpdateNewsRequestDto dto) {
        return newsService.updateNews(dto.toServiceRequest());
    }

    @Operation(summary = "id로 News 삭제 API")
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteNews(@PathVariable Long id) {
        return newsService.deleteNews(id);
    }
}
