package studio.studioeye.domain.news.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import studio.studioeye.domain.news.domain.NewsFile;

@Repository
public interface NewsFileRepository extends JpaRepository<NewsFile, Long> {

    void deleteAllByNewsId(Long newsId);
}
