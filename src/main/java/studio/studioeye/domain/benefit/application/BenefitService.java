package studio.studioeye.domain.benefit.application;

import studio.studioeye.domain.benefit.dao.BenefitRepository;
import studio.studioeye.domain.benefit.domain.Benefit;
import studio.studioeye.domain.benefit.dto.request.CreateBenefitServiceRequestDto;
import studio.studioeye.domain.benefit.dto.request.UpdateBenefitServiceRequestDto;
import studio.studioeye.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import studio.studioeye.global.exception.error.ErrorCode;
import studio.studioeye.infrastructure.s3.S3Adapter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class BenefitService {
    private final BenefitRepository benefitRepository;
    private final S3Adapter s3Adapter;

    public ApiResponse<Benefit> createBenefit(CreateBenefitServiceRequestDto dto, MultipartFile file) throws IOException {
        if(file.isEmpty()) {
            return ApiResponse.withError(ErrorCode.NOT_EXIST_IMAGE_FILE);
        }
        ApiResponse<String> updateFileResponse = s3Adapter.uploadFile(file);
        if (updateFileResponse.getStatus().is5xxServerError()) {
            return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);
        }
        String imageUrl = updateFileResponse.getData();
        String imageFileName = file.getOriginalFilename();
        Benefit benefit = dto.toEntity(imageUrl, imageFileName);
        Benefit savedBenefit = benefitRepository.save(benefit);
        return ApiResponse.ok("혜택 정보를 성공적으로 등록하였습니다.", savedBenefit);
    }

    public ApiResponse<List<Benefit>> retrieveBenefit() {
        List<Benefit> benefits = benefitRepository.findAll();
        if(benefits.isEmpty()) {
            return ApiResponse.ok("혜택 정보가 존재하지 않습니다.");
        }
        return ApiResponse.ok("혜택 정보를 성공적으로 조회했습니다.", benefits);
    }

    public ApiResponse<Benefit> updateBenefit(UpdateBenefitServiceRequestDto dto, MultipartFile file) throws IOException {
        Optional<Benefit> optionalBenefit = benefitRepository.findById(dto.id());
        if(optionalBenefit.isEmpty()) {
            return ApiResponse.withError(ErrorCode.INVALID_BENEFIT_ID);
        }
        Benefit benefit = optionalBenefit.get();
        if(!file.isEmpty()) {
            s3Adapter.deleteFile(benefit.getImageFileName());
            ApiResponse<String> updateFileResponse = s3Adapter.uploadFile(file);
            if (updateFileResponse.getStatus().is5xxServerError()) {
                return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);
            }
            benefit.setImageUrl(updateFileResponse.getData());
            benefit.setImageFileName(file.getOriginalFilename());
        }
        benefit.setTitle(dto.title());
        benefit.setContent(dto.content());
        Benefit savedBenefit = benefitRepository.save(benefit);
        return ApiResponse.ok("혜택 정보를 성공적으로 수정했습니다.", savedBenefit);
    }

    public ApiResponse<Benefit> updateBenefitText(UpdateBenefitServiceRequestDto dto) {
        Optional<Benefit> optionalBenefit = benefitRepository.findById(dto.id());
        if(optionalBenefit.isEmpty()) {
            return ApiResponse.withError(ErrorCode.INVALID_BENEFIT_ID);
        }
        Benefit benefit = optionalBenefit.get();
        benefit.setTitle(dto.title());
        benefit.setContent(dto.content());
        Benefit savedBenefit = benefitRepository.save(benefit);
        return ApiResponse.ok("혜택 텍스트 정보를 성공적으로 수정했습니다.", savedBenefit);
    }

    public ApiResponse<String> deleteBenefit(Long benefitId) {
        Optional<Benefit> optionalBenefit = benefitRepository.findById(benefitId);
        if(optionalBenefit.isEmpty()) {
            return ApiResponse.withError(ErrorCode.INVALID_BENEFIT_ID);
        }
        Benefit benefit = optionalBenefit.get();
        String imageFileName = benefit.getImageFileName();
        s3Adapter.deleteFile(imageFileName);
        benefitRepository.delete(benefit);
        return ApiResponse.ok("혜택 정보를 성공적으로 삭제했습니다.");
    }
}
