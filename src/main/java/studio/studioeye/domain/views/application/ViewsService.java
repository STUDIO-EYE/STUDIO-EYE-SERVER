package studio.studioeye.domain.views.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studio.studioeye.domain.views.dao.ViewsRepository;
import studio.studioeye.domain.views.domain.Views;
import studio.studioeye.domain.views.dto.request.CreateViewsServiceDto;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

@Service
@Transactional
@RequiredArgsConstructor
public class ViewsService {

    private final ViewsRepository viewsRepository;
    // for initial views data / for adding views
    private static final Long num1 = 1L;

    public ApiResponse<Views> createViews(CreateViewsServiceDto dto) {
        if(!checkMonth(dto.month())) return ApiResponse.withError(ErrorCode.INVALID_VIEWS_MONTH);
        Optional<Views> optionalViews = viewsRepository.findByYearAndMonth(dto.year(), dto.month());
        if(optionalViews.isPresent()) {
            return ApiResponse.withError(ErrorCode.ALREADY_EXISTED_DATA);
        }
        return this.justCreateViews(dto);
    }

    private ApiResponse<Views> justCreateViews(CreateViewsServiceDto dto) {
        if(!checkMonth(dto.month())) return ApiResponse.withError(ErrorCode.INVALID_VIEWS_MONTH);
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        Views views = dto.toEntity(new Date());
        Views savedViews = viewsRepository.save(views);
        return ApiResponse.ok("조회 수 등록을 완료했습니다.", savedViews);
    }

    public ApiResponse<List<Views>> retrieveAllViews() {
        List<Views> viewsList = viewsRepository.findAll();
        if(viewsList.isEmpty()) {
            return ApiResponse.ok("조회수가 존재하지 않습니다.");
        }
        return ApiResponse.ok("조회수 목록을 성공적으로 조회했습니다.", viewsList);
    }

    public ApiResponse<List<Views>> retrieveViewsByPeriod(Integer startYear, Integer startMonth, Integer endYear, Integer endMonth) {
        // 월 형식 검사
        if(!checkMonth(startMonth) || !checkMonth(endMonth)) return ApiResponse.withError(ErrorCode.INVALID_VIEWS_MONTH);
        // 종료점이 시작점보다 앞에 있을 경우 제한 걸기
        if(startYear > endYear || (startYear.equals(endYear) && startMonth > endMonth)) {
            return ApiResponse.withError(ErrorCode.INVALID_PERIOD_FORMAT);
        }
        // 2~12달로 제한 걸기
        Integer months = (endYear - startYear) * 12 + (endMonth - startMonth) + 1;
        if(months < 2 || months > 12) {
            return ApiResponse.withError(ErrorCode.INVALID_VIEWS_PERIOD);
        }
        List<Views> viewsList = viewsRepository.findByYearAndMonthBetween(startYear, startMonth, endYear, endMonth);

        for (int year = startYear; year <= endYear; year++) {
            int monthStart = (year == startYear) ? startMonth : 1;
            int monthEnd = (year == endYear) ? endMonth : 12;

            for (int month = monthStart; month <= monthEnd; month++) {
                boolean found = false;

                // 현재 조회할 연도와 월에 해당하는 인덱스 찾기
                int index = 0;
                for (Views view : viewsList) {
                    // 이미 해당 연도와 월에 대한 데이터가 존재하는 경우
                    if (view.getYear() == year && view.getMonth() == month) {
                        found = true;
                        break;
                    }
                    // 현재 연도보다 작은 경우 삽입 위치 찾기
                    else if (view.getYear() < year || (view.getYear() == year && view.getMonth() < month)) {
                        // 삽입 위치 계산
                        index++;
                    }
                }

                // 해당 연도와 월에 대한 데이터가 존재하지 않는 경우, 0으로 데이터 추가
                if (!found) {
                    // 데이터를 삽입한 후에는 인덱스를 증가시킴
                    viewsList.add(index, new Views(year, month, 0L, new Date()));
                }
            }
        }
        return ApiResponse.ok("조회수 목록을 성공적으로 조회했습니다.", viewsList);
    }

    public ApiResponse<List<Views>> retrieveViewsByYear(Integer year) {
        List<Views> viewsList = viewsRepository.findByYear(year);
        if(viewsList.isEmpty()) {
            return ApiResponse.ok("조회수가 존재하지 않습니다.");
        }
        return ApiResponse.ok("조회수 목록을 성공적으로 조회했습니다.", viewsList);
    }

    public ApiResponse<Views> retrieveViewsByYearMonth(Integer year, Integer month) {
        Optional<Views> optionalViews = viewsRepository.findByYearAndMonth(year, month);
        if(optionalViews.isEmpty()){
            return ApiResponse.ok("조회수가 존재하지 않습니다.");
        }
        Views views = optionalViews.get();
        return ApiResponse.ok("조회수를 성공적으로 조회했습니다.", views);
    }

    public ApiResponse<Views> retrieveViewsById(Long viewsId) {
        Optional<Views> optionalViews = viewsRepository.findById(viewsId);
        if(optionalViews.isEmpty()){
            return ApiResponse.ok("조회수가 존재하지 않습니다.");
        }
        Views views = optionalViews.get();
        return ApiResponse.ok("조회수를 성공적으로 조회했습니다.", views);
    }

    public ApiResponse<Views> updateViewsById(Long viewsId) {
        Optional<Views> optionalViews = viewsRepository.findById(viewsId);
        if(optionalViews.isEmpty()){
            return ApiResponse.withError(ErrorCode.INVALID_VIEWS_ID);
        }
        Views views = optionalViews.get();
        views.updateViews(views.getViews()+num1);
        Views updatedViews = viewsRepository.save(views);
        return ApiResponse.ok("조회수를 성공적으로 수정했습니다.", updatedViews);
    }

    public ApiResponse<Views> updateViewsByYearMonth(Integer year, Integer month) {
        Optional<Views> optionalViews = viewsRepository.findByYearAndMonth(year, month);
        if(optionalViews.isEmpty()){
            // 생성 코드 필요?
            return this.justCreateViews(new CreateViewsServiceDto(year, month, num1));
        }
        Views views = optionalViews.get();
        views.updateViews(views.getViews()+num1);
        Views updatedViews = viewsRepository.save(views);
        return ApiResponse.ok("조회수를 성공적으로 수정했습니다.", updatedViews);
    }

    public ApiResponse<Views> updateThisMonthViews(String cookieValue) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));

        if(cookieValue != null) {
            return ApiResponse.ok("이미 방문한 사용자입니다.");
        }

        return this.updateViewsByYearMonth(
                Integer.parseInt(new SimpleDateFormat("yyyy").format(new Date().getTime())),
                Integer.parseInt(new SimpleDateFormat("MM").format(new Date().getTime()))
        );
    }

    private boolean checkMonth(int month) {
        // 월 형식 검사
        return month >= 1 && month <= 12;
    }

}
