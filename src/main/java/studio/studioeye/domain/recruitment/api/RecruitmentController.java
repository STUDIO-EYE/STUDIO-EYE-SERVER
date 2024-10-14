package studio.studioeye.domain.recruitment.api;

import studio.studioeye.domain.recruitment.application.RecruitmentService;
import studio.studioeye.domain.recruitment.dao.RecruitmentTitle;
import studio.studioeye.domain.recruitment.domain.Recruitment;
import studio.studioeye.domain.recruitment.dto.request.CreateRecruitmentRequestDto;
import studio.studioeye.domain.recruitment.dto.request.UpdateRecruitmentRequestDto;
import studio.studioeye.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Tag(name = "채용공고 게시판 API", description = "채용공고 게시물 등록 / 수정 / 삭제 / 조회")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RecruitmentController {

    private final RecruitmentService recruitmentService;

    @Operation(summary = "채용공고 게시물 등록 API")
    @PostMapping("/recruitment")
    public ApiResponse<Recruitment> createRecruitment(@Valid @RequestBody CreateRecruitmentRequestDto dto) {
        return recruitmentService.createRecruitment(dto.toServiceRequest());
    }

    @Operation(summary = "채용공고 게시물 목록 조회 API")
    @GetMapping("/recruitment")
    public ApiResponse<Page<RecruitmentTitle>> retrieveRecruitmentList(@RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "10") int size) {
        return recruitmentService.retrieveRecruitmentList(page, size);
    }

    @Operation(summary = "채용공고 게시물 조회 API")
    @GetMapping("/recruitment/{id}")
    public ApiResponse<Recruitment> retrieveRecruitmentById(@PathVariable Long id) {
        return recruitmentService.retrieveRecruitmentById(id);
    }

    @Operation(summary = "최신 채용공고 1개만 조회 API")
    @GetMapping("/recruitment/recent")
    public ApiResponse<Recruitment> retrieveRecentRecruitment() {
        return recruitmentService.retrieveRecentRecruitment();
    }

    @Operation(summary = "채용공고 게시물 수정 API")
    @PutMapping("/recruitment")
    public ApiResponse<Recruitment> updateRecruitment(@RequestBody UpdateRecruitmentRequestDto dto) {
        return recruitmentService.updateRecruitment(dto.toServiceRequest());
    }

    @Operation(summary = "채용공고 게시물 삭제 API")
    @DeleteMapping("/recruitment/{id}")
    public ApiResponse<String> deleteRecruitment(@PathVariable Long id) {
        return recruitmentService.deleteRecruitment(id);
    }
}
