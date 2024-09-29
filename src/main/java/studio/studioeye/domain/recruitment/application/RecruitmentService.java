package studio.studioeye.domain.recruitment.application;

import studio.studioeye.domain.recruitment.dao.RecruitmentRepository;
import studio.studioeye.domain.recruitment.dao.RecruitmentTitle;
import studio.studioeye.domain.recruitment.domain.Recruitment;
import studio.studioeye.domain.recruitment.dto.request.CreateRecruitmentServiceRequestDto;
import studio.studioeye.domain.recruitment.dto.request.UpdateRecruitmentServiceRequestDto;
import studio.studioeye.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studio.studioeye.global.exception.error.ErrorCode;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

@Service
@Transactional
@RequiredArgsConstructor
public class RecruitmentService {

    private final RecruitmentRepository recruitmentRepository;
    public ApiResponse<Recruitment> createRecruitment(CreateRecruitmentServiceRequestDto dto) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        Recruitment recruitment = dto.toEntity(new Date(), calculateStatus(dto.startDate(), dto.deadline()));

        Recruitment savedRecruitment = recruitmentRepository.save(recruitment);

        return ApiResponse.ok("채용공고 게시물을 성공적으로 등록하였습니다.", savedRecruitment);
    }

    public ApiResponse<Page<RecruitmentTitle>> retrieveRecruitmentList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RecruitmentTitle> recruitmentTitleList = recruitmentRepository.findAllRecruitments(pageable);
        return ApiResponse.ok("채용공고 목록을 성공적으로 조회했습니다.", recruitmentTitleList);
    }

    public ApiResponse<Recruitment> retrieveRecruitmentById(Long id) {
        Optional<Recruitment> optionalRecruitment = recruitmentRepository.findById(id);
        if(optionalRecruitment.isEmpty()) {
            return ApiResponse.withError(ErrorCode.INVALID_RECRUITMENT_ID);
        }
        Recruitment recruitment = optionalRecruitment.get();
        return ApiResponse.ok("채용공고를 성공적으로 조회했습니다.", recruitment);
    }

    public ApiResponse<Recruitment> retrieveRecentRecruitment() {
        Optional<Recruitment> optionalRecruitment = recruitmentRepository.findTopByOrderByCreatedAtDesc();
        if(optionalRecruitment.isEmpty()) {
            return ApiResponse.ok(ErrorCode.RECRUITMENT_IS_EMPTY.getMessage());
        }
        Recruitment recruitment = optionalRecruitment.get();
        return ApiResponse.ok("가장 최근 채용공고를 성공적으로 조회했습니다.", recruitment);
    }

    public ApiResponse<Recruitment> updateRecruitment(UpdateRecruitmentServiceRequestDto dto) {
        Optional<Recruitment> optionalRecruitment = recruitmentRepository.findById(dto.id());
        if(optionalRecruitment.isEmpty()) {
            return ApiResponse.withError(ErrorCode.INVALID_RECRUITMENT_ID);
        }
        Recruitment recruitment = optionalRecruitment.get();
        recruitment.update(dto, calculateStatus(dto.startDate(), dto.deadline()));
        Recruitment savedRecruitment = recruitmentRepository.save(recruitment);
        return ApiResponse.ok("채용공고 게시물을 성공적으로 수정했습니다.", savedRecruitment);
    }

    public ApiResponse<String> deleteRecruitment(Long id) {
        Optional<Recruitment> optionalRecruitment = recruitmentRepository.findById(id);
        if(optionalRecruitment.isEmpty()) {
            return ApiResponse.withError(ErrorCode.INVALID_RECRUITMENT_ID);
        }
        Recruitment recruitment = optionalRecruitment.get();
        recruitmentRepository.delete(recruitment);
        return ApiResponse.ok("채용공고를 성공적으로 삭제하였습니다.");
    }

    private Boolean calculateStatus(Date startDate, Date deadline) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        Date now = new Date();
        return now.compareTo(startDate) >= 0 && now.compareTo(deadline) <= 0;
    }

    @Scheduled(cron = "0 0 0 * * *") // 자정
    public void autoUpdate() {
        List<Recruitment> recruitmentList = recruitmentRepository.findByStatusTrue();
        for(Recruitment recruitment : recruitmentList) {
            recruitment.setStatus(calculateStatus(recruitment.getStartDate(), recruitment.getDeadline()));
            recruitmentRepository.save(recruitment);
        }
    }
}
