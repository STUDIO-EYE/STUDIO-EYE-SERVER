package studio.studioeye.domain.recruitment.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import studio.studioeye.domain.recruitment.dao.RecruitmentRepository;
import studio.studioeye.domain.recruitment.dao.RecruitmentTitle;
import studio.studioeye.domain.recruitment.domain.Recruitment;
import studio.studioeye.domain.recruitment.domain.Status;
import studio.studioeye.domain.recruitment.dto.request.CreateRecruitmentServiceRequestDto;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RecruitmentServiceTest {
    @InjectMocks
    private RecruitmentService recruitmentService;

    @Mock
    private RecruitmentRepository recruitmentRepository;

    @Test
    @DisplayName("채용공고 생성 성공 테스트")
    public void createRecruitmentSuccess() {
        //given
        String startDateString = "2024-10-02 23:39:40.281000";
        String deadLineString = "2024-10-31 13:39:40.281000";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        LocalDateTime localStartDateTime = LocalDateTime.parse(startDateString, formatter);
        LocalDateTime localDeadLineTime = LocalDateTime.parse(deadLineString, formatter);

        CreateRecruitmentServiceRequestDto requestDto = new CreateRecruitmentServiceRequestDto(
                "title",
                Date.from(localStartDateTime.atZone(ZoneId.systemDefault()).toInstant()),
                Date.from(localDeadLineTime.atZone(ZoneId.systemDefault()).toInstant()),
                "https://www.naver.com/"
        );

        //when
        ApiResponse<Recruitment> response = recruitmentService.createRecruitment(requestDto);

        //then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("채용공고 게시물을 성공적으로 등록하였습니다.", response.getMessage());
        Mockito.verify(recruitmentRepository, times(1)).save(any(Recruitment.class));
    }

    @Test
    @DisplayName("채용공고 생성 실패 테스트 - 제목이 비어 있는 경우")
    public void createRecruitmentFail_emptyTitle() {
        // given
        Date startDate = new Date(System.currentTimeMillis() + 100000);
        Date deadline = new Date(System.currentTimeMillis());
        CreateRecruitmentServiceRequestDto requestDto = new CreateRecruitmentServiceRequestDto(
                "",
                startDate,
                deadline,
                "https://www.naver.com"
        );

        // when & then
        ApiResponse<Recruitment> response = recruitmentService.createRecruitment(requestDto);

        assertNotNull(response);
        assertEquals(ErrorCode.RECRUITMENT_TITLE_IS_EMPTY.getStatus(), response.getStatus()); // 에러 코드 검증
        assertEquals(ErrorCode.RECRUITMENT_TITLE_IS_EMPTY.getMessage(), response.getMessage()); // 에러 메시지 검증
    }

    @Test
    @DisplayName("채용공고 생성 실패 테스트 - 시작일이 마감일보다 이후인 경우")
    public void createRecruitmentFail_invalidDate() {
        // given
        Date startDate = new Date(System.currentTimeMillis() + 100000);
        Date deadline = new Date(System.currentTimeMillis());
        CreateRecruitmentServiceRequestDto requestDto = new CreateRecruitmentServiceRequestDto(
                "title",
                startDate,
                deadline,
                "https://www.naver.com"
        );

        // when & then
        ApiResponse<Recruitment> response = recruitmentService.createRecruitment(requestDto);

        assertNotNull(response);
        assertEquals(ErrorCode.INVALID_RECRUITMENT_DATE.getStatus(), response.getStatus()); // 에러 코드 검증
        assertEquals(ErrorCode.INVALID_RECRUITMENT_DATE.getMessage(), response.getMessage()); // 에러 메시지 검증
    }

    @Test
    @DisplayName("채용공고 페이지네이션 조회 성공 테스트")
    public void retrieveRecruitmentListSuccess() {
        int page = 0;
        int size = 2;

        Pageable pageable = PageRequest.of(page, size);


        List<RecruitmentTitle> recruitmentList = new ArrayList<>();
        recruitmentList.add(new RecruitmentTitle() {
            @Override
            public Long getId() {
                return 1L;
            }

            @Override
            public String getTitle() {
                return "Test Title1";
            }

            @Override
            public Status getStatus() {
                return Status.OPEN;
            }
        });
        recruitmentList.add(new RecruitmentTitle() {
            @Override
            public Long getId() {
                return 2L;
            }

            @Override
            public String getTitle() {
                return "Test Title2";
            }

            @Override
            public Status getStatus() {
                return Status.OPEN;
            }
        });
        recruitmentList.add(new RecruitmentTitle() {
            @Override
            public Long getId() {
                return 3L;
            }

            @Override
            public String getTitle() {
                return "Test Title3";
            }

            @Override
            public Status getStatus() {
                return Status.OPEN;
            }
        });
        recruitmentList.add(new RecruitmentTitle() {
            @Override
            public Long getId() {
                return 4L;
            }

            @Override
            public String getTitle() {
                return "Test Title4";
            }

            @Override
            public Status getStatus() {
                return Status.OPEN;
            }
        });


        Page<RecruitmentTitle> recruitmentTitlePage = new PageImpl<>(recruitmentList, pageable, recruitmentList.size());

        // stub
        when(recruitmentRepository.findAllRecruitments(pageable)).thenReturn(recruitmentTitlePage);

        // when
        ApiResponse<Page<RecruitmentTitle>> response = recruitmentService.retrieveRecruitmentList(page, size);

        // then

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("채용공고 목록을 성공적으로 조회했습니다.", response.getMessage());
        assertEquals(recruitmentTitlePage, response.getData());
        Mockito.verify(recruitmentRepository, times(1)).findAllRecruitments(pageable);
    }
}
