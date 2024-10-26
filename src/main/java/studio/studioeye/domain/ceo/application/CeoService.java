package studio.studioeye.domain.ceo.application;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import studio.studioeye.domain.ceo.dao.CeoRepository;
import studio.studioeye.domain.ceo.domain.Ceo;
import studio.studioeye.domain.ceo.dto.request.CreateCeoServiceRequestDto;
import studio.studioeye.domain.ceo.dto.request.UpdateCeoServiceRequestDto;
import studio.studioeye.infrastructure.s3.S3Adapter;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;

import java.io.IOException;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CeoService {
    private final CeoRepository ceoRepository;
    private final S3Adapter s3Adapter;

    public ApiResponse<Ceo> createCeoInformation(CreateCeoServiceRequestDto dto, MultipartFile file) throws IOException {
        List<Ceo> ceoList = ceoRepository.findAll();
        if(!ceoList.isEmpty()) {
            return updateCeoInformation(dto.toUpdateServiceRequest(), file);
        }
        String imageUrl = null;
        if (file != null) {
            ApiResponse<String> updateFileResponse = s3Adapter.uploadFile(file);
            if (updateFileResponse.getStatus().is5xxServerError()) {
                return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);
            }
            imageUrl = updateFileResponse.getData();
            if(imageUrl.isEmpty()) return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);
        }
        Ceo ceo = dto.toEntity(file != null ? file.getOriginalFilename() : null, imageUrl);
        Ceo savedCeo = ceoRepository.save(ceo);
        return ApiResponse.ok("CEO 정보를 성공적으로 등록하였습니다.", savedCeo);
    }

    public ApiResponse<Ceo> retrieveCeoInformation() {
        List<Ceo> ceoList = ceoRepository.findAll();
        if(ceoList.isEmpty()) {
            return ApiResponse.ok("CEO 정보가 존재하지 않습니다.");
        }
        Ceo ceo = ceoList.get(0);
        return ApiResponse.ok("CEO 정보를 성공적으로 조회했습니다.", ceo);
    }

    public ApiResponse<Ceo> updateCeoInformation(UpdateCeoServiceRequestDto dto, MultipartFile file) throws IOException {
        String imageUrl = null;
        String fileName = null;
        List<Ceo> ceoList = ceoRepository.findAll();
        if(!ceoList.isEmpty()) {
            String ceoImageFileName = ceoList.get(0).getImageFileName();
            if(ceoImageFileName != null) s3Adapter.deleteFile(ceoImageFileName);
        }
        if(file != null) {
            ApiResponse<String> updateFileResponse = s3Adapter.uploadFile(file);
            if (updateFileResponse.getStatus().is5xxServerError()) {
                return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);
            }
            imageUrl = updateFileResponse.getData();
            if(imageUrl.isEmpty()) return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);
            fileName = file.getOriginalFilename();
        }
        Ceo ceo = ceoList.get(0);
        ceo.updateCeoInformation(dto, fileName, imageUrl);
        Ceo savedCeo = ceoRepository.save(ceo);
        return ApiResponse.ok("CEO 정보를 성공적으로 수정했습니다.", savedCeo);
    }

    public ApiResponse<Ceo> updateCeoTextInformation(UpdateCeoServiceRequestDto dto) {
        List<Ceo> ceoList = ceoRepository.findAll();
        if(!ceoList.isEmpty()) {
            String ceoImageFileName = ceoList.get(0).getImageFileName();
            if(ceoImageFileName != null) s3Adapter.deleteFile(ceoImageFileName);
        }
        Ceo ceo = ceoList.get(0);
        ceo.updateCeoTextInformation(dto);
        Ceo savedCeo = ceoRepository.save(ceo);
        return ApiResponse.ok("CEO 텍스트 정보를 성공적으로 수정했습니다.", savedCeo);
    }

    @SneakyThrows
    public ApiResponse<Ceo> updateCeoImageInformation(MultipartFile file) {
        String imageUrl = null;
        String fileName = null;
        List<Ceo> ceoList = ceoRepository.findAll();
        if(!ceoList.isEmpty()) {
            String ceoImageFileName = ceoList.get(0).getImageFileName();
            if(ceoImageFileName != null) s3Adapter.deleteFile(ceoImageFileName);
        }
        if(file != null) {
            ApiResponse<String> updateFileResponse = s3Adapter.uploadFile(file);
            if (updateFileResponse.getStatus().is5xxServerError()) {
                return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);
            }
            imageUrl = updateFileResponse.getData();
            if(imageUrl.isEmpty()) return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);
            fileName = file.getOriginalFilename();
        }
        Ceo ceo = ceoList.get(0);
        ceo.updateCeoImageInformation(fileName, imageUrl);
        Ceo savedCeo = ceoRepository.save(ceo);
        return ApiResponse.ok("CEO 이미지 정보를 성공적으로 수정했습니다.", savedCeo);
    }


    public ApiResponse<String> deleteCeoInformation() {
        List<Ceo> ceoList = ceoRepository.findAll();
        if(ceoList.isEmpty()) {
            ApiResponse.withError(ErrorCode.CEO_IS_EMPTY);
        }
        Ceo ceo = ceoList.get(0);
        ceoRepository.delete(ceo);
        return ApiResponse.ok("CEO 정보를 성공적으로 삭제했습니다.");
    }
}
