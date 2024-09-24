package studio.studioeye.domain.news.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import studio.studioeye.domain.news.domain.NewsFile;

import java.time.LocalDate;
import java.util.List;

public record CreateNewsRequestDto(
    @Schema(description = "News 제목, 빈 값/공백/null 을 허용하지 않습니다.")
    @NotBlank(message = "News 제목은 필수 값입니다.")
    String title,
    @Schema(description = "News 출처, 빈 값/공백/null 을 허용하지 않습니다.")
    @NotBlank(message = "News 출처는 필수 값입니다.")
    String source,
    @Schema(description = "News 날짜, 빈 값/공백/null 을 허용하지 않습니다.")
    @NotBlank(message = "News 날짜은 필수 값입니다.")
    LocalDate pubDate,
    @Schema(description = "News 내용, 빈 값/공백/null 을 허용하지 않습니다.")
    @NotBlank(message = "News 내용은 필수 값입니다.")
    String content,
    @Schema(description = "News 공개여부, 빈 값/공백/null 을 허용하지 않습니다.")
    @NotBlank(message = "News 공개여부는 필수 값입니다.")
    Boolean visibility,
    @Schema(description = "News file List, 빈 값/공백/null 을 허용하지 않습니다.")
    List<NewsFile> newsFiles

) {
    public CreateNewsServiceRequestDto toServiceNews() {
        return new CreateNewsServiceRequestDto(title, source, pubDate, content, visibility, newsFiles);
    }
}
