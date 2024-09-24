package studio.studioeye.domain.news.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import studio.studioeye.domain.news.domain.News;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

}
