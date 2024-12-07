package studio.studioeye.domain.company_information.application;

import studio.studioeye.domain.company_information.dao.CompanyBasicInformation;
import studio.studioeye.domain.company_information.dao.CompanyInformationRepository;
import studio.studioeye.domain.company_information.dao.CompanyIntroductionInformation;
import studio.studioeye.domain.company_information.dao.CompanyIntroductionInformationImpl;
import studio.studioeye.domain.company_information.domain.CompanyInformation;
import studio.studioeye.domain.company_information.domain.CompanyInformationDetailInformation;
import studio.studioeye.domain.company_information.dto.request.*;
import studio.studioeye.infrastructure.s3.S3Adapter;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CompanyInformationService {

    private final CompanyInformationRepository companyInformationRepository;
    private final S3Adapter s3Adapter;


    public ApiResponse<CompanyInformation> createCompanyInformation(CreateCompanyInformationServiceRequestDto dto,
                                                MultipartFile lightLogoImage, MultipartFile darkLogoImage,
                                                MultipartFile sloganImage) throws IOException {
        List<CompanyInformation> companyInformations = companyInformationRepository.findAll();
        if(!companyInformations.isEmpty()) {
            return updateAllCompanyInformation(dto.toUpdateServiceRequest(), lightLogoImage, darkLogoImage, sloganImage);
        }

        if(lightLogoImage == null || lightLogoImage.isEmpty()
                || darkLogoImage == null || darkLogoImage.isEmpty()
                || sloganImage == null || sloganImage.isEmpty()) {
            return ApiResponse.withError(ErrorCode.NOT_EXIST_IMAGE_FILE);
        }

        String lightLogoImageFileName = null;
        String lightLogoImageUrl = null;
        String darkLogoImageFileName = null;
        String darkLogoImageUrl = null;
        String sloganImageFileName = null;
        String sloganImageUrl = null;
        if(!lightLogoImage.isEmpty()) {
            ApiResponse<String> updateLogoFileResponse = s3Adapter.uploadFile(lightLogoImage);
            if (updateLogoFileResponse.getStatus().is5xxServerError()) {
                return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);
            }
            lightLogoImageUrl = updateLogoFileResponse.getData();
            lightLogoImageFileName = lightLogoImage.getOriginalFilename();
        }
        if(!darkLogoImage.isEmpty()) {
            ApiResponse<String> updateLogoFileResponse = s3Adapter.uploadFile(darkLogoImage);
            if (updateLogoFileResponse.getStatus().is5xxServerError()) {
                return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);
            }
            darkLogoImageUrl = updateLogoFileResponse.getData();
            darkLogoImageFileName = lightLogoImage.getOriginalFilename();
        }
        if(!sloganImage.isEmpty()) {
            ApiResponse<String> updateSloganFileResponse = s3Adapter.uploadFile(sloganImage);

            if (updateSloganFileResponse.getStatus().is5xxServerError()) {
                return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);
            }
            sloganImageUrl = updateSloganFileResponse.getData();
            sloganImageFileName = sloganImage.getOriginalFilename();
        }
        CompanyInformation companyInformation = dto.toEntity(lightLogoImageFileName, lightLogoImageUrl, darkLogoImageFileName, darkLogoImageUrl, sloganImageFileName, sloganImageUrl);
        CompanyInformation savedCompanyInformation = companyInformationRepository.save(companyInformation);
        return ApiResponse.ok("회사 정보를 성공적으로 등록하였습니다.", savedCompanyInformation);
    }

    public ApiResponse<CompanyInformation> retrieveAllCompanyInformation() {
        List<CompanyInformation> companyInformations = companyInformationRepository.findAll();
        if(companyInformations.isEmpty()) {
            return ApiResponse.ok("회사 정보가 존재하지 않습니다.");
        }
        CompanyInformation companyInformation = companyInformations.get(0);
        return ApiResponse.ok("전체 회사 정보를 성공적으로 조회하였습니다.", companyInformation);
    }

    public ApiResponse<String> retrieveCompanyLogoImage(boolean isLight) {
        List<String> logoImageUrls;
        if(isLight) {
            logoImageUrls = companyInformationRepository.findLightLogoImageUrl();
        }
        else {
            logoImageUrls = companyInformationRepository.findDarkLogoImageUrl();
        }
        if(logoImageUrls.isEmpty()) {
            return ApiResponse.ok("회사 로고 이미지가 존재하지 않습니다.");
        }
        String logoImageUrl = logoImageUrls.get(0);
        String cacheBustedLogoImageUrl = logoImageUrl + "?v=" + System.currentTimeMillis();
        return ApiResponse.ok("회사 로고 이미지를 성공적으로 조회하였습니다.", cacheBustedLogoImageUrl);
    }

    public ApiResponse<CompanyBasicInformation> retrieveCompanyBasicInformation() {
        List<CompanyBasicInformation> companyBasicInformations = companyInformationRepository.findAddressAndPhoneAndFax();
        if(companyBasicInformations.isEmpty()) {
            return ApiResponse.ok("회사 기본 정보가 존재하지 않습니다.");
        }
        CompanyBasicInformation companyBasicInformation = companyBasicInformations.get(0);
        return ApiResponse.ok("회사 기본 정보를 성공적으로 조회하였습니다.", companyBasicInformation);
    }

    public ApiResponse<CompanyIntroductionInformation> retrieveCompanyIntroductionInformation() {
        List<CompanyIntroductionInformation> companyIntroductionInformations = companyInformationRepository.findIntroductionAndSloganImageUrl();
        if(companyIntroductionInformations.isEmpty()) {
            return ApiResponse.ok("회사 소개 정보가 존재하지 않습니다.");
        }
        CompanyIntroductionInformation companyIntroductionInformation = companyIntroductionInformations.get(0);

        String updatedSloganImageUrl = companyIntroductionInformation.getSloganImageUrl() + "?v=" + System.currentTimeMillis();
        CompanyIntroductionInformation updatedCompanyIntroInformation = new CompanyIntroductionInformationImpl(companyIntroductionInformation.getIntroduction(), updatedSloganImageUrl);

        return ApiResponse.ok("회사 소개 정보를 성공적으로 조회하였습니다.", updatedCompanyIntroInformation);
    }

    public ApiResponse<List<CompanyInformationDetailInformation>> retrieveCompanyDetailInformation() {
        List<CompanyInformation> companyInformations = companyInformationRepository.findAll();
        if(companyInformations.isEmpty()) {
            return ApiResponse.ok("회사 정보가 존재하지 않습니다.");
        }
        CompanyInformation companyInformation = companyInformations.get(0);
        List<CompanyInformationDetailInformation> companyDetailInformation = companyInformation.getDetailInformation();
        if(companyDetailInformation.isEmpty()) {
            return ApiResponse.ok("회사 상세 정보가 존재하지 않습니다.");
        }
        return ApiResponse.ok("회사 상세 정보를 성공적으로 조회하였습니다.", companyDetailInformation);
    }

    public ApiResponse<CompanyInformation> updateAllCompanyInformation(UpdateAllCompanyInformationServiceRequestDto dto,
                                                            MultipartFile lightLogoImage, MultipartFile darkLogoImage,
                                                            MultipartFile sloganImage) throws IOException {

        List<CompanyInformation> companyInformations = companyInformationRepository.findAll();
        if (companyInformations.isEmpty()) {
            return ApiResponse.withError(ErrorCode.COMPANYINFORMATION_IS_EMPTY);
        }

        String lightLogoImageFileName = companyInformations.get(0).getLightLogoImageFileName();
        String lightLogoImageUrl = companyInformations.get(0).getLightLogoImageUrl();
        String darkLogoImageFileName = companyInformations.get(0).getDarkLogoImageFileName();
        String darkLogoImageUrl = companyInformations.get(0).getDarkLogoImageUrl();
        String sloganImageFileName = companyInformations.get(0).getSloganImageFileName();
        String sloganImageUrl = companyInformations.get(0).getSloganImageUrl();

        if(lightLogoImage != null && !lightLogoImage.isEmpty()) {
            if (lightLogoImageFileName != null) s3Adapter.deleteFile(lightLogoImageFileName);

            ApiResponse<String> updateLogoFileResponse = s3Adapter.uploadFile(lightLogoImage);
            if (updateLogoFileResponse.getStatus().is5xxServerError()) {
                return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);
            }
            lightLogoImageUrl = updateLogoFileResponse.getData();
            lightLogoImageFileName = lightLogoImage.getOriginalFilename();
        }
        if(darkLogoImage != null && !darkLogoImage.isEmpty()) {
            if (darkLogoImageFileName != null) s3Adapter.deleteFile(darkLogoImageFileName);

            ApiResponse<String> updateLogoFileResponse = s3Adapter.uploadFile(darkLogoImage);
            if (updateLogoFileResponse.getStatus().is5xxServerError()) {
                return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);
            }
            darkLogoImageUrl = updateLogoFileResponse.getData();
            darkLogoImageFileName = darkLogoImage.getOriginalFilename();
        }
        if(sloganImage != null && !sloganImage.isEmpty()) {
            if (sloganImageFileName != null) s3Adapter.deleteFile(sloganImageFileName);

            ApiResponse<String> updateSloganFileResponse = s3Adapter.uploadFile(sloganImage);
            if (updateSloganFileResponse.getStatus().is5xxServerError()) {
                return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);
            }
            sloganImageUrl = updateSloganFileResponse.getData();
            sloganImageFileName = sloganImage.getOriginalFilename();
        }
        CompanyInformation companyInformation = companyInformations.get(0);
        companyInformation.updateAllCompanyInformation(dto, lightLogoImageFileName, lightLogoImageUrl, darkLogoImageFileName, darkLogoImageUrl, sloganImageFileName, sloganImageUrl);
        CompanyInformation savedCompanyInformation = companyInformationRepository.save(companyInformation);
        return ApiResponse.ok("전체 회사 정보를 성공적으로 수정했습니다.", savedCompanyInformation);
    }

    public ApiResponse<CompanyInformation> updateAllCompanyTextInformation(UpdateAllCompanyInformationServiceRequestDto dto) {
        List<CompanyInformation> companyInformations = companyInformationRepository.findAll();
        if (companyInformations.isEmpty()) {
            return ApiResponse.withError(ErrorCode.COMPANYINFORMATION_IS_EMPTY);
        }
        CompanyInformation companyInformation = companyInformations.get(0);
        companyInformation.updateAllCompanyTextInformation(dto);
        CompanyInformation savedCompanyInformation = companyInformationRepository.save(companyInformation);
        return ApiResponse.ok("전체 회사 정보를 성공적으로 수정했습니다.", savedCompanyInformation);
    }

    public ApiResponse<CompanyInformation> updateCompanyBasicInformation(UpdateCompanyBasicInformationServiceRequestDto dto) {
        List<CompanyInformation> companyInformations = companyInformationRepository.findAll();
        if (companyInformations.isEmpty()) {
            return ApiResponse.withError(ErrorCode.COMPANYINFORMATION_IS_EMPTY);
        }
        CompanyInformation companyInformation = companyInformations.get(0);
        companyInformation.updateCompanyBasicInformation(dto);
        CompanyInformation savedCompanyInformation = companyInformationRepository.save(companyInformation);
        return ApiResponse.ok("회사 기본 정보를 성공적으로 수정했습니다.", savedCompanyInformation);
    }

    public ApiResponse<CompanyInformation> updateCompanyLogoImage(MultipartFile lightLogoImage, MultipartFile darkLogoImage) throws IOException  {
        if(lightLogoImage == null || darkLogoImage == null) {
            return ApiResponse.withError(ErrorCode.NOT_EXIST_IMAGE_FILE);
        }
        List<CompanyInformation> companyInformations = companyInformationRepository.findAll();
        if (companyInformations.isEmpty()) {
            return ApiResponse.withError(ErrorCode.COMPANYINFORMATION_IS_EMPTY);
        }
        String fileName = companyInformations.get(0).getLightLogoImageFileName();
        s3Adapter.deleteFile(fileName);
        fileName = companyInformations.get(0).getDarkLogoImageFileName();
        s3Adapter.deleteFile(fileName);

        ApiResponse<String> updateLightLogoFileResponse = s3Adapter.uploadFile(lightLogoImage);
        if (updateLightLogoFileResponse.getStatus().is5xxServerError()) {
            return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);
        }
        ApiResponse<String> updateDarkLogoFileResponse = s3Adapter.uploadFile(darkLogoImage);
        if (updateDarkLogoFileResponse.getStatus().is5xxServerError()) {
            return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);
        }
        CompanyInformation companyInformation = companyInformations.get(0);
        companyInformation.updateCompanyLogo(lightLogoImage.getOriginalFilename(), updateLightLogoFileResponse.getData(), darkLogoImage.getOriginalFilename(), updateDarkLogoFileResponse.getData());
        CompanyInformation savedCompanyInformation = companyInformationRepository.save(companyInformation);
        return ApiResponse.ok("회사 로고 이미지를 성공적으로 수정했습니다.", savedCompanyInformation);
    }

    public ApiResponse<CompanyInformation> updateCompanySloganImage(MultipartFile sloganImageUrl) throws IOException {
        if(sloganImageUrl == null) {
            return ApiResponse.withError(ErrorCode.NOT_EXIST_IMAGE_FILE);
        }
        List<CompanyInformation> companyInformations = companyInformationRepository.findAll();
        if (companyInformations.isEmpty()) {
            return ApiResponse.withError(ErrorCode.COMPANYINFORMATION_IS_EMPTY);
        }
        String fileName = companyInformations.get(0).getSloganImageFileName();
        s3Adapter.deleteFile(fileName);

        ApiResponse<String> updateSloganFileResponse = s3Adapter.uploadFile(sloganImageUrl);
        if (updateSloganFileResponse.getStatus().is5xxServerError()) {
            return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);
        }
        CompanyInformation companyInformation = companyInformations.get(0);
        companyInformation.updateCompanySlogan(sloganImageUrl.getOriginalFilename(), updateSloganFileResponse.getData());
        CompanyInformation savedCompanyInformation = companyInformationRepository.save(companyInformation);
        return ApiResponse.ok("회사 슬로건 이미지를 성공적으로 수정했습니다.", savedCompanyInformation);
    }

    public ApiResponse<CompanyInformation> updateCompanyLogoAndSlogan(MultipartFile lightLogoImage, MultipartFile darkLogoImage,MultipartFile sloganImage) throws IOException {
        List<CompanyInformation> companyInformations = companyInformationRepository.findAll();
        if (companyInformations.isEmpty()) {
            return ApiResponse.withError(ErrorCode.COMPANYINFORMATION_IS_EMPTY);
        }

        String lightLogoImageFileName = companyInformations.get(0).getLightLogoImageFileName();
        String lightLogoImageUrl = companyInformations.get(0).getLightLogoImageUrl();
        String darkLogoImageFileName = companyInformations.get(0).getDarkLogoImageFileName();
        String darkLogoImageUrl = companyInformations.get(0).getDarkLogoImageUrl();
        String sloganImageFileName = companyInformations.get(0).getSloganImageFileName();
        String sloganImageUrl = companyInformations.get(0).getSloganImageUrl();

        if(lightLogoImage != null && !lightLogoImage.isEmpty()) {
            if (lightLogoImageFileName != null) s3Adapter.deleteFile(lightLogoImageFileName);

            ApiResponse<String> updateLogoFileResponse = s3Adapter.uploadFile(lightLogoImage);
            if (updateLogoFileResponse.getStatus().is5xxServerError()) {
                return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);
            }
            lightLogoImageUrl = updateLogoFileResponse.getData();
            lightLogoImageFileName = lightLogoImage.getOriginalFilename();
        }
        if(darkLogoImage != null && !darkLogoImage.isEmpty()) {
            if (darkLogoImageFileName != null) s3Adapter.deleteFile(darkLogoImageFileName);

            ApiResponse<String> updateLogoFileResponse = s3Adapter.uploadFile(darkLogoImage);
            if (updateLogoFileResponse.getStatus().is5xxServerError()) {
                return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);
            }
            darkLogoImageUrl = updateLogoFileResponse.getData();
            darkLogoImageFileName = darkLogoImage.getOriginalFilename();
        }
        if(sloganImage != null && !sloganImage.isEmpty()) {
            if (sloganImageFileName != null) s3Adapter.deleteFile(sloganImageFileName);

            ApiResponse<String> updateSloganFileResponse = s3Adapter.uploadFile(sloganImage);
            if (updateSloganFileResponse.getStatus().is5xxServerError()) {
                return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);
            }
            sloganImageUrl = updateSloganFileResponse.getData();
            sloganImageFileName = sloganImage.getOriginalFilename();
        }
        CompanyInformation companyInformation = companyInformations.get(0);
        companyInformation.updateCompanyLogoAndSlogan(lightLogoImageFileName, lightLogoImageUrl, darkLogoImageFileName, darkLogoImageUrl, sloganImageFileName, sloganImageUrl);
        CompanyInformation savedCompanyInformation = companyInformationRepository.save(companyInformation);
        return ApiResponse.ok("회사 로고 이미지와 슬로건 이미지를 성공적으로 수정했습니다.", savedCompanyInformation);
    }


    public ApiResponse<CompanyInformation> updateCompanyIntroductionInformation(UpdateCompanyIntroductionInformationServiceRequestDto dto) {
        List<CompanyInformation> companyInformations = companyInformationRepository.findAll();
        if (companyInformations.isEmpty()) {
            return ApiResponse.withError(ErrorCode.COMPANYINFORMATION_IS_EMPTY);
        }
        CompanyInformation companyInformation = companyInformations.get(0);
        companyInformation.updateCompanyIntroductionInformation(dto);
        CompanyInformation savedCompanyInformation = companyInformationRepository.save(companyInformation);
        return ApiResponse.ok("회사 소개 정보를 성공적으로 수정했습니다.", savedCompanyInformation);
    }

    public ApiResponse<CompanyInformation> updateCompanyDetailInformation(UpdateCompanyDetailInformationServiceRequestDto dto) {
        List<CompanyInformation> companyInformations = companyInformationRepository.findAll();
        if (companyInformations.isEmpty()) {
            return ApiResponse.withError(ErrorCode.COMPANYINFORMATION_IS_EMPTY);
        }
        CompanyInformation companyInformation = companyInformations.get(0);
        companyInformation.updateCompanyDetailInformation(dto);
        CompanyInformation savedCompanyInformation = companyInformationRepository.save(companyInformation);
        return ApiResponse.ok("회사 5가지 상세 정보를 성공적으로 수정했습니다.", savedCompanyInformation);
    }

    public ApiResponse<String> deleteAllCompanyInformation() {
        List<CompanyInformation> companyInformations = companyInformationRepository.findAll();
        if (companyInformations.isEmpty()) {
            ApiResponse.withError(ErrorCode.COMPANYINFORMATION_IS_EMPTY);
        }
        for (CompanyInformation companyInformation : companyInformations) {
            String lightLogoImageFileName = companyInformation.getLightLogoImageFileName();
            s3Adapter.deleteFile(lightLogoImageFileName);
            String darkLogoImageFileName = companyInformation.getDarkLogoImageFileName();
            s3Adapter.deleteFile(darkLogoImageFileName);
            String sloganFileName = companyInformation.getSloganImageFileName();
            s3Adapter.deleteFile(sloganFileName);
            companyInformationRepository.delete(companyInformation);
        }
        return ApiResponse.ok("전체 회사 정보를 성공적으로 삭제했습니다.");
    }

    public ApiResponse<String> deleteCompanyLogoImage() {
        List<CompanyInformation> companyInformations = companyInformationRepository.findAll();
        if (companyInformations.isEmpty()) {
            ApiResponse.withError(ErrorCode.COMPANYINFORMATION_IS_EMPTY);
        }
        for (CompanyInformation companyInformation : companyInformations) {
            String lightLogoImageFileName = companyInformation.getLightLogoImageFileName();
            s3Adapter.deleteFile(lightLogoImageFileName);
            String darkLogoImageFileName = companyInformation.getDarkLogoImageFileName();
            s3Adapter.deleteFile(darkLogoImageFileName);
            companyInformation.deleteLogoImage();
            companyInformationRepository.save(companyInformation);
        }
        return ApiResponse.ok("회사 로고 이미지를 성공적으로 삭제했습니다.");
    }

    public ApiResponse<CompanyInformation> deleteCompanyBasicInformation() {
        List<CompanyInformation> companyInformations = companyInformationRepository.findAll();
        if (companyInformations.isEmpty()) {
            ApiResponse.withError(ErrorCode.COMPANYINFORMATION_IS_EMPTY);
        }
        CompanyInformation companyInformation = companyInformations.get(0);
        companyInformation.deleteCompanyBasicInformation();
        CompanyInformation savedCompanyInformation = companyInformationRepository.save(companyInformation);
        return ApiResponse.ok("회사 기본 정보를 성공적으로 삭제했습니다.", savedCompanyInformation);
    }

    public ApiResponse<CompanyInformation> deleteCompanyIntroductionInformation() {
        List<CompanyInformation> companyInformations = companyInformationRepository.findAll();
        if (companyInformations.isEmpty()) {
            return ApiResponse.withError(ErrorCode.COMPANYINFORMATION_IS_EMPTY);
        }
        for (CompanyInformation companyInformation : companyInformations) {
            String fileName = companyInformation.getSloganImageFileName();
            s3Adapter.deleteFile(fileName);
        }
        CompanyInformation companyInformation = companyInformations.get(0);
        companyInformation.deleteCompanyIntroductionInformation();
        CompanyInformation savedCompanyInformation = companyInformationRepository.save(companyInformation);
        return ApiResponse.ok("회사 소개 정보를 성공적으로 삭제했습니다.", savedCompanyInformation);
    }

    public ApiResponse<CompanyInformation> deleteCompanyDetailInformation() {
        List<CompanyInformation> companyInformations = companyInformationRepository.findAll();
        if (companyInformations.isEmpty()) {
            return ApiResponse.withError(ErrorCode.COMPANYINFORMATION_IS_EMPTY);
        }
        CompanyInformation companyInformation = companyInformations.get(0);
        companyInformation.deleteCompanyDetailInformation();
        CompanyInformation savedCompanyInformation = companyInformationRepository.save(companyInformation);
        return ApiResponse.ok("회사 5가지 상세 정보를 성공적으로 삭제했습니다.", savedCompanyInformation);
    }
}
