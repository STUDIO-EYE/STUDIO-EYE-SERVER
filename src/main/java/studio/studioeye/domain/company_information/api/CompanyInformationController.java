package studio.studioeye.domain.company_information.api;

import studio.studioeye.domain.company_information.application.CompanyInformationService;
import studio.studioeye.domain.company_information.dao.CompanyBasicInformation;
import studio.studioeye.domain.company_information.dao.CompanyIntroductionInformation;
import studio.studioeye.domain.company_information.domain.CompanyInformation;
import studio.studioeye.domain.company_information.domain.CompanyInformationDetailInformation;
import studio.studioeye.domain.company_information.dto.request.*;
import studio.studioeye.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "회사 정보(기본 정보, 소개 정보, 상세 정보) API", description = "회사 정보(기본 정보, 소개 정보, 상세 정보) 등록 / 수정 / 삭제 / 조회")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CompanyInformationController {
    private final CompanyInformationService companyInformationService;


    @Operation(summary = "회사 정보 등록 API")
    @PostMapping("/company/information")
    public ApiResponse<CompanyInformation> createCompanyInformation(@Valid @RequestPart("request") CreateCompanyInformationRequestDto dto,
                                                    @RequestPart(value = "lightLogoImage", required = false) MultipartFile lightLogoImage,
                                                    @RequestPart(value = "darkLogoImage", required = false) MultipartFile darkLogoImage,
                                                    @RequestPart(value = "sloganImage", required = false) MultipartFile sloganImage) throws IOException {
        return companyInformationService.createCompanyInformation(dto.toServiceRequest(), lightLogoImage, darkLogoImage, sloganImage);
    }

    @Operation(summary = "회사 전체 정보 조회 API")
    @GetMapping("/company/information")
    public ApiResponse<CompanyInformation> retrieveAllCampanyInformation() {
        return companyInformationService.retrieveAllCampanyInformation();
    }

    @Operation(summary = "회사 로고 이미지 조회 API")
    @GetMapping("/company/logo/{isLight}")
    public ApiResponse<String> retrieveCampanyLogoImage(@PathVariable boolean isLight) {
        return companyInformationService.retrieveCampanyLogoImage(isLight);
    }

    @Operation(summary = "회사 기본 정보(주소, 영문주소, 유선번호, 팩스번호) 조회 API")
    @GetMapping("/company/basic")
    public ApiResponse<CompanyBasicInformation> retrieveCompanyBasicInformation() {
        return companyInformationService.retrieveCompanyBasicInformation();
    }

    @Operation(summary = "회사 소개 정보 조회 API")
    @GetMapping("/company/introduction")
    public ApiResponse<CompanyIntroductionInformation> retrieveCompanyIntroductionInformation() {
        return companyInformationService.retrieveCompanyIntroductionInformation();
    }

    @Operation(summary = "회사 5가지 상세 정보 조회 API")
    @GetMapping("/company/detail")
    public ApiResponse<List<CompanyInformationDetailInformation>> retrieveCompanyDetailInformation() {
        return companyInformationService.retrieveCompanyDetailInformation();
    }

    @Operation(summary = "회사 전체 정보 수정 API")
    @PutMapping("/company/information")
    public ApiResponse<CompanyInformation> updateAllCompanyInformation(@Valid @RequestPart("request") UpdateAllCompanyInformationRequestDto dto,
                                                   @RequestPart(value = "lightLogoImage", required = false) MultipartFile lightLogoImage,
                                                   @RequestPart(value = "darkLogoImage", required = false) MultipartFile darkLogoImage,
                                                   @RequestPart(value = "sloganImageUrl", required = false) MultipartFile sloganImageUrl) throws IOException {
        return companyInformationService.updateAllCompanyInformation(dto.toServiceRequest(), lightLogoImage, darkLogoImage, sloganImageUrl);
    }
    @Operation(summary = "회사 전체 텍스트 정보(이미지 제외) 수정 API")
    @PutMapping("/company/information/modify")
    public ApiResponse<CompanyInformation> updateAllCompanyInformation(@Valid @RequestPart("request") UpdateAllCompanyInformationRequestDto dto) {
        return companyInformationService.updateAllCompanyTextInformation(dto.toServiceRequest());
    }

    @Operation(summary = "회사 로고 이미지 수정 API")
    @PutMapping("/company/logo")
    public ApiResponse<CompanyInformation> updateCompanyLogoImage(@RequestPart(value = "lightLogoImage", required = false) MultipartFile lightLogoImage,
                                                                  @RequestPart(value = "darkLogoImage", required = false) MultipartFile darkLogoImage) throws IOException {
        return companyInformationService.updateCompanyLogoImage(lightLogoImage, darkLogoImage);
    }

    @Operation(summary = "회사 슬로건 이미지 수정 API")
    @PutMapping("/company/slogan")
    public ApiResponse<CompanyInformation> updateCompanySloganImage(@RequestPart(value = "sloganImageUrl", required = false) MultipartFile sloganImageUrl) throws IOException {
        return companyInformationService.updateCompanySloganImage(sloganImageUrl);
    }

    @Operation(summary = "회사 로고, 슬로건 이미지 수정 API")
    @PutMapping("/company/logo&slogan")
    public ApiResponse<CompanyInformation> updateCompanyLogoImage(@RequestPart(value = "lightLogoImage", required = false) MultipartFile lightLogoImage,
                                                                  @RequestPart(value = "darkLogoImage", required = false) MultipartFile darkLogoImage,
                                                                  @RequestPart(value = "sloganImageUrl", required = false) MultipartFile sloganImageUrl) throws IOException {
        return companyInformationService.updateCompanyLogoAndSlogan(lightLogoImage, darkLogoImage, sloganImageUrl);
    }

    @Operation(summary = "회사 기본 정보(주소, 유선번호, 팩스번호) 수정 API")
    @PutMapping("/company/basic")
    public ApiResponse<CompanyInformation> updateCompanyBasicInformation(@Valid @RequestBody UpdateCompanyBasicInformationRequestDto dto) {
        return companyInformationService.updateCompanyBasicInformation(dto.toServiceRequest());
    }

    @Operation(summary = "회사 소개 정보(mainOverview, commitment, introduction) 수정 API")
    @PutMapping("/company/introduction")
    public ApiResponse<CompanyInformation> updateCompanyIntroductionInformation(@Valid @RequestBody UpdateCompanyIntroductionInformationRequestDto dto) {
        return companyInformationService.updateCompanyIntroductionInformation(dto.toServiceRequest());
    }

    @Operation(summary = "회사 5가지 상세 정보 수정 API")
    @PutMapping("/company/detail")
    public ApiResponse<CompanyInformation> updateCompanyDetailInformation(@Valid @RequestBody UpdateCompanyDetailInformationRequestDto dto) {
        return companyInformationService.updateCompanyDetailInformation(dto.toServiceRequest());
    }

    @Operation(summary = "회사 전체 정보 삭제 API")
    @DeleteMapping("/company/information")
    public ApiResponse<String> deleteAllCompanyInformation() {
        return companyInformationService.deleteAllCompanyInformation();
    }

    @Operation(summary = "회사 로고 이미지 삭제 API")
    @DeleteMapping("/company/logo")
    public ApiResponse<String> deleteCompanyLogoImage() {
        return companyInformationService.deleteCompanyLogoImage();
    }

    @Operation(summary = "회사 기본 정보(주소, 영문주소, 유선번호, 팩스번호) 삭제 API")
    @DeleteMapping("/company/basic")
    public ApiResponse<CompanyInformation> deleteCompanyBasicInformation() {
        return companyInformationService.deleteCompanyBasicInformation();
    }

    @Operation(summary = "회사 소개 정보 삭제 API")
    @DeleteMapping("/company/introduction")
    public ApiResponse<CompanyInformation> deleteCompanyIntroductionInformation() {
        return companyInformationService.deleteCompanyIntroductionInformation();
    }

    @Operation(summary = "회사 5가지 상세 정보 삭제 API")
    @DeleteMapping("/company/detail")
    public ApiResponse<CompanyInformation> deleteCompanyDetailInformation() {
        return companyInformationService.deleteCompanyDetailInformation();
    }
}
