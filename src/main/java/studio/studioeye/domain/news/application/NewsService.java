package studio.studioeye.domain.news.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import studio.studioeye.domain.news.dao.NewsFileRepository;
import studio.studioeye.domain.news.dao.NewsRepository;
import studio.studioeye.domain.news.domain.News;
import studio.studioeye.domain.news.domain.NewsFile;
import studio.studioeye.domain.news.dto.CreateNewsServiceRequestDto;
import studio.studioeye.domain.news.dto.UpdateNewsServiceRequestDto;
import studio.studioeye.infrastructure.s3.S3Adapter;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;
    private final NewsFileRepository newsFileRepository;
    private final S3Adapter s3Adapter;

    public ApiResponse<News> createNews(CreateNewsServiceRequestDto dto, List<MultipartFile> files) throws IOException {
        if(dto.title().trim().isEmpty() || dto.content().trim().isEmpty() || dto.visibility() == null) {
            return ApiResponse.withError(ErrorCode.NEWS_IS_EMPTY);
        }
        News news = dto.toEntity();
        newsRepository.save(news);

        // 파일 업로드
        List<NewsFile> newsFiles = new ArrayList<>();

        if (files != null){
            for (MultipartFile file : files) {
//                String s3key = s3Adapter.uploadFile(file);
                newsFiles.add(NewsFile.builder()
                        .fileName(file.getOriginalFilename())
                        .filePath(s3Adapter.uploadFile(file).getData())
//                        .s3key(s3key)
                        .news(news)
                        .build());
            }
            newsFileRepository.saveAllAndFlush(newsFiles);
        }
        return ApiResponse.ok("News를 성공적으로 등록하였습니다.");
    }

    public ApiResponse<List<News>> retrieveAllNews() {
        List<News> newsList = newsRepository.findAll();
        if(newsList.isEmpty()) {
            return ApiResponse.ok("News가 존재하지 않습니다.");
        }
        return ApiResponse.ok("News 목록을 성공적으로 조회했습니다.", newsList);
    }

    public ApiResponse<News> retrieveNewsById(Long id) {
        Optional<News> optionalNews = newsRepository.findById(id);
        if(optionalNews.isEmpty()) {
            return ApiResponse.ok("News가 존재하지 않습니다.");
        }
        News news = optionalNews.get();
        return ApiResponse.ok("News를 성공적으로 조회했습니다.", news);
    }

    public Page<News> retrieveNewsPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return newsRepository.findAll(pageable);
    }

    public ApiResponse<News> updateNews(UpdateNewsServiceRequestDto dto, List<MultipartFile> files) throws IOException {
        String title = dto.title().trim();
        String source = dto.source().trim();
        LocalDate pubDate = dto.pubDate();
        String content = dto.content().trim();
        Boolean visibility = dto.visibility();

        Optional<News> optionalNews = newsRepository.findById(dto.id());
        if(optionalNews.isEmpty()) {
            return ApiResponse.withError(ErrorCode.INVALID_NEWS_ID);
        }

        News news = optionalNews.get();
        if(!title.isEmpty()) {
            news.updateTitle(title);
        }
        if(!source.isEmpty()) {
            news.updateSource(source);
        }
        if(!(pubDate == null)) {
            news.updatePubDate(pubDate);
        }
        if(!content.isEmpty()) {
            news.updateContent(content);
        }
        if(visibility != null) {
            news.updateVisibility(visibility);
        }
        newsFileRepository.deleteAllByNewsId(optionalNews.get().getId());
        // 파일 업로드
        List<NewsFile> newsFiles = new ArrayList<>();
        if (files != null){
            for (MultipartFile file : files) {
//                String s3key = s3Adapter.uploadFile(file);
                newsFiles.add(NewsFile.builder()
                        .fileName(file.getOriginalFilename())
                        .filePath(s3Adapter.uploadFile(file).getData())
//                        .s3key(s3key)
                        .news(news)
                        .build());
            }
            newsFileRepository.saveAllAndFlush(newsFiles);
        }
        return ApiResponse.ok("News를 성공적으로 수정하였습니다.");
    }

    public ApiResponse<String> deleteNews(Long id) {
        Optional<News> optionalNews = newsRepository.findById(id);
        if(optionalNews.isEmpty()) {
            return ApiResponse.withError(ErrorCode.INVALID_NEWS_ID);
        }
        News news = optionalNews.get();
        newsRepository.delete(news);
        newsFileRepository.deleteAllByNewsId(news.getId());
        return ApiResponse.ok("News를 성공적으로 삭제했습니다.");
    }
    public ApiResponse<String> deleteNewsList(List<Long> ids) {
        for(Long id : ids) {
            Optional<News> optionalNews = newsRepository.findById(id);
            if (optionalNews.isEmpty()) {
                return ApiResponse.withError(ErrorCode.INVALID_NEWS_ID);
            }
            News news = optionalNews.get();
            newsRepository.delete(news);
        }
        return ApiResponse.ok("News를 성공적으로 삭제했습니다.");
    }
}
