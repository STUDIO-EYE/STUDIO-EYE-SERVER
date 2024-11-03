package studio.studioeye.domain.views.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import studio.studioeye.domain.menu.domain.MenuTitle;
import studio.studioeye.domain.project.domain.ArtworkCategory;
import studio.studioeye.domain.recruitment.application.RecruitmentService;
import studio.studioeye.domain.recruitment.dao.RecruitmentRepository;
import studio.studioeye.domain.recruitment.domain.Recruitment;
import studio.studioeye.domain.recruitment.dto.request.CreateRecruitmentServiceRequestDto;
import studio.studioeye.domain.views.dao.ViewsRepository;
import studio.studioeye.domain.views.domain.Views;
import studio.studioeye.domain.views.dto.request.CreateViewsServiceRequestDto;
import studio.studioeye.global.common.response.ApiResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class ViewsServiceTest {
    @InjectMocks
    private ViewsService viewsService;

    @Mock
    private ViewsRepository viewsRepository;

    @Test
    @DisplayName("조회수 생성 성공 테스트")
    public void createViewsSuccess() {
        //given
        Integer year = 2024;
        Integer month = 11;
        Long views = 1L;
        MenuTitle menu = MenuTitle.ABOUT;
        ArtworkCategory category = ArtworkCategory.ALL;


        CreateViewsServiceRequestDto requestDto = new CreateViewsServiceRequestDto(
                year, month, views, menu, category
        );

        //when
        ApiResponse<Views> response = viewsService.createViews(requestDto);

        //then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("조회 수 등록을 완료했습니다.", response.getMessage());
        Mockito.verify(viewsRepository, times(1)).save(any(Views.class));
    }
}
