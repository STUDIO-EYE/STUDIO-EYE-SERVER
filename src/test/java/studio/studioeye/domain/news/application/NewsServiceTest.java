package studio.studioeye.domain.news.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import studio.studioeye.domain.news.dao.NewsRepository;
import studio.studioeye.domain.news.domain.News;
import studio.studioeye.domain.news.dto.CreateNewsServiceRequestDto;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

    @InjectMocks
    private NewsService newsService;

    @Mock
    private NewsRepository newsRepository;

    @Test
    @DisplayName("뉴스 생성 성공")
    public void createNewsSuccess() {
        //given
        CreateNewsServiceRequestDto requestDto = new CreateNewsServiceRequestDto(
                "title",
                "source",
                LocalDate.of(2024, 1, 1),
                "https://www.naver.com/",
                true
        );

        //when
        ApiResponse<News> response = newsService.createNews(requestDto);

        //then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("News를 성공적으로 등록하였습니다.", response.getMessage());
    }

    @Test
    @DisplayName("뉴스 생성 실패 - 타이틀이 비어있는 경우")
    public void createNewsFail() {
        // given: 타이틀 필드가 비어있는 DTO를 생성
        CreateNewsServiceRequestDto requestDto = new CreateNewsServiceRequestDto(
                "", // title이 비어있음
                "source",
                LocalDate.of(2024, 1, 1),
                "https://www.naver.com/",
                true
        );

        // when: NewsService의 createNews 메서드를 호출
        ApiResponse<News> response = newsService.createNews(requestDto);

        // then: 오류 응답이 반환되는지 확인
        assertNotNull(response);
        assertEquals(ErrorCode.NEWS_IS_EMPTY.getStatus(), response.getStatus()); // 에러 코드 검증
        assertEquals(ErrorCode.NEWS_IS_EMPTY.getMessage(), response.getMessage()); // 에러 메시지 검증
    }
    @Test
    @DisplayName("뉴스 전체 조회 성공 테스트")
    void retrieveAllNewsSuccess() {
        // given
        List<News> newsList = new ArrayList<>();
        newsList.add(new News("Test Title1", "Test Source1",  LocalDate.now(), "Test URL1", true));
        newsList.add(new News("Test Title2", "Test Source2",  LocalDate.now(), "Test URL2", true));
        newsList.add(new News("Test Title3", "Test Source3",  LocalDate.now(), "Test URL3", true));

        List<News> savedNews = newsList;

        // stub
        when(newsRepository.findAll()).thenReturn(savedNews);

        // when
        ApiResponse<List<News>> response = newsService.retrieveAllNews();
        List<News> findNews = response.getData();

        // then
        Assertions.assertThat(findNews).isEqualTo(savedNews);
        Assertions.assertThat(findNews.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("뉴스 전체 조회 실패 테스트 - 데이터 없음")
    void retrieveAllNewsFail() {
        // given
        List<News> newsList = new ArrayList<>();

        // stub
        when(newsRepository.findAll()).thenReturn(newsList);

        // when
        ApiResponse<List<News>> response = newsService.retrieveAllNews();

        // then
        Assertions.assertThat(response.getData()).isNull();
        Assertions.assertThat(response.getMessage()).isEqualTo("News가 존재하지 않습니다.");
    }
    @Test
    @DisplayName("단일 뉴스 조회 성공 테스트")
    void retrieveNewsByIdSuccess() {
        // given
        Long id = 1L;
        News savedNews = new News("Test Title1", "Test Source1",  LocalDate.now(), "Test URL1", true);
        // stub
        when(newsRepository.findById(id)).thenReturn(Optional.of(savedNews));

        // when
        ApiResponse<News> response = newsService.retrieveNewsById(id);
        News findNews = response.getData();

        // then
        Assertions.assertThat(findNews).isEqualTo(savedNews);
        Assertions.assertThat(findNews.getTitle()).isEqualTo(savedNews.getTitle());
        Assertions.assertThat(findNews.getSource()).isEqualTo(savedNews.getSource());
        Assertions.assertThat(findNews.getPubDate()).isEqualTo(savedNews.getPubDate());
        Assertions.assertThat(findNews.getUrl()).isEqualTo(savedNews.getUrl());
        Assertions.assertThat(findNews.getVisibility()).isEqualTo(savedNews.getVisibility());
    }

    @Test
    @DisplayName("단일 뉴스 조회 실패 테스트")
    void retrieveNewsByIdFail() {
        // given
        Long id = 2L;
        News savedNews = new News("Test Title1", "Test Source1",  LocalDate.now(), "Test URL1", true);
        // stub
        when(newsRepository.findById(id)).thenReturn(Optional.empty());

        // when
        ApiResponse<News> response = newsService.retrieveNewsById(id);
        News findNews = response.getData();

        // then
        Assertions.assertThat(findNews).isNotEqualTo(savedNews);
    }

    @Test
    @DisplayName("단일 뉴스 삭제 성공 테스트")
    void deleteNewsSuccess() {
        // given
        Long id = 1L;
        News savedNews = new News("Test Title1", "Test Source1", LocalDate.now(), "Test URL1", true);
        // stub
        when(newsRepository.findById(id)).thenReturn(Optional.of(savedNews));

        // when
        ApiResponse<String> response = newsService.deleteNews(id);

        // then
        Assertions.assertThat(response.getMessage()).isEqualTo("News를 성공적으로 삭제했습니다.");
        // method call verify
        Mockito.verify(newsRepository, times(1)).delete(savedNews);
        Mockito.verify(newsRepository, times(1)).delete(Mockito.any()); // any argument
    }

    @Test
    @DisplayName("단일 뉴스 삭제 실패 테스트")
    void deleteNewsFail() {
        // given
        Long id = 1L;
        // stub
        when(newsRepository.findById(id)).thenReturn(Optional.empty());

        // when
        ApiResponse<String> response = newsService.deleteNews(id);

        // then
        Assertions.assertThat(response.getStatus()).isEqualTo(ErrorCode.INVALID_NEWS_ID.getStatus());
        // method call verify
        Mockito.verify(newsRepository, Mockito.never()).delete(Mockito.any());
    }

    @Test
    @DisplayName("뉴스 리스트 삭제 성공 테스트")
    void deleteNewsListSuccess() {
        // given
        List<News> newsList = new ArrayList<>();
        newsList.add(new News("Test Title1", "Test Source1",  LocalDate.now(), "Test URL1", true));
        newsList.add(new News("Test Title2", "Test Source2",  LocalDate.now(), "Test URL2", true));
        newsList.add(new News("Test Title3", "Test Source3",  LocalDate.now(), "Test URL3", true));

        List<Long> ids = Arrays.asList(1L, 2L, 3L);

        // stub
        when(newsRepository.findById(1L)).thenReturn(Optional.of(newsList.get(0)));
        when(newsRepository.findById(2L)).thenReturn(Optional.of(newsList.get(1)));
        when(newsRepository.findById(3L)).thenReturn(Optional.of(newsList.get(2)));

        // when
        ApiResponse<String> response = newsService.deleteNewsList(ids);

        // then
        Assertions.assertThat(response.getMessage()).isEqualTo("News를 성공적으로 삭제했습니다.");
        // method call verify
        Mockito.verify(newsRepository, times(1)).delete(newsList.get(0));
        Mockito.verify(newsRepository, times(1)).delete(newsList.get(1));
        Mockito.verify(newsRepository, times(1)).delete(newsList.get(2));
    }

}