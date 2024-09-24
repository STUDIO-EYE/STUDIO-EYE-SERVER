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

    @Operation(summary = "FAQ base64 이미지 저장 후 url 반환 API")
    @PostMapping("/faq/image")
    public ApiResponse<String> convertBase64ToImageUrl(@RequestBody String base64Code) throws IOException {
        return faqService.convertBase64ToImageUrl(base64Code);
    }

    @Operation(summary = "FAQ 전체 조회 API")
    @GetMapping("/faq")
    public ApiResponse<List<Faq>> retrieveAllFaq() {
        return faqService.retrieveAllFaq();
    }

    @Operation(summary = "FAQ 제목(id, 제목) 전체 조회 API")
    @GetMapping("/faq/title")
    public ApiResponse<List<FaqQuestions>> retrieveAllFaqTitle() {
        return faqService.retrieveAllFaqTitle();
    }

    @Operation(summary = "id로 FAQ 상세 조회 API")
    @GetMapping("/faq/{id}")
    public ApiResponse<Faq> retrieveFaqById(@PathVariable Long id) {
        return faqService.retrieveFaqById(id);
    }

    @Operation(summary = "FAQ 페이지네이션 조회 API")
    @GetMapping("/faq/page")
    public Page<Faq> retrieveFaqPage(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size) {
        return faqService.retrieveFaqPage(page, size);
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

    @Operation(summary = "id로 FAQ 복수 삭제 API")
    @DeleteMapping("/faq")
    public ApiResponse<String> deleteFaqs(@RequestBody List<Long> ids) {
        return faqService.deleteFaqs(ids);
    }
}
