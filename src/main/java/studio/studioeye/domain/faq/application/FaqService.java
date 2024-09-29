package studio.studioeye.domain.faq.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import studio.studioeye.domain.faq.dao.FaqQuestions;
import studio.studioeye.domain.faq.dao.FaqRepository;
import studio.studioeye.domain.faq.domain.Faq;
import studio.studioeye.domain.faq.dto.request.CreateFaqServiceRequestDto;
import studio.studioeye.domain.faq.dto.request.UpdateFaqServiceRequestDto;
import studio.studioeye.infrastructure.s3.S3Adapter;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class FaqService {

    private final FaqRepository faqRepository;
    private final S3Adapter s3Adapter;

    public ApiResponse<Faq> createFaq(CreateFaqServiceRequestDto dto){
        if(dto.question().trim().isEmpty() || dto.answer().trim().isEmpty() || dto.visibility() == null) {
            return ApiResponse.withError(ErrorCode.FAQ_IS_EMPTY);
        }
        Faq faq = dto.toEntity();
        Faq savedFaq = faqRepository.save(faq);
        return ApiResponse.ok("FAQ를 성공적으로 등록하였습니다.", savedFaq);
    }

    public ApiResponse<List<Faq>> retrieveAllFaq() {
        List<Faq> faqList = faqRepository.findAll();
        if(faqList.isEmpty()) {
            return ApiResponse.ok("FAQ가 존재하지 않습니다.");
        }
        return ApiResponse.ok("FAQ 목록을 성공적으로 조회했습니다.", faqList);
    }


    public ApiResponse<List<FaqQuestions>> retrieveAllFaqTitle() {
        List<FaqQuestions> faqQuestions = faqRepository.findAllQuestions();
        if(faqQuestions.isEmpty()) {
            return ApiResponse.ok("FAQ가 존재하지 않습니다.");
        }
        return ApiResponse.ok("FAQ 목록을 성공적으로 조회했습니다.", faqQuestions);
    }

    public ApiResponse<Faq> retrieveFaqById(Long id) {
        Optional<Faq> optionalFaq = faqRepository.findById(id);
        if(optionalFaq.isEmpty()) {
            return ApiResponse.ok("FAQ가 존재하지 않습니다.");
        }
        Faq faq = optionalFaq.get();
        return ApiResponse.ok("FAQ를 성공적으로 조회했습니다.", faq);
    }

    public Page<Faq> retrieveFaqPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return faqRepository.findAll(pageable);
    }

    public ApiResponse<String> convertBase64ToImageUrl(String base64Code) throws IOException {
        base64Code = base64Code.replaceAll("\"", ""); // requestBody에서 문자열 앞뒤에 "" 추가되는 현상 처리
        MultipartFile file = this.convert(base64Code);
        ApiResponse<String> updateFileResponse = s3Adapter.uploadImage(file);
        String imageUrl = null;
        if (updateFileResponse.getStatus().is5xxServerError()) {
            return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);
        }
        imageUrl = updateFileResponse.getData();
        if(imageUrl.isEmpty()) return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);
        return ApiResponse.ok("FAQ base64 이미지를 성공적으로 저장했습니다.",imageUrl);
    }

    public ApiResponse<Faq> updateFaq(UpdateFaqServiceRequestDto dto) {
        String question = dto.question().trim();
        String answer = dto.answer().trim();
        Boolean visibility = dto.visibility();
        Optional<Faq> optionalFaq = faqRepository.findById(dto.id());
        if(optionalFaq.isEmpty()) {
            return ApiResponse.withError(ErrorCode.INVALID_FAQ_ID);
        }

        Faq faq = optionalFaq.get();
        if(!question.isEmpty()) {
            faq.updateTitle(question);
        }
        if(!answer.isEmpty()) {
            faq.updateContent(answer);
        }
        if(visibility != null) {
            faq.updateVisibility(visibility);
        }
        Faq updatedFaq = faqRepository.save(faq);
        return ApiResponse.ok("FAQ를 성공적으로 수정하였습니다.", updatedFaq);
    }

    public ApiResponse<String> deleteFaq(Long id) {
        Optional<Faq> optionalFaq = faqRepository.findById(id);
        if(optionalFaq.isEmpty()) {
            return ApiResponse.withError(ErrorCode.INVALID_FAQ_ID);
        }
        Faq faq = optionalFaq.get();
        faqRepository.delete(faq);
        return ApiResponse.ok("FAQ를 성공적으로 삭제했습니다.");
    }
    public ApiResponse<String> deleteFaqs(List<Long> ids) {
        for(Long id : ids) {
            Optional<Faq> optionalFaq = faqRepository.findById(id);
            if (optionalFaq.isEmpty()) {
                return ApiResponse.withError(ErrorCode.INVALID_FAQ_ID);
            }
            Faq faq = optionalFaq.get();
            faqRepository.delete(faq);
        }
        return ApiResponse.ok("FAQ를 성공적으로 삭제했습니다.");
    }

    public MultipartFile convert(String base64Image) throws IOException {
        // "data:image/png;base64,"와 같은 접두사 제거
        String[] parts = base64Image.split(",");
        String imageString = parts[1];
        byte[] decodedBytes = Base64.getDecoder().decode(imageString);

        return new MockMultipartFile(
                "image", // 파일 이름
                "image.png", // 원본 파일 이름
                "image/png", // MIME 타입
                new ByteArrayInputStream(decodedBytes)
        );
    }
}
