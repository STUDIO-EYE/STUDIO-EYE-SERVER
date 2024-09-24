package studio.studioeye.domain.partner_information.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import studio.studioeye.domain.partner_information.application.PartnerInformationService;
import studio.studioeye.domain.partner_information.domain.PartnerInformation;
import studio.studioeye.domain.partner_information.dto.request.CreatePartnerInfoRequestDto;
import studio.studioeye.domain.partner_information.dto.request.UpdatePartnerInfoRequestDto;
import studio.studioeye.global.common.response.ApiResponse;

import java.util.List;
import java.util.Map;

@Tag(name = "협력사 API", description = "협력사 목록 전체 조회 / 협력사 상세 조회 / 저장 / 삭제")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PartnerInformationController {

	private final PartnerInformationService partnerInformationService;

	@Operation(summary = "협력사 정보 등록 API")
	@PostMapping("/partners")
	public ApiResponse<PartnerInformation> createPartnerInfo(@Valid @RequestPart("partnerInfo") CreatePartnerInfoRequestDto dto,
										 @RequestPart(value = "logoImg", required = false) MultipartFile logoImg){
		return partnerInformationService.createPartnerInfo(dto.toServiceRequest(), logoImg);
	}

	@Operation(summary = "협력사 목록 전체 조회 API")
	@GetMapping("/partners")
	public ApiResponse<List<Map<String, Object>>> retrieveAllPartnerInfo(){
		return partnerInformationService.retrieveAllPartnerInfo();
	}

	@Operation(summary = "협력사 정보 상세 조회 API")
	@GetMapping("/partners/{partnerId}")
	public ApiResponse<Map<String, Object>> retrievePartnerInfo(@PathVariable Long partnerId){
		return partnerInformationService.retrievePartnerInfo(partnerId);
	}

	@Operation(summary = "협력사 로고 이미지 리스트 조회 API")
	@GetMapping("/partners/logoImgList")
	public ApiResponse<List<String>> retrieveAllPartnerLogoImgList(){
		return partnerInformationService.retrieveAllPartnerLogoImgList();
	}

	@Operation(summary = "협력사 정보 페이지네이션 조회 API")
	@GetMapping("/partners/page")
	public Page<PartnerInformation> retrievePartnerInformationPage(@RequestParam(defaultValue = "0") int page,
																   @RequestParam(defaultValue = "10") int size) {
		return partnerInformationService.retrievePartnerInformationPage(page, size);
	}

	@Operation(summary = "협력사 전체 수정 API")
	@PutMapping("/partners")
	public ApiResponse<PartnerInformation> updatePartnerInfo(@Valid @RequestPart("partnerInfo") UpdatePartnerInfoRequestDto dto,
											@RequestPart(value = "logoImg", required = false) MultipartFile logoImg){
		return partnerInformationService.updatePartnerInfo(dto.toServiceRequest(), logoImg);
	}
	@Operation(summary = "협력사 텍스트(이미지 제외) 수정 API")
	@PutMapping("/partners/modify")
	public ApiResponse<PartnerInformation> updatePartnerInfoText(@Valid @RequestPart("partnerInfo") UpdatePartnerInfoRequestDto dto){
		return partnerInformationService.updatePartnerInfoText(dto.toServiceRequest());
	}

	@Operation(summary = "협력사 로고 이미지 수정 API")
	@PutMapping("/partners/{partnerId}/logoImg")
	public ApiResponse<PartnerInformation> updatePartnerLogoImg(@PathVariable Long partnerId,
																@RequestPart(value = "logoImg", required = false) MultipartFile logoImg){
		return partnerInformationService.updatePartnerLogoImg(partnerId, logoImg);
	}

	@Operation(summary = "협력사 정보 삭제 API")
	@DeleteMapping("/partners/{partnerId}")
	public ApiResponse<String> deletePartnerInfo(@PathVariable Long partnerId){
		return partnerInformationService.deletePartnerInfo(partnerId);
	}
}
