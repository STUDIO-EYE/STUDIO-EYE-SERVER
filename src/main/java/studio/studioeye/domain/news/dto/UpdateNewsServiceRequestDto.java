package studio.studioeye.domain.news.dto;

import java.time.LocalDate;

public record UpdateNewsServiceRequestDto(
        Long id,
        String title,
        String source,
        LocalDate pubDate,
        String content,
        Boolean visibility
) {

}
