package studio.studioeye.domain.partner_information.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import studio.studioeye.domain.partner_information.dao.PartnerInformationRepository;
import studio.studioeye.domain.partner_information.domain.PartnerInformation;
import studio.studioeye.domain.partner_information.dto.request.CreatePartnerInfoServiceRequestDto;
import studio.studioeye.domain.partner_information.dto.request.UpdatePartnerInfoServiceRequestDto;
import studio.studioeye.infrastructure.s3.S3Adapter;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;

import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class PartnerInformationService {

	private final PartnerInformationRepository partnerInformationRepository;
	private final S3Adapter s3Adapter;


	public ApiResponse<PartnerInformation> createPartnerInfo(CreatePartnerInfoServiceRequestDto dto, MultipartFile logoImg) {
		String logoImgStr = getImgUrl(logoImg);
		if (logoImgStr.isEmpty()) return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);

		PartnerInformation partnerInformation = dto.toEntity(logoImgStr);

		PartnerInformation savedPartnerInformation = partnerInformationRepository.save(partnerInformation);
		return ApiResponse.ok("협력사 정보를 성공적으로 등록하였습니다.", savedPartnerInformation);
	}

	private String getImgUrl(MultipartFile logoImg) {
		ApiResponse<String> updateFileResponse = s3Adapter.uploadImage(logoImg);
		if (updateFileResponse == null || updateFileResponse.getStatus().is5xxServerError()) {
			return "";
		}
		return updateFileResponse.getData();
	}


	public ApiResponse<List<Map<String, Object>>> retrieveAllPartnerInfo() {
		List<PartnerInformation> partnerInformationList = partnerInformationRepository.findAll();
		if (partnerInformationList.isEmpty()){
			return ApiResponse.ok("협력사 정보가 존재하지 않습니다.");
		}

		List<Map<String, Object>> responseList = new ArrayList<>();
		for (PartnerInformation partnerInformation : partnerInformationList) {
			Map<String, Object> responseBody = getResponseBody(partnerInformation);
			responseList.add(responseBody);
		}

		return ApiResponse.ok("협력사 정보 목록을 성공적으로 조회했습니다.", responseList);
	}

	public ApiResponse<Map<String, Object>> retrievePartnerInfo(Long partnerId) {
		Optional<PartnerInformation> optionalPartnerInformation = partnerInformationRepository.findById(partnerId);
		if(optionalPartnerInformation.isEmpty()){
			return ApiResponse.withError(ErrorCode.INVALID_PARTNER_INFORMATION_ID);
		}

		PartnerInformation partnerInformation = optionalPartnerInformation.get();
		Map<String, Object> responseBody = getResponseBody(partnerInformation);
		return ApiResponse.ok("협력사 정보를 성공적으로 조회했습니다.", responseBody);
	}

	public ApiResponse<List<String>> retrieveAllPartnerLogoImgList() {
		List<PartnerInformation> partnerList = partnerInformationRepository.findAll();
		if (partnerList.isEmpty()){
			return ApiResponse.ok("협력사 정보가 존재하지 않습니다.");
		}

		List<String> logoImgList = new ArrayList<>();
		for (PartnerInformation partnerInformation : partnerList) {
			logoImgList.add(partnerInformation.getLogoImageUrl());
		}

		return ApiResponse.ok("협력사 로고 이미지 리스트를 성공적으로 조회했습니다.", logoImgList);
	}

	public Page<PartnerInformation> retrievePartnerInformationPage(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return partnerInformationRepository.findAll(pageable);
	}

	private static Map<String, Object> getResponseBody(PartnerInformation partnerInformation) {
		// post와 get의 구조 통일
		LinkedHashMap<String, Object> responseBody = new LinkedHashMap<>();
		responseBody.put("partnerInfo", Map.of(
				"id", partnerInformation.getId(),
				"name", partnerInformation.getName(),
				"is_main", partnerInformation.getIs_main(),
				"link", partnerInformation.getLink()

		));
		responseBody.put("logoImg", partnerInformation.getLogoImageUrl());
		return responseBody;
	}

	public ApiResponse<PartnerInformation> updatePartnerInfo(UpdatePartnerInfoServiceRequestDto dto, MultipartFile logoImg) {
		Optional<PartnerInformation> optionalPartnerInformation = partnerInformationRepository.findById(dto.id());
		if(optionalPartnerInformation.isEmpty()){
			return ApiResponse.withError(ErrorCode.INVALID_PARTNER_INFORMATION_ID);
		}
		PartnerInformation partnerInformation = optionalPartnerInformation.get();

		String logoImgStr = null;
		if(!logoImg.isEmpty()) logoImgStr = getImgUrl(logoImg);
		if (logoImgStr.isEmpty()) return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);

		if(!logoImgStr.equals(partnerInformation.getLogoImageUrl())) {
			ApiResponse<String> deleteFileResponse = s3Adapter.deleteFile(partnerInformation.getLogoImageUrl().split("/")[3]);
			if(deleteFileResponse.getStatus().is5xxServerError()){
				return ApiResponse.withError(ErrorCode.ERROR_S3_DELETE_OBJECT);
			}
			partnerInformation.setLogoImageUrl(logoImgStr);
		}
		partnerInformation.setName(dto.name());
		partnerInformation.setIs_main(dto.is_main());
		partnerInformation.setLink(dto.link());

		PartnerInformation savedPartnerInformation = partnerInformationRepository.save(partnerInformation);
		return ApiResponse.ok("협력사 정보를 성공적으로 수정했습니다.", savedPartnerInformation);
	}

	public ApiResponse<PartnerInformation> updatePartnerInfoText(UpdatePartnerInfoServiceRequestDto dto) {
		Optional<PartnerInformation> optionalPartnerInformation = partnerInformationRepository.findById(dto.id());
		if(optionalPartnerInformation.isEmpty()){
			return ApiResponse.withError(ErrorCode.INVALID_PARTNER_INFORMATION_ID);
		}
		PartnerInformation partnerInformation = optionalPartnerInformation.get();

		partnerInformation.setName(dto.name());
		partnerInformation.setIs_main(dto.is_main());
		partnerInformation.setLink(dto.link());

		PartnerInformation savedPartnerInformation = partnerInformationRepository.save(partnerInformation);
		return ApiResponse.ok("협력사 정보를 성공적으로 수정했습니다.", savedPartnerInformation);
	}

	public ApiResponse<PartnerInformation> updatePartnerLogoImg(Long partnerId, MultipartFile logoImg) {
		Optional<PartnerInformation> optionalPartnerInformation = partnerInformationRepository.findById(partnerId);
		if (optionalPartnerInformation.isEmpty()) {
			return ApiResponse.withError(ErrorCode.INVALID_PARTNER_INFORMATION_ID);
		}

		PartnerInformation partner = optionalPartnerInformation.get();

		// 새로운 로고이미지 저장
		String logoImgStr = getImgUrl(logoImg);
		if (logoImgStr.isEmpty()) return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);
		partner.setLogoImageUrl(logoImgStr);

		PartnerInformation updatedPartner = partnerInformationRepository.save(partner);
		return ApiResponse.ok("협력사 로고 이미지를 성공적으로 수정했습니다.", updatedPartner);
	}

	public ApiResponse<String> deletePartnerInfo(Long partnerId) {
		Optional<PartnerInformation> optionalPartnerInformation = partnerInformationRepository.findById(partnerId);
		if(optionalPartnerInformation.isEmpty()){
			return ApiResponse.withError(ErrorCode.INVALID_PARTNER_INFORMATION_ID);
		}

		PartnerInformation partnerInformation = optionalPartnerInformation.get();
		ApiResponse<String> deleteFileResponse = s3Adapter.deleteFile(partnerInformation.getLogoImageUrl().split("/")[3]);
		if(deleteFileResponse.getStatus().is5xxServerError()){
			return ApiResponse.withError(ErrorCode.ERROR_S3_DELETE_OBJECT);
		}

		partnerInformationRepository.delete(partnerInformation);
		return ApiResponse.ok("협력사 정보를 성공적으로 삭제하였습니다.");
	}
}
