package studio.studioeye.domain.recruitment.application;

import org.assertj.core.api.Assertions;
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
import studio.studioeye.domain.recruitment.dto.request.UpdateRecruitmentServiceRequestDto;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
        Mockito.verify(recruitmentRepository, Mockito.never()).save(any());
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

        // when
        ApiResponse<Recruitment> response = recruitmentService.createRecruitment(requestDto);

        // then
        assertNotNull(response);
        assertEquals(ErrorCode.INVALID_RECRUITMENT_DATE.getStatus(), response.getStatus()); // 에러 코드 검증
        assertEquals(ErrorCode.INVALID_RECRUITMENT_DATE.getMessage(), response.getMessage()); // 에러 메시지 검증
        Mockito.verify(recruitmentRepository, Mockito.never()).save(any());
    }

    @Test
    @DisplayName("채용공고 페이지네이션 조회 성공 테스트")
    public void retrieveRecruitmentListSuccess() {
        // given
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

    @Test
    @DisplayName("채용공고 페이지네이션 조회 실패 테스트 - page가 음수인 경우")
    public void retrieveRecruitmentListFail_InvalidPage() {
        // given
        int page = -1;
        int size = 2;

        // when
        ApiResponse<Page<RecruitmentTitle>> response = recruitmentService.retrieveRecruitmentList(page, size);

        // then
        assertNotNull(response);
        assertEquals(ErrorCode.INVALID_RECRUITMENT_PAGE.getStatus(), response.getStatus()); // 에러 코드 검증
        assertEquals(ErrorCode.INVALID_RECRUITMENT_PAGE.getMessage(), response.getMessage()); // 에러 메시지 검증
        Mockito.verify(recruitmentRepository, Mockito.never()).findAllRecruitments(any());
    }

    @Test
    @DisplayName("채용공고 페이지네이션 조회 실패 테스트 - size가 0이하인 경우")
    public void retrieveRecruitmentListFail_InvalidSize() {
        // given
        int page = 0;
        int size = 0;

        // when
        ApiResponse<Page<RecruitmentTitle>> response = recruitmentService.retrieveRecruitmentList(page, size);

        // then
        assertNotNull(response);
        assertEquals(ErrorCode.INVALID_RECRUITMENT_SIZE.getStatus(), response.getStatus()); // 에러 코드 검증
        assertEquals(ErrorCode.INVALID_RECRUITMENT_SIZE.getMessage(), response.getMessage()); // 에러 메시지 검증
        Mockito.verify(recruitmentRepository, Mockito.never()).findAllRecruitments(any());
    }


    @Test
    @DisplayName("단일 채용공고 조회 성공 테스트")
    public void retrieveRecruitmentByIdSuccess() {
        // given
        Long id = 1L;
        Recruitment savedRecruitment = new Recruitment("Test Title1", new Date(System.currentTimeMillis() - 100000), new Date(System.currentTimeMillis() + 100000), "Test URL1", new Date(), Status.OPEN);
        // stub
        when(recruitmentRepository.findById(id)).thenReturn(Optional.of(savedRecruitment));

        // when
        ApiResponse<Recruitment> response = recruitmentService.retrieveRecruitmentById(id);
        Recruitment findRecruitment = response.getData();

        // then
        Assertions.assertThat(findRecruitment).isEqualTo(savedRecruitment);
        Assertions.assertThat(findRecruitment.getTitle()).isEqualTo(savedRecruitment.getTitle());
        Assertions.assertThat(findRecruitment.getStartDate()).isEqualTo(savedRecruitment.getStartDate());
        Assertions.assertThat(findRecruitment.getDeadline()).isEqualTo(savedRecruitment.getDeadline());
        Assertions.assertThat(findRecruitment.getLink()).isEqualTo(savedRecruitment.getLink());
        Assertions.assertThat(findRecruitment.getCreatedAt()).isEqualTo(savedRecruitment.getCreatedAt());
        Assertions.assertThat(findRecruitment.getStatus()).isEqualTo(savedRecruitment.getStatus());
        Mockito.verify(recruitmentRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("단일 채용공고 조회 실패 테스트")
    public void retrieveRecruitmentByIdFail() {
        // given
        Long id = 2L;
        Recruitment savedRecruitment = new Recruitment("Test Title1", new Date(System.currentTimeMillis() - 100000), new Date(System.currentTimeMillis() + 100000), "Test URL1", new Date(), Status.OPEN);
        // stub
        when(recruitmentRepository.findById(id)).thenReturn(Optional.empty());

        // when
        ApiResponse<Recruitment> response = recruitmentService.retrieveRecruitmentById(id);
        Recruitment findRecruitment = response.getData();

        // then
        assertNotNull(response);
        Assertions.assertThat(findRecruitment).isNotEqualTo(savedRecruitment);
        assertEquals(ErrorCode.INVALID_RECRUITMENT_ID.getStatus(), response.getStatus()); // 에러 코드 검증
        assertEquals(ErrorCode.INVALID_RECRUITMENT_ID.getMessage(), response.getMessage()); // 에러 메시지 검증
    }

    @Test
    @DisplayName("최근 채용공고 조회 성공 테스트")
    public void retrieveRecentRecruitmentSuccess() {
        // given
        Recruitment savedRecruitment = new Recruitment("Test Title1", new Date(System.currentTimeMillis() - 100000), new Date(System.currentTimeMillis() + 100000), "Test URL1", new Date(), Status.OPEN);

        // stub
        when(recruitmentRepository.findTopByOrderByCreatedAtDesc()).thenReturn(Optional.of(savedRecruitment));

        // when
        ApiResponse<Recruitment> response = recruitmentService.retrieveRecentRecruitment();
        Recruitment findRecruitment = response.getData();

        // then
        Assertions.assertThat(findRecruitment).isEqualTo(savedRecruitment);
        Assertions.assertThat(findRecruitment.getTitle()).isEqualTo(savedRecruitment.getTitle());
        Assertions.assertThat(findRecruitment.getStartDate()).isEqualTo(savedRecruitment.getStartDate());
        Assertions.assertThat(findRecruitment.getDeadline()).isEqualTo(savedRecruitment.getDeadline());
        Assertions.assertThat(findRecruitment.getLink()).isEqualTo(savedRecruitment.getLink());
        Assertions.assertThat(findRecruitment.getCreatedAt()).isEqualTo(savedRecruitment.getCreatedAt());
        Assertions.assertThat(findRecruitment.getStatus()).isEqualTo(savedRecruitment.getStatus());
        Mockito.verify(recruitmentRepository, times(1)).findTopByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("최근 채용공고 조회 실패 테스트")
    public void retrieveRecentRecruitmentFail() {
        // given
        Recruitment savedRecruitment = new Recruitment("Test Title1", new Date(System.currentTimeMillis() - 100000), new Date(System.currentTimeMillis() + 100000), "Test URL1", new Date(), Status.OPEN);

        // stub
        when(recruitmentRepository.findTopByOrderByCreatedAtDesc()).thenReturn(Optional.empty());

        // when
        ApiResponse<Recruitment> response = recruitmentService.retrieveRecentRecruitment();
        Recruitment findRecruitment = response.getData();

        // then
        assertNotNull(response);
        Assertions.assertThat(findRecruitment).isNotEqualTo(savedRecruitment);
        assertEquals(ErrorCode.RECRUITMENT_IS_EMPTY.getMessage(), response.getMessage()); // 에러 메시지 검증
    }

    @Test
    @DisplayName("채용공고 수정 성공 테스트")
    public void updateRecruitmentSuccess() {
        // given
        String startDateString = "2024-10-02 23:39:40.281000";
        String deadLineString = "2024-10-31 13:39:40.281000";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        LocalDateTime localStartDateTime = LocalDateTime.parse(startDateString, formatter);
        LocalDateTime localDeadLineTime = LocalDateTime.parse(deadLineString, formatter);

        Recruitment savedRecruitment = new Recruitment("Test Title1", new Date(System.currentTimeMillis() - 100000), new Date(System.currentTimeMillis() + 100000), "Test URL1", new Date(), Status.OPEN);

        UpdateRecruitmentServiceRequestDto requestDto = new UpdateRecruitmentServiceRequestDto(
                1L,
                "title",
                Date.from(localStartDateTime.atZone(ZoneId.systemDefault()).toInstant()),
                Date.from(localDeadLineTime.atZone(ZoneId.systemDefault()).toInstant()),
                "https://www.naver.com/"
        );

        // stub
        when(recruitmentRepository.findById(requestDto.id())).thenReturn(Optional.of(savedRecruitment));

        // when
        ApiResponse<Recruitment> response = recruitmentService.updateRecruitment(requestDto);

        // then
        Assertions.assertThat(response.getMessage()).isEqualTo("채용공고 게시물을 성공적으로 수정했습니다.");
        Assertions.assertThat(requestDto.title()).isEqualTo(savedRecruitment.getTitle());
        Assertions.assertThat(requestDto.title()).isEqualTo(savedRecruitment.getTitle());
        Assertions.assertThat(requestDto.startDate()).isEqualTo(savedRecruitment.getStartDate());
        Assertions.assertThat(requestDto.deadline()).isEqualTo(savedRecruitment.getDeadline());
        Assertions.assertThat(requestDto.link()).isEqualTo(savedRecruitment.getLink());

        Mockito.verify(recruitmentRepository, times(1)).findById(requestDto.id());
        Mockito.verify(recruitmentRepository, times(1)).save(any(Recruitment.class));
    }

    @Test
    @DisplayName("채용공고 수정 실패 테스트")
    public void updateRecruitmentFail() {
        // given
        Long invalidId = 999L;

        String startDateString = "2024-10-02 23:39:40.281000";
        String deadLineString = "2024-10-31 13:39:40.281000";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        LocalDateTime localStartDateTime = LocalDateTime.parse(startDateString, formatter);
        LocalDateTime localDeadLineTime = LocalDateTime.parse(deadLineString, formatter);

        Recruitment savedRecruitment = new Recruitment("Test Title1", new Date(System.currentTimeMillis() - 100000), new Date(System.currentTimeMillis() + 100000), "Test URL1", new Date(), Status.OPEN);
        UpdateRecruitmentServiceRequestDto requestDto = new UpdateRecruitmentServiceRequestDto(
                invalidId,
                "title",
                Date.from(localStartDateTime.atZone(ZoneId.systemDefault()).toInstant()),
                Date.from(localDeadLineTime.atZone(ZoneId.systemDefault()).toInstant()),
                "https://www.naver.com/"
        );

        // stub
        when(recruitmentRepository.findById(invalidId)).thenReturn(Optional.empty());

        // when
        ApiResponse<Recruitment> response = recruitmentService.updateRecruitment(requestDto);
        Recruitment findRecruitment = response.getData();

        // then
        Assertions.assertThat(response.getStatus()).isEqualTo(ErrorCode.INVALID_NEWS_ID.getStatus());
        Assertions.assertThat(findRecruitment).isNotEqualTo(savedRecruitment);
        Mockito.verify(recruitmentRepository, times(1)).findById(invalidId);  // repository 메소드 호출 검증
        Mockito.verify(recruitmentRepository, Mockito.never()).save(any());
    }

    @Test
    @DisplayName("채용공고 삭제 성공 테스트")
    public void deleteRecruitmentSuccess() {
        // given
        Long id = 1L;
        Recruitment savedRecruitment = new Recruitment("Test Title1", new Date(System.currentTimeMillis() - 100000), new Date(System.currentTimeMillis() + 100000), "Test URL1", new Date(), Status.OPEN);
        // stub
        when(recruitmentRepository.findById(id)).thenReturn(Optional.of(savedRecruitment));

        // when
        ApiResponse<String> response = recruitmentService.deleteRecruitment(id);

        // then
        Assertions.assertThat(response.getMessage()).isEqualTo("채용공고를 성공적으로 삭제하였습니다.");
        Mockito.verify(recruitmentRepository, times(1)).findById(id);
        Mockito.verify(recruitmentRepository, times(1)).delete(savedRecruitment);
    }

    @Test
    @DisplayName("채용공고 삭제 실패 테스트")
    public void deleteRecruitmentFail() {
        // given
        Long id = 1L;
        // stub
        when(recruitmentRepository.findById(id)).thenReturn(Optional.empty());

        // when
        ApiResponse<String> response = recruitmentService.deleteRecruitment(id);

        // then
        Assertions.assertThat(response.getStatus()).isEqualTo(ErrorCode.INVALID_RECRUITMENT_ID.getStatus());
        // method call verify
        Mockito.verify(recruitmentRepository, times(1)).findById(id);
        Mockito.verify(recruitmentRepository, Mockito.never()).delete(any());
    }
}
