package studio.studioeye.domain.faq.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import studio.studioeye.domain.faq.dao.FaqRepository;
import studio.studioeye.domain.faq.domain.Faq;
import studio.studioeye.domain.faq.dto.request.CreateFaqServiceRequestDto;
import studio.studioeye.domain.recruitment.domain.Recruitment;
import studio.studioeye.global.common.response.ApiResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class FaqServiceTest {
    @InjectMocks
    private FaqService faqService;

    @Mock
    private FaqRepository faqRepository;

    @Test
    @DisplayName("FAQ 생성 성공")
    public void createFaqSuccess() {
        // given
        String question = "test question";
        String answer = "test answer";
        Boolean visibility = true;
        CreateFaqServiceRequestDto requestDto = new CreateFaqServiceRequestDto(
                question, answer, visibility
        );

        // when
        ApiResponse<Faq> response = faqService.createFaq(requestDto);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("FAQ를 성공적으로 등록하였습니다.", response.getMessage());
        Mockito.verify(faqRepository, times(1)).save(any(Faq.class));
    }
}
