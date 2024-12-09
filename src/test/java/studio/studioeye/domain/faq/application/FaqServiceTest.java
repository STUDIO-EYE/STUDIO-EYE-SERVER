package studio.studioeye.domain.faq.application;

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
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import studio.studioeye.domain.faq.dao.FaqQuestions;
import studio.studioeye.domain.faq.dao.FaqRepository;
import studio.studioeye.domain.faq.domain.Faq;
import studio.studioeye.domain.faq.dto.request.CreateFaqServiceRequestDto;
import studio.studioeye.domain.faq.dto.request.UpdateFaqServiceRequestDto;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;
import studio.studioeye.infrastructure.s3.S3Adapter;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FaqServiceTest {
    @InjectMocks
    private FaqService faqService;
    @Mock
    private FaqRepository faqRepository;
    @Mock
    private S3Adapter s3Adapter;

    @Test
    @DisplayName("FAQ 생성 성공 테스트")
    void createFaqSuccess() {
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
    @DisplayName("FAQ 생성 실패 테스트 - 질문이 비어 있는 경우")
    void createFaqFail() {
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
    @DisplayName("FAQ 생성 실패 테스트 - 답변이 비어 있는 경우")
    void createFaqFail_EmptyAnswer() {
        // given
        String question = "test question";
        String answer = ""; // 비어 있는 답변
        Boolean visibility = true;
        CreateFaqServiceRequestDto requestDto = new CreateFaqServiceRequestDto(question, answer, visibility);
        // when
        ApiResponse<Faq> response = faqService.createFaq(requestDto);
        // then
        assertNotNull(response);
        assertEquals(ErrorCode.FAQ_IS_EMPTY.getStatus(), response.getStatus());
        assertEquals(ErrorCode.FAQ_IS_EMPTY.getMessage(), response.getMessage());
        Mockito.verify(faqRepository, never()).save(any());
    }

    @Test
    @DisplayName("FAQ 전체 조회 성공 테스트")
    void retrieveAllFaqSuccess() {
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
    @DisplayName("FAQ 전체 조회 실패 테스트 - FAQ가 없는 경우")
    void retrieveAllFaqFail() {
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
    @DisplayName("FAQ 단일 조회 성공 테스트")
    void retrieveFaqByIdSuccess() {
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

    @Test
    @DisplayName("FAQ 단일 조회 실패 테스트 - 잘못된 ID")
    void retrieveFaqByIdFail() {
        // given
        Long id = 2L;
        Faq savedFaq = new Faq("Test Question1", "Test Answer1", true);
        // stub
        when(faqRepository.findById(id)).thenReturn(Optional.empty());
        // when
        ApiResponse<Faq> response = faqService.retrieveFaqById(id);
        Faq findFaq = response.getData();
        // then
        assertNotNull(response);
        assertNotEquals(findFaq, savedFaq);
        assertEquals(ErrorCode.INVALID_FAQ_ID.getStatus(), response.getStatus());
        assertEquals(ErrorCode.INVALID_FAQ_ID.getMessage(), response.getMessage());
        Mockito.verify(faqRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("FAQ 수정 성공 테스트")
    void updateFaqSuccess() {
        // given
        Faq savedFaq = new Faq("Test Question1", "Test Answer1", true);
        String question = "Test Question2";
        String answer = "Test Answer2";
        Boolean visibility = false;
        UpdateFaqServiceRequestDto requestDto = new UpdateFaqServiceRequestDto(
                1L, question, answer, visibility
        );
        // stub
        when(faqRepository.findById(requestDto.id())).thenReturn(Optional.of(savedFaq));
        when(faqRepository.save(any(Faq.class))).thenAnswer(invocation -> {
            Faq argumentFaq = invocation.getArgument(0);
            argumentFaq.updateTitle(question);
            argumentFaq.updateContent(answer);
            argumentFaq.updateVisibility(visibility);
            return new Faq(argumentFaq.getQuestion(), argumentFaq.getAnswer(), argumentFaq.getVisibility());
        });
        // when
        ApiResponse<Faq> response = faqService.updateFaq(requestDto);
        Faq findFaq = response.getData();
        // then
        assertNotNull(findFaq);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("FAQ를 성공적으로 수정하였습니다.", response.getMessage());
        assertNotEquals(findFaq, savedFaq);
        assertEquals(findFaq.getQuestion(), savedFaq.getQuestion());
        assertEquals(findFaq.getAnswer(), savedFaq.getAnswer());
        assertEquals(findFaq.getVisibility(), savedFaq.getVisibility());
        assertEquals(findFaq.getQuestion(), requestDto.question());
        assertEquals(findFaq.getAnswer(), requestDto.answer());
        assertEquals(findFaq.getVisibility(), requestDto.visibility());
        Mockito.verify(faqRepository, times(1)).findById(requestDto.id());
        Mockito.verify(faqRepository, times(1)).save(any(Faq.class));
    }

    @Test
    @DisplayName("FAQ 수정 실패 테스트 - 잘못된 ID")
    void updateFaqFail() {
        // given
        Faq savedFaq = new Faq("Test Question1", "Test Answer1", true);
        Long invalidId = 999L;
        String question = "Test Question2";
        String answer = "Test Answer2";
        Boolean visibility = false;
        UpdateFaqServiceRequestDto requestDto = new UpdateFaqServiceRequestDto(
                invalidId, question, answer, visibility
        );
        // stub
        when(faqRepository.findById(requestDto.id())).thenReturn(Optional.empty());
        // when
        ApiResponse<Faq> response = faqService.updateFaq(requestDto);
        Faq findFaq = response.getData();
        // then
        assertEquals(ErrorCode.INVALID_FAQ_ID.getStatus(), response.getStatus());
        assertEquals(ErrorCode.INVALID_FAQ_ID.getMessage(), response.getMessage());
        assertNotEquals(findFaq, savedFaq);
        assertNull(findFaq);
        Mockito.verify(faqRepository, times(1)).findById(invalidId);  // repository 메소드 호출 검증
        Mockito.verify(faqRepository, Mockito.never()).save(any());
    }

    @Test
    @DisplayName("FAQ 삭제 성공 테스트")
    void deleteFaqSuccess() {
        // given
        Long id = 1L;
        Faq savedFaq = new Faq("Test Question1", "Test Answer1", true);
        // stub
        when(faqRepository.findById(id)).thenReturn(Optional.of(savedFaq));
        // when
        ApiResponse<String> response = faqService.deleteFaq(id);
        // then
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("FAQ를 성공적으로 삭제했습니다.", response.getMessage());
        Mockito.verify(faqRepository, times(1)).findById(id);
        Mockito.verify(faqRepository, times(1)).delete(savedFaq);
    }

    @Test
    @DisplayName("FAQ 삭제 실패 테스트 - 잘못된 ID")
    void deleteFaqFail() {
        // given
        Long invalidId = 999L;
        // stub
        when(faqRepository.findById(invalidId)).thenReturn(Optional.empty());
        // when
        ApiResponse<String> response = faqService.deleteFaq(invalidId);
        // then
        assertEquals(ErrorCode.INVALID_FAQ_ID.getStatus(), response.getStatus());
        assertEquals(ErrorCode.INVALID_FAQ_ID.getMessage(), response.getMessage());
        Mockito.verify(faqRepository, times(1)).findById(invalidId);
        Mockito.verify(faqRepository, never()).delete(any());
    }

    @Test
    @DisplayName("FAQ 다중 삭제 성공 테스트")
    void deleteFaqsSuccess() {
        // given
        List<Long> ids = List.of(1L, 2L, 3L);
        List<Faq> faqs = ids.stream()
                .map(id -> new Faq("Test Question " + id, "Test Answer " + id, true))
                .toList();
        for (int i = 0; i < ids.size(); i++) {
            when(faqRepository.findById(ids.get(i))).thenReturn(Optional.of(faqs.get(i)));
        }
        // when
        ApiResponse<String> response = faqService.deleteFaqs(ids);
        // then
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("FAQ를 성공적으로 삭제했습니다.", response.getMessage());
        for (Faq faq : faqs) {
            verify(faqRepository, times(1)).delete(faq);
        }
    }

    @Test
    @DisplayName("FAQ 다중 삭제 실패 테스트 - 일부 잘못된 ID")
    void deleteFaqsPartialFail() {
        // given
        List<Long> ids = List.of(1L, 2L, 999L);
        Faq faq1 = new Faq("Question 1", "Answer 1", true);
        Faq faq2 = new Faq("Question 2", "Answer 2", true);
        when(faqRepository.findById(1L)).thenReturn(Optional.of(faq1));
        when(faqRepository.findById(2L)).thenReturn(Optional.of(faq2));
        when(faqRepository.findById(999L)).thenReturn(Optional.empty());
        // when
        ApiResponse<String> response = faqService.deleteFaqs(ids);
        // then
        assertEquals(ErrorCode.INVALID_FAQ_ID.getStatus(), response.getStatus());
        assertEquals(ErrorCode.INVALID_FAQ_ID.getMessage(), response.getMessage());
        verify(faqRepository, times(1)).findById(1L);
        verify(faqRepository, times(1)).findById(2L);
        verify(faqRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("FAQ 제목 조회 성공 테스트")
    void retrieveAllFaqTitleSuccess() {
        // given
        List<FaqQuestions> faqTitles = List.of(
                new FaqQuestions() {
                    public Long getId() { return 1L; }
                    public String getQuestion() { return "Title 1"; }
                },
                new FaqQuestions() {
                    public Long getId() { return 2L; }
                    public String getQuestion() { return "Title 2"; }
                }
        );
        when(faqRepository.findAllQuestions()).thenReturn(faqTitles);
        // when
        ApiResponse<List<FaqQuestions>> response = faqService.retrieveAllFaqTitle();
        List<FaqQuestions> retrievedTitles = response.getData();
        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("FAQ 목록을 성공적으로 조회했습니다.", response.getMessage());
        assertEquals(faqTitles.size(), retrievedTitles.size());
        Mockito.verify(faqRepository, times(1)).findAllQuestions();
    }

    @Test
    @DisplayName("FAQ 제목 조회 실패 테스트")
    void retrieveAllFaqTitleFail() {
        // given
        when(faqRepository.findAllQuestions()).thenReturn(new ArrayList<>());
        // when
        ApiResponse<List<FaqQuestions>> response = faqService.retrieveAllFaqTitle();
        // then
        assertNotNull(response);
        assertEquals(ErrorCode.INVALID_FAQ_ID.getStatus(), response.getStatus());
        assertEquals(ErrorCode.INVALID_FAQ_ID.getMessage(), response.getMessage());
        Mockito.verify(faqRepository, times(1)).findAllQuestions();
    }

    @Test
    @DisplayName("FAQ 페이지 조회 성공 테스트")
    void retrieveFaqPageSuccess() {
        // given
        Pageable pageable = PageRequest.of(0, 5);
        List<Faq> faqList = List.of(
                new Faq("Question 1", "Answer 1", true),
                new Faq("Question 2", "Answer 2", true)
        );
        Page<Faq> faqPage = new PageImpl<>(faqList, pageable, faqList.size());
        when(faqRepository.findAll(pageable)).thenReturn(faqPage);
        // when
        Page<Faq> result = faqService.retrieveFaqPage(0, 5);
        // then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals("Question 1", result.getContent().get(0).getQuestion());
        verify(faqRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("FAQ 페이지 조회 실패 테스트 - 잘못된 페이지 크기")
    void retrieveFaqPage_InvalidPageSize_Fail() {
        // given
        int invalidPageSize = -1;
        // when
        assertThrows(IllegalArgumentException.class, () -> faqService.retrieveFaqPage(0, invalidPageSize));
        verify(faqRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("createFaq 실패 테스트 - visibility가 null인 경우")
    void createFaqFail_NullVisibility() {
        // given
        String question = "test question";
        String answer = "test answer";
        Boolean visibility = null; // visibility가 null
        CreateFaqServiceRequestDto requestDto = new CreateFaqServiceRequestDto(question, answer, visibility);
        // when
        ApiResponse<Faq> response = faqService.createFaq(requestDto);
        // then
        assertNotNull(response);
        assertEquals(ErrorCode.FAQ_IS_EMPTY.getStatus(), response.getStatus());
        assertEquals(ErrorCode.FAQ_IS_EMPTY.getMessage(), response.getMessage());
        Mockito.verify(faqRepository, never()).save(any());
    }

    @Test
    @DisplayName("retrieveFaqPage 실패 테스트 - 페이지가 음수인 경우")
    void retrieveFaqPage_NegativePage_Fail() {
        // given
        int invalidPage = -1; // 음수 페이지
        int pageSize = 5;
        // when
        assertThrows(IllegalArgumentException.class, () -> faqService.retrieveFaqPage(invalidPage, pageSize));
        verify(faqRepository, never()).findAll(any(Pageable.class));
    }
}
