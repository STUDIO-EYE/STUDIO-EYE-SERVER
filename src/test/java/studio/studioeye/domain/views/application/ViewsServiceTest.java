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
import studio.studioeye.domain.views.dao.ViewsRepository;
import studio.studioeye.domain.views.dao.ViewsSummary;
import studio.studioeye.domain.views.domain.Views;
import studio.studioeye.domain.views.dto.request.CreateViewsServiceRequestDto;
import studio.studioeye.domain.views.dto.request.UpdateViewsServiceRequestDto;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ViewsServiceTest {
    @InjectMocks
    private ViewsService viewsService;
    @Mock
    private ViewsRepository viewsRepository;
    @Test
    @DisplayName("조회수 생성 성공 테스트")
    void createViewsSuccess() {
        // given
        Integer year = 2024;
        Integer month = 11;
        Long views = 1L;
        MenuTitle menu = MenuTitle.ABOUT;
        ArtworkCategory category = ArtworkCategory.ALL;
        CreateViewsServiceRequestDto requestDto = new CreateViewsServiceRequestDto(
                year, month, views, menu, category
        );
        // when
        ApiResponse<Views> response = viewsService.createViews(requestDto);
        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("조회 수 등록을 완료했습니다.", response.getMessage());
        Mockito.verify(viewsRepository, times(1)).save(any(Views.class));
    }

    @Test
    @DisplayName("조회수 생성 실패 테스트 - 유효하지 않은 월 형식(1~12)인 경우")
    void createViewsFail_invalidMonth() {
        // given
        Integer year = 2024;
        Integer month = 13;
        Long views = 1L;
        MenuTitle menu = MenuTitle.ABOUT;
        ArtworkCategory category = ArtworkCategory.ALL;
        CreateViewsServiceRequestDto requestDto = new CreateViewsServiceRequestDto(
                year, month, views, menu, category
        );
        // when
        ApiResponse<Views> response = viewsService.createViews(requestDto);
        // then
        assertNotNull(response);
        assertEquals(ErrorCode.INVALID_VIEWS_MONTH.getStatus(), response.getStatus());
        assertEquals(ErrorCode.INVALID_VIEWS_MONTH.getMessage(), response.getMessage());
        Mockito.verify(viewsRepository, never()).save(any(Views.class));
    }

    @Test
    @DisplayName("조회수 생성 실패 테스트 - 이미 해당 연월의 조회수가 존재하는 경우")
    void createViewsFail_alreadyExisted() {
        // given
        Integer year = 2024;
        Integer month = 11;
        Long views = 1L;
        MenuTitle menu = MenuTitle.ABOUT;
        ArtworkCategory category = ArtworkCategory.ALL;
        Views savedViews = Views.builder()
                .year(year)
                .month(month)
                .views(views)
                .menu(menu)
                .category(category)
                .createdAt(new Date())
                .build();
        CreateViewsServiceRequestDto requestDto = new CreateViewsServiceRequestDto(
                year, month, views, menu, category
        );
        // stub
        when(viewsRepository.findByYearAndMonth(year, month)).thenReturn(Optional.of(savedViews));
        // when
        ApiResponse<Views> response = viewsService.createViews(requestDto);
        // then
        assertNotNull(response);
        assertEquals(ErrorCode.ALREADY_EXISTED_DATA.getStatus(), response.getStatus());
        assertEquals(ErrorCode.ALREADY_EXISTED_DATA.getMessage(), response.getMessage());
        Mockito.verify(viewsRepository, never()).save(any(Views.class));
    }

    @Test
    @DisplayName("조회수 전체 조회 성공 테스트")
    void retrieveAllViewsSuccess() {
        // given
        List<Views> savedViewsList = new ArrayList<>();
        savedViewsList.add(new Views(2024, 6, 1L, MenuTitle.ABOUT, ArtworkCategory.ALL, new Date()));
        savedViewsList.add(new Views(2024, 7, 1L, MenuTitle.ABOUT, ArtworkCategory.ALL, new Date()));
        savedViewsList.add(new Views(2024, 8, 1L, MenuTitle.ABOUT, ArtworkCategory.ALL, new Date()));
        savedViewsList.add(new Views(2024, 9, 1L, MenuTitle.ABOUT, ArtworkCategory.ALL, new Date()));
        savedViewsList.add(new Views(2024, 10, 1L, MenuTitle.ABOUT, ArtworkCategory.ALL, new Date()));
        savedViewsList.add(new Views(2024, 11, 1L, MenuTitle.ABOUT, ArtworkCategory.ALL, new Date()));
        // stub
        when(viewsRepository.findAll()).thenReturn(savedViewsList);
        // when
        ApiResponse<List<Views>> response = viewsService.retrieveAllViews();
        List<Views> findViews = response.getData();
        // then
        assertNotNull(response);
        assertNotNull(findViews);
        assertEquals(savedViewsList, findViews);
        assertEquals(savedViewsList.size(), findViews.size());
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("조회수 목록을 성공적으로 조회했습니다.", response.getMessage());
        Mockito.verify(viewsRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("조회수 전체 조회 실패 테스트")
    void retrieveAllViewsFail() {
        // given
        List<Views> savedViewsList = new ArrayList<>();
        // stub
        when(viewsRepository.findAll()).thenReturn(savedViewsList);
        // when
        ApiResponse<List<Views>> response = viewsService.retrieveAllViews();
        List<Views> findViews = response.getData();
        // then
        assertNotNull(response);
        assertNull(findViews);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("조회수가 존재하지 않습니다.", response.getMessage());
        Mockito.verify(viewsRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("단일 조회수 조회 성공 테스트")
    void retrieveViewsByIdSuccess() {
        // given
        Long id = 1L;
        Integer year = 2024;
        Integer month = 11;
        Long views = 1L;
        MenuTitle menu = MenuTitle.ABOUT;
        ArtworkCategory category = ArtworkCategory.ALL;
        Views savedViews = Views.builder()
                .year(year)
                .month(month)
                .views(views)
                .menu(menu)
                .category(category)
                .createdAt(new Date())
                .build();
        // stub
        when(viewsRepository.findById(id)).thenReturn(Optional.of(savedViews));
        // when
        ApiResponse<Views> response = viewsService.retrieveViewsById(id);
        Views findViews = response.getData();
        // then
        assertNotNull(response);
        assertNotNull(findViews);
        assertEquals(savedViews, findViews);
        assertEquals(savedViews.getYear(), findViews.getYear());
        assertEquals(savedViews.getMonth(), findViews.getMonth());
        assertEquals(savedViews.getViews(), findViews.getViews());
        assertEquals(savedViews.getMenu(), findViews.getMenu());
        assertEquals(savedViews.getCategory(), findViews.getCategory());
        assertEquals(savedViews.getCreatedAt(), findViews.getCreatedAt());
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("조회수를 성공적으로 조회했습니다.", response.getMessage());
        Mockito.verify(viewsRepository, times(1)).findById(any(Long.class));
    }

    @Test
    @DisplayName("단일 조회수 조회 실패 테스트")
    void retrieveViewsByIdFail() {
        // given
        Long id = 1L;
        // stub
        when(viewsRepository.findById(id)).thenReturn(Optional.empty());
        // when
        ApiResponse<Views> response = viewsService.retrieveViewsById(id);
        Views findViews = response.getData();
        // then
        assertNotNull(response);
        assertNull(findViews);
        assertEquals(ErrorCode.INVALID_VIEWS_ID.getStatus(), response.getStatus());
        assertEquals(ErrorCode.INVALID_VIEWS_ID.getMessage(), response.getMessage());
        Mockito.verify(viewsRepository, times(1)).findById(any(Long.class));
    }

    @Test
    @DisplayName("해당 연도 조회수 전체 조회 성공 테스트")
    void retrieveViewsByYearSuccess() {
        // given
        Integer year = 2024;
        List<Views> savedViewsList = new ArrayList<>();
        savedViewsList.add(new Views(year, 1, 1L, MenuTitle.ABOUT, ArtworkCategory.ALL, new Date()));
        savedViewsList.add(new Views(year, 2, 1L, MenuTitle.ABOUT, ArtworkCategory.ALL, new Date()));
        savedViewsList.add(new Views(year, 3, 1L, MenuTitle.ABOUT, ArtworkCategory.ALL, new Date()));
        savedViewsList.add(new Views(year, 4, 1L, MenuTitle.ABOUT, ArtworkCategory.ALL, new Date()));
        savedViewsList.add(new Views(year, 5, 1L, MenuTitle.ABOUT, ArtworkCategory.ALL, new Date()));
        savedViewsList.add(new Views(year, 6, 1L, MenuTitle.ABOUT, ArtworkCategory.ALL, new Date()));
        savedViewsList.add(new Views(year, 7, 1L, MenuTitle.ABOUT, ArtworkCategory.ALL, new Date()));
        savedViewsList.add(new Views(year, 8, 1L, MenuTitle.ABOUT, ArtworkCategory.ALL, new Date()));
        savedViewsList.add(new Views(year, 9, 1L, MenuTitle.ABOUT, ArtworkCategory.ALL, new Date()));
        savedViewsList.add(new Views(year, 10, 1L, MenuTitle.ABOUT, ArtworkCategory.ALL, new Date()));
        savedViewsList.add(new Views(year, 11, 1L, MenuTitle.ABOUT, ArtworkCategory.ALL, new Date()));
        // stub
        when(viewsRepository.findByYear(year)).thenReturn(savedViewsList);
        // when
        ApiResponse<List<Views>> response = viewsService.retrieveViewsByYear(year);
        List<Views> findViews = response.getData();
        // then
        assertNotNull(response);
        assertNotNull(findViews);
        assertEquals(savedViewsList, findViews);
        assertEquals(savedViewsList.size(), findViews.size());
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("조회수 목록을 성공적으로 조회했습니다.", response.getMessage());
        Mockito.verify(viewsRepository, times(1)).findByYear(any(Integer.class));
    }

    @Test
    @DisplayName("해당 연도 조회수 전체 조회 실패 테스트")
    void retrieveViewsByYearFail() {
        // given
        Integer year = 2024;
        List<Views> savedViewsList = new ArrayList<>();
        // stub
        when(viewsRepository.findByYear(year)).thenReturn(savedViewsList);
        // when
        ApiResponse<List<Views>> response = viewsService.retrieveViewsByYear(year);
        List<Views> findViews = response.getData();
        // then
        assertNotNull(response);
        assertNull(findViews);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("조회수가 존재하지 않습니다.", response.getMessage());
        Mockito.verify(viewsRepository, times(1)).findByYear(any(Integer.class));
    }

    @Test
    @DisplayName("연도, 월로 조회수 상세 조회 성공 테스트")
    void retrieveViewsByYearMonthSuccess() {
        // given
        Integer year = 2024;
        Integer month = 11;
        Long views = 1L;
        MenuTitle menu = MenuTitle.ABOUT;
        ArtworkCategory category = ArtworkCategory.ALL;
        Views savedViews = Views.builder()
                .year(year)
                .month(month)
                .views(views)
                .menu(menu)
                .category(category)
                .createdAt(new Date())
                .build();
        // stub
        when(viewsRepository.findByYearAndMonth(year, month)).thenReturn(Optional.of(savedViews));
        // when
        ApiResponse<Views> response = viewsService.retrieveViewsByYearMonth(year, month);
        Views findViews = response.getData();
        // then
        assertNotNull(response);
        assertNotNull(findViews);
        assertEquals(savedViews, findViews);
        assertEquals(savedViews.getYear(), findViews.getYear());
        assertEquals(savedViews.getMonth(), findViews.getMonth());
        assertEquals(savedViews.getViews(), findViews.getViews());
        assertEquals(savedViews.getMenu(), findViews.getMenu());
        assertEquals(savedViews.getCategory(), findViews.getCategory());
        assertEquals(savedViews.getCreatedAt(), findViews.getCreatedAt());
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("조회수를 성공적으로 조회했습니다.", response.getMessage());
        Mockito.verify(viewsRepository, times(1)).findByYearAndMonth(any(Integer.class), any(Integer.class));
    }

    @Test
    @DisplayName("연도, 월로 조회수 상세 조회 실패 테스트")
    void retrieveViewsByYearMonthFail() {
        // given
        Integer year = 2024;
        Integer month = 11;
        // stub
        when(viewsRepository.findByYearAndMonth(year, month)).thenReturn(Optional.empty());
        // when
        ApiResponse<Views> response = viewsService.retrieveViewsByYearMonth(year, month);
        Views findViews = response.getData();
        // then
        assertNotNull(response);
        assertNull(findViews);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("조회수가 존재하지 않습니다.", response.getMessage());
        Mockito.verify(viewsRepository, times(1)).findByYearAndMonth(any(Integer.class), any(Integer.class));
    }

    @Test
    @DisplayName("기간(시작점(연도,월), 종료점(연도,월))으로 카테고리별, 메뉴별 전체 조회수 조회 성공 테스트")
    void retrieveAllMenuCategoryViewsByPeriodSuccess() {
        // given
        Integer startYear = 2024;
        Integer startMonth = 7;
        Integer endYear = 2024;
        Integer endMonth = 11;
        MenuTitle menu = MenuTitle.ABOUT;
        ArtworkCategory category = ArtworkCategory.ALL;

        List<ViewsSummary> savedViewsList = new ArrayList<>();
        savedViewsList.add(new ViewsSummary() {
            @Override
            public Integer getYear() {
                return 2024;
            }
            @Override
            public Integer getMonth() {
                return 7;
            }
            @Override
            public Long getViews() {
                return 2L;
            }
        });
        savedViewsList.add(new ViewsSummary() {
            @Override
            public Integer getYear() {
                return 2024;
            }
            @Override
            public Integer getMonth() {
                return 8;
            }
            @Override
            public Long getViews() {
                return 8L;
            }
        });
        savedViewsList.add(new ViewsSummary() {
            @Override
            public Integer getYear() {
                return 2024;
            }
            @Override
            public Integer getMonth() {
                return 11;
            }
            @Override
            public Long getViews() {
                return 24L;
            }
        });
        // stub
        when(viewsRepository.findByYearAndMonthBetweenAndMenuAndCategory(startYear, startMonth, endYear, endMonth, menu, category)).thenReturn(savedViewsList);
        // when
        ApiResponse<List<ViewsSummary>> response = viewsService.retrieveAllMenuCategoryViewsByPeriod(startYear, startMonth, endYear, endMonth, menu, category);
        List<ViewsSummary> findViews = response.getData();
        // then
        assertNotNull(response);
        assertNotNull(findViews);
        assertEquals(savedViewsList, findViews);
        assertEquals(savedViewsList.size(), findViews.size());
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("조회수 목록을 성공적으로 조회했습니다.", response.getMessage());
        Mockito.verify(viewsRepository, times(1)).findByYearAndMonthBetweenAndMenuAndCategory(any(Integer.class), any(Integer.class), any(Integer.class), any(Integer.class), any(MenuTitle.class), any(ArtworkCategory.class));
    }

    @Test
    @DisplayName("기간(시작점(연도,월), 종료점(연도,월))으로 카테고리별, 메뉴별 전체 조회수 조회 실패 테스트 - 유효하지 않은 월 형식(1~12)인 경우")
    void retrieveAllMenuCategoryViewsByPeriodFail_InvalidMonth() {
        // given
        Integer startYear = 2024;
        Integer startMonth = 9;
        Integer endYear = 2024;
        Integer endMonth = 13;
        MenuTitle menu = MenuTitle.ABOUT;
        ArtworkCategory category = ArtworkCategory.ALL;
        // when
        ApiResponse<List<ViewsSummary>> response = viewsService.retrieveAllMenuCategoryViewsByPeriod(startYear, startMonth, endYear, endMonth, menu, category);
        List<ViewsSummary> findViews = response.getData();
        // then
        assertNotNull(response);
        assertNull(findViews);
        assertEquals(ErrorCode.INVALID_VIEWS_MONTH.getStatus(), response.getStatus());
        assertEquals(ErrorCode.INVALID_VIEWS_MONTH.getMessage(), response.getMessage());
        Mockito.verify(viewsRepository, never()).findByYearAndMonthBetweenAndMenuAndCategory(any(Integer.class), any(Integer.class), any(Integer.class), any(Integer.class), any(MenuTitle.class), any(ArtworkCategory.class));
    }

    @Test
    @DisplayName("기간(시작점(연도,월), 종료점(연도,월))으로 카테고리별, 메뉴별 전체 조회수 조회 실패 테스트 - 종료점이 시작점보다 앞에 있는 경우")
    void retrieveAllMenuCategoryViewsByPeriodFail_invalidPeriod() {
        // given
        Integer startYear = 2024;
        Integer startMonth = 11;
        Integer endYear = 2024;
        Integer endMonth = 9;
        MenuTitle menu = MenuTitle.ABOUT;
        ArtworkCategory category = ArtworkCategory.ALL;
        // when
        ApiResponse<List<ViewsSummary>> response = viewsService.retrieveAllMenuCategoryViewsByPeriod(startYear, startMonth, endYear, endMonth, menu, category);
        List<ViewsSummary> findViews = response.getData();
        // then
        assertNotNull(response);
        assertNull(findViews);
        assertEquals(ErrorCode.INVALID_PERIOD_FORMAT.getStatus(), response.getStatus());
        assertEquals(ErrorCode.INVALID_PERIOD_FORMAT.getMessage(), response.getMessage());
        Mockito.verify(viewsRepository, never()).findByYearAndMonthBetweenAndMenuAndCategory(any(Integer.class), any(Integer.class), any(Integer.class), any(Integer.class), any(MenuTitle.class), any(ArtworkCategory.class));
    }

    @Test
    @DisplayName("이번 월 조회수 1 상승 성공 테스트 - 이번 월 조회수 데이터가 없는 경우")
    void updateThisMonthViewsSuccess_viewsNotExisted() {
        // given
        MenuTitle menu = MenuTitle.ABOUT;
        ArtworkCategory category = ArtworkCategory.ALL;
        UpdateViewsServiceRequestDto requestDto = new UpdateViewsServiceRequestDto(menu, category);
        // when
        ApiResponse<Views> response = viewsService.updateThisMonthViews(requestDto);
        Views findViews = response.getData();
        // then
        assertNotNull(response);
        assertNull(findViews);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertTrue(
                response.getMessage().equals("조회수를 성공적으로 수정했습니다.") ||
                        response.getMessage().equals("조회 수 등록을 완료했습니다.")
        );
        Mockito.verify(viewsRepository, times(1)).save(any(Views.class));
    }

    @Test
    @DisplayName("이번 월 조회수 1 상승 성공 테스트 - 이번 월 조회수 데이터가 이미 존재하는 경우")
    void updateThisMonthViewsSuccess_alreadyViewsExisted() {
        // given
        MenuTitle menu = MenuTitle.ABOUT;
        ArtworkCategory category = ArtworkCategory.ALL;
        UpdateViewsServiceRequestDto requestDto = new UpdateViewsServiceRequestDto(menu, category);
        Views mockViews = Views.builder()
                .year(2024)
                .month(11)
                .views(1L)
                .menu(MenuTitle.ABOUT)
                .category(category)
                .createdAt(new Date())
                .build();
        // stub
        when(viewsRepository.findByYearAndMonthAndMenuAndCategory(Integer.parseInt(
                new SimpleDateFormat("yyyy").format(new Date().getTime())),
                Integer.parseInt(new SimpleDateFormat("MM").format(new Date().getTime())),
                requestDto.menu(), requestDto.category())).thenReturn(Optional.of(mockViews));
        // when
        ApiResponse<Views> response = viewsService.updateThisMonthViews(requestDto);
        Views findViews = response.getData();
        // then
        assertNotNull(response);
        assertNull(findViews);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertTrue(
                response.getMessage().equals("조회수를 성공적으로 수정했습니다.") ||
                        response.getMessage().equals("조회 수 등록을 완료했습니다.")
        );
        Mockito.verify(viewsRepository, times(1)).save(any(Views.class));
    }

    @Test
    @DisplayName("이번 월 조회수 1 상승 실패 테스트 - 잘못된 메뉴와 카테고리 조합인 경우")
    void updateThisMonthViewsFail_invalidMenuAndCategory() {
        // given
        MenuTitle menu = MenuTitle.ABOUT;
        ArtworkCategory category = ArtworkCategory.ENTERTAINMENT;
        UpdateViewsServiceRequestDto requestDto = new UpdateViewsServiceRequestDto(menu, category);
        // when
        ApiResponse<Views> response = viewsService.updateThisMonthViews(requestDto);
        Views findViews = response.getData();
        // then
        assertNotNull(response);
        assertNull(findViews);
        assertEquals(ErrorCode.INVALID_VIEWS_CATEGORY.getStatus(), response.getStatus());
        assertEquals(ErrorCode.INVALID_VIEWS_CATEGORY.getMessage(), response.getMessage());
        Mockito.verify(viewsRepository, never()).save(any(Views.class));
    }
}
