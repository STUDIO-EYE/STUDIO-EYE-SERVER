package studio.studioeye.domain.ceo.api;

import studio.studioeye.domain.ceo.application.CeoService;
import studio.studioeye.domain.ceo.domain.Ceo;
import studio.studioeye.domain.ceo.dto.request.CreateCeoRequestDto;
import studio.studioeye.domain.ceo.dto.request.UpdateCeoRequestDto;
import studio.studioeye.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "CEO 정보 API", description = "CEO 정보 등록 / 수정 / 삭제 / 조회")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CeoController {
    private final CeoService ceoService;

    @Operation(summary = "CEO 정보 등록 API")
    @PostMapping("/ceo")
    public ApiResponse<Ceo> createCeoInformation(@Valid @RequestPart("request") CreateCeoRequestDto dto,
                                                 @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        return ceoService.createCeoInformation(dto.toServiceRequest(), file);
    }

    @Operation(summary = "CEO 전체 정보 조회 API")
    @GetMapping("/ceo")
    public ApiResponse<Ceo> retrieveCeoInformation() {
        return ceoService.retrieveCeoInformation();
    }

    @Operation(summary = "CEO 전체 정보 수정 API")
    @PutMapping("/ceo")
    public ApiResponse<Ceo> updateCeoInformation(@Valid @RequestPart("request") UpdateCeoRequestDto dto,
                                            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ceoService.updateCeoInformation(dto.toServiceRequest(), file);
    }

    @Operation(summary = "CEO 텍스트(이미지 제외) 정보 수정 API")
    @PutMapping("/ceo/modify")
    public ApiResponse<Ceo> updateCeoTextInformation(@Valid @RequestPart("request") UpdateCeoRequestDto dto) {
        return ceoService.updateCeoTextInformation(dto.toServiceRequest());
    }

    @Operation(summary = "CEO 이미지 정보 수정 API")
    @PutMapping("/ceo/image")
    public ApiResponse<Ceo> updateCeoImageInformation(@RequestPart(value = "file", required = false) MultipartFile file) {
        return ceoService.updateCeoImageInformation(file);
    }

    @Operation(summary = "CEO 전체 정보 삭제 API")
    @DeleteMapping("/ceo")
    public ApiResponse<String> deleteCeoInformation() {
        return ceoService.deleteCeoInformation();
    }
}
