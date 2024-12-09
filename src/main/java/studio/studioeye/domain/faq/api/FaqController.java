package studio.studioeye.domain.faq.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import studio.studioeye.domain.faq.application.FaqService;
import studio.studioeye.domain.faq.dao.FaqQuestions;
import studio.studioeye.domain.faq.domain.Faq;
import studio.studioeye.domain.faq.dto.request.CreateFaqRequestDto;
import studio.studioeye.domain.faq.dto.request.UpdateFaqRequestDto;
import studio.studioeye.global.common.response.ApiResponse;

import java.io.IOException;
import java.util.List;

@Tag(name = "FAQ API", description = "FAQ 등록 / 수정 / 삭제 / 조회")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;

    @Operation(summary = "FAQ 등록 API")
    @PostMapping("/faq")
    public ApiResponse<Faq> createFaq(@Valid @RequestBody CreateFaqRequestDto dto) {
        return faqService.createFaq(dto.toServiceFaq());
    }

    @Operation(summary = "FAQ 전체 조회 API")
    @GetMapping("/faq")
    public ApiResponse<List<Faq>> retrieveAllFaq() {
        return faqService.retrieveAllFaq();
    }

    @Operation(summary = "FAQ 수정 API")
    @PutMapping("/faq")
    public ApiResponse<Faq> updateFaq(@Valid @RequestBody UpdateFaqRequestDto dto) {
        return faqService.updateFaq(dto.toServiceRequest());
    }

    @Operation(summary = "id로 FAQ 삭제 API")
    @DeleteMapping("/faq/{id}")
    public ApiResponse<String> deleteFaq(@PathVariable Long id) {
        return faqService.deleteFaq(id);
    }
}
