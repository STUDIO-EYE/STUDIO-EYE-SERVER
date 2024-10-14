package studio.studioeye.domain.benefit.api;

import studio.studioeye.domain.benefit.application.BenefitService;
import studio.studioeye.domain.benefit.domain.Benefit;
import studio.studioeye.domain.benefit.dto.request.CreateBenefitRequestDto;
import studio.studioeye.domain.benefit.dto.request.UpdateBenefitRequestDto;
import studio.studioeye.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "혜택 정보 API", description = "혜택 정보 등록 / 수정 / 삭제 / 조회")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BenefitController {
    private final BenefitService benefitService;

    @Operation(summary = "혜택 정보 등록 API")
    @PostMapping("/benefit")
    public ApiResponse<Benefit> createBenefit(@Valid @RequestPart("request") CreateBenefitRequestDto dto,
                                              @RequestPart(value = "file") MultipartFile file) throws IOException {
        return benefitService.createBenefit(dto.toServiceRequest(), file);
    }

    @Operation(summary = "혜택 정보 조회 API")
    @GetMapping("/benefit")
    public ApiResponse<List<Benefit>> retrieveBenefit() {
        return benefitService.retrieveBenefit();
    }

    @Operation(summary = "혜택 정보 수정 API")
    @PutMapping("/benefit")
    public ApiResponse<Benefit> updateBenefit(@Valid @RequestPart("request") UpdateBenefitRequestDto dto,
                                              @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        return benefitService.updateBenefit(dto.toServiceRequest(), file);
    }

    @Operation(summary = "혜택 텍스트(이미지 제외) 정보 수정 API")
    @PutMapping("/benefit/modify")
    public ApiResponse<Benefit> updateBenefitText(@Valid @RequestPart("request") UpdateBenefitRequestDto dto) {
        return benefitService.updateBenefitText(dto.toServiceRequest());
    }

    @Operation(summary = "혜택 정보 삭제 API")
    @DeleteMapping("/benefit/{benefitId}")
    public ApiResponse<String> deleteBenefit(@PathVariable Long benefitId) {
        return benefitService.deleteBenefit(benefitId);
    }
}
