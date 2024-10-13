package studio.studioeye.unit_test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import studio.studioeye.domain.news.application.NewsService;
import studio.studioeye.domain.news.dao.NewsRepository;
import studio.studioeye.domain.news.domain.News;
import studio.studioeye.domain.news.dto.CreateNewsServiceRequestDto;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;

import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
public class NewsServiceTest {

    @Mock
    private NewsRepository newsRepository;

    @InjectMocks
    private NewsService newsService;

    @Test
    @DisplayName("뉴스 생성 성공")
    public void createNews_success() {
        //given
        CreateNewsServiceRequestDto requestDto = new CreateNewsServiceRequestDto(
                "title",
                "source",
                LocalDate.of(2024, 1, 1),
                "https://www.naver.com/",
                true
        );
//        News news = requestDto.toEntity();
//
//        // mocking
//        when(newsRepository.save(any(News.class))).thenReturn(news);

        //when
        ApiResponse<News> response = newsService.createNews(requestDto);

        //then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("News를 성공적으로 등록하였습니다.", response.getMessage());
    }

}
