package studio.studioeye.domain.faq.application;

import org.assertj.core.api.Assertions;
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
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @Test
    @DisplayName("FAQ 생성 실패 - question이 비어 있는 경우")
    public void createFaqFail() {
        // given
        String question = "";
        String answer = "test answer";
        Boolean visibility = true;
        CreateFaqServiceRequestDto requestDto = new CreateFaqServiceRequestDto(
                question, answer, visibility
        );

        // when
        ApiResponse<Faq> response = faqService.createFaq(requestDto);

        // then
        assertNotNull(response);
        assertEquals(ErrorCode.FAQ_IS_EMPTY.getStatus(), response.getStatus());
        assertEquals(ErrorCode.FAQ_IS_EMPTY.getMessage(), response.getMessage());
        Mockito.verify(faqRepository, never()).save(any());
    }

    @Test
    @DisplayName("FAQ 전체 조회 성공")
    public void retrieveAllFaqSuccess() {
        // given
        List<Faq> faqList = new ArrayList<>();
        faqList.add(new Faq("Test Question1", "Test Answer1", true));
        faqList.add(new Faq("Test Question2", "Test Answer2", true));
        faqList.add(new Faq("Test Question3", "Test Answer3", true));

        List<Faq> savedFaqList = faqList;

        // stub
        when(faqRepository.findAll()).thenReturn(savedFaqList);

        // when
        ApiResponse<List<Faq>> response = faqService.retrieveAllFaq();
        List<Faq> findFaq = response.getData();

        // then
        assertNotNull(response);
        assertEquals(findFaq, savedFaqList);
        assertEquals(findFaq.size(), savedFaqList.size());
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("FAQ 목록을 성공적으로 조회했습니다.", response.getMessage());
        Mockito.verify(faqRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("FAQ 전체 조회 실패")
    public void retrieveAllFaqFail() {
        // given
        List<Faq> savedFaqList = new ArrayList<>();

        // stub
        when(faqRepository.findAll()).thenReturn(savedFaqList);

        // when
        ApiResponse<List<Faq>> response = faqService.retrieveAllFaq();
        List<Faq> findFaq = response.getData();

        // then
        assertNotNull(response);
        assertNull(findFaq);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("FAQ가 존재하지 않습니다.", response.getMessage());
        Mockito.verify(faqRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("FAQ 단일 조회 성공")
    public void retrieveFaqByIdSuccess() {
        // given
        Long id = 1L;
        Faq savedFaq = new Faq("Test Question1", "Test Answer1", true);

        // stub
        when(faqRepository.findById(id)).thenReturn(Optional.of(savedFaq));

        // when
        ApiResponse<Faq> response = faqService.retrieveFaqById(id);
        Faq findFaq = response.getData();

        // then
        assertNotNull(findFaq);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("FAQ를 성공적으로 조회했습니다.", response.getMessage());
        assertEquals(findFaq, savedFaq);
        assertEquals(findFaq.getQuestion(), savedFaq.getQuestion());
        assertEquals(findFaq.getAnswer(), savedFaq.getAnswer());
        assertEquals(findFaq.getVisibility(), savedFaq.getVisibility());
        Mockito.verify(faqRepository, times(1)).findById(id);
    }
}
