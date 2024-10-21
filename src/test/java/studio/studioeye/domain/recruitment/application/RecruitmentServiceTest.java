package studio.studioeye.domain.recruitment.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import studio.studioeye.domain.news.domain.News;
import studio.studioeye.domain.news.dto.CreateNewsServiceRequestDto;
import studio.studioeye.domain.recruitment.dao.RecruitmentRepository;
import studio.studioeye.domain.recruitment.domain.Recruitment;
import studio.studioeye.domain.recruitment.dto.request.CreateRecruitmentServiceRequestDto;
import studio.studioeye.global.common.response.ApiResponse;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class RecruitmentServiceTest {
    @InjectMocks
    private RecruitmentService recruitmentService;

    @Mock
    private RecruitmentRepository recruitmentRepository;

    @Test
    @DisplayName("채용공고 생성 성공")
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
    }
}
