package studio.studioeye.domain.news.application;

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
import studio.studioeye.domain.news.dao.NewsRepository;
import studio.studioeye.domain.news.domain.News;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

    @InjectMocks
    private NewsService newsService;

    @Mock
    private NewsRepository newsRepository;

    @Test
    @DisplayName("News 페이지네이션 조회 성공 테스트")
    void retrieveAllNewsSuccess() {
        // given

        int page = 0;
        int size = 2;

        Pageable pageable = PageRequest.of(page, size);

        List<News> newsList = new ArrayList<>();
        newsList.add(new News("Test Title1", "Test Source1",  LocalDate.now(), "Test URL1", true));
        newsList.add(new News("Test Title2", "Test Source2",  LocalDate.now(), "Test URL2", true));
        newsList.add(new News("Test Title3", "Test Source3",  LocalDate.now(), "Test URL3", true));
        newsList.add(new News("Test Title4", "Test Source4",  LocalDate.now(), "Test URL4", true));
        newsList.add(new News("Test Title5", "Test Source5",  LocalDate.now(), "Test URL5", true));
        newsList.add(new News("Test Title6", "Test Source6",  LocalDate.now(), "Test URL6", true));
        newsList.add(new News("Test Title7", "Test Source7",  LocalDate.now(), "Test URL7", true));
        newsList.add(new News("Test Title8", "Test Source8",  LocalDate.now(), "Test URL8", true));
        newsList.add(new News("Test Title9", "Test Source9",  LocalDate.now(), "Test URL9", true));
        newsList.add(new News("Test Title10", "Test Source10",  LocalDate.now(), "Test URL10", true));

        Page<News> newsPage = new PageImpl<>(newsList, pageable, newsList.size());

        // stub
        when(newsRepository.findAll(pageable)).thenReturn(newsPage);

        // when
        Page<News> result = newsService.retrieveNewsPage(page, size);

        // then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isEqualTo(newsPage);
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
}