package studio.studioeye.domain.request.application;

import studio.studioeye.domain.email.service.EmailService;
import studio.studioeye.domain.notification.application.NotificationService;
import studio.studioeye.domain.request.dao.*;
import studio.studioeye.domain.request.domain.Answer;
import studio.studioeye.domain.request.domain.Request;
import studio.studioeye.domain.request.domain.State;
import studio.studioeye.domain.request.dto.request.CreateRequestServiceDto;
import studio.studioeye.domain.request.dto.request.UpdateRequestCommentServiceDto;
import studio.studioeye.domain.request.dto.request.UpdateRequestStateServiceDto;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import studio.studioeye.infrastructure.s3.S3Adapter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
@RequiredArgsConstructor
public class RequestService {

	private final RequestRepository requestRepository;
	private final AnswerRepository answerRepository;
	private final S3Adapter s3Adapter;

	private final NotificationService notificationService;
	private final EmailService emailService;

	private static final String EMAIL_REGEX =
			"^[a-zA-Z0-9_+&*-]+(?:\\." +
					"[a-zA-Z0-9_+&*-]+)*@" +
					"(?:[a-zA-Z0-9-]+\\.)+[a-z" +
					"A-Z]{2,7}$";

//	private final Integer waitingState = 0;
//	private final Integer approvedState = 1;
//	private final Integer rejectedState = 2;
//	private final Integer completedState = 3;


//	private String convertState(Integer state) {
//		if(state == this.approvedState) {
//			return "승인";
//		}
//		if(state == this.completedState) {
//			return "처리 완료";
//		}
//		if(state == this.rejectedState) {
//			return "거절";
//		}
//		if(state == this.waitingState) {
//			return "대기중";
//		}
//		return "해당사항 없음";
//	}
	public ApiResponse<Request> createRequest(CreateRequestServiceDto dto, List<MultipartFile> files) throws IOException {
		if(!isValidEmail(dto.email())) {
			return ApiResponse.withError(ErrorCode.INVALID_EMAIL_FORMAT);
		}
		List<String> fileUrlList = new LinkedList<>();
		if (files != null) {
			for (var file : files) {
				ApiResponse<String> updateFileResponse = s3Adapter.uploadFile(file);

				if (updateFileResponse.getStatus().is5xxServerError()) {
					return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);
				}
				String fileUrl = updateFileResponse.getData();
				fileUrlList.add(fileUrl);
			}
		}

		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		Integer year = Integer.parseInt(new SimpleDateFormat("yyyy").format(new Date().getTime()));
		Integer month = Integer.parseInt(new SimpleDateFormat("MM").format(new Date().getTime()));

		Request request = dto.toEntity(fileUrlList, new ArrayList<>(), year, month, State.WAITING, new Date());
		Request savedRequest = requestRepository.saveAndFlush(request);

//		String state = convertState(savedRequest.getState());

		String subject = "[STUDIO EYE] 문의가 완료되었습니다."; // 이메일 제목
		String text = "아래와 같이 작성하신 문의가 성공적으로 접수되었습니다.\n"
				+ "추후 담당자 배정 후 해당 메일로 결과를 전송드리겠습니다.\n\n\n"

				+ "의뢰인 성명: " + savedRequest.getClientName() + "\n"
				+ "기관 혹은 기업: " + savedRequest.getOrganization() + "\n"
//				+ "이메일 주소: " + savedRequest.getEmail() + "\n"
				+ "직책: " + savedRequest.getPosition() + "\n"
				+ "연락처: " + savedRequest.getContact() + "\n\n"

				+ "카테고리: " + savedRequest.getCategory() + "\n"
				+ "프로젝트명: " + savedRequest.getProjectName() + "\n"
				+ "문의 내용: " + savedRequest.getDescription() + "\n";

		emailService.sendEmail(savedRequest.getEmail(), subject, text);
		boolean isExceeded = emailService.sendEmail(savedRequest.getEmail(), subject, text);
		if(!isExceeded) {
			return ApiResponse.withError(ErrorCode.EMAIL_SIZE_EXCEEDED);
		}
		notificationService.subscribe(request.getId());    // 문의 등록 알림 보내기
		return ApiResponse.ok("문의를 성공적으로 등록하였습니다.", savedRequest);
	}

	public ApiResponse<List<Request>> retrieveAllRequest() {
		List<Request> requestList = requestRepository.findAll();

		if (requestList.isEmpty()){
			return ApiResponse.ok("문의가 존재하지 않습니다.");
		}
		return ApiResponse.ok("문의 목록을 성공적으로 조회했습니다.", requestList);
	}

	public ApiResponse<Request> retrieveRequest(Long requestId) {
		Optional<Request> optionalRequest = requestRepository.findById(requestId);
		if(optionalRequest.isEmpty()){
			return ApiResponse.withError(ErrorCode.INVALID_REQUEST_ID);
		}

		Request request = optionalRequest.get();
		return ApiResponse.ok("문의를 성공적으로 조회했습니다.", request);
	}

	public ApiResponse<Long> retrieveRequestCount() {
		Long requestCount = requestRepository.count();
		return ApiResponse.ok("전체 문의수를 성공적으로 조회했습니다.", requestCount);
	}

	public ApiResponse<List<RequestCount>> retrieveRequestCountByPeriod(Integer startYear, Integer startMonth, Integer endYear, Integer endMonth) {
		// 월 형식 검사
		if(!checkMonth(startMonth) || !checkMonth(endMonth)) return ApiResponse.withError(ErrorCode.INVALID_REQUEST_MONTH);
		// 종료점이 시작점보다 앞에 있을 경우 제한 걸기
		if(startYear > endYear || (startYear.equals(endYear) && startMonth > endMonth)) {
			return ApiResponse.withError(ErrorCode.INVALID_PERIOD_FORMAT);
		}
		// 2~12달로 제한 걸기
		Integer months = (endYear - startYear) * 12 + (endMonth - startMonth) + 1;
		if(months < 2 || months > 12) {
			return ApiResponse.withError(ErrorCode.INVALID_REQUEST_PERIOD);
		}
		List<RequestCount> requestCountList = requestRepository.findByYearAndMonthBetween(startYear, startMonth, endYear, endMonth);
		for (int year = startYear; year <= endYear; year++) {
			int monthStart = (year == startYear) ? startMonth : 1;
			int monthEnd = (year == endYear) ? endMonth : 12;

			for (int month = monthStart; month <= monthEnd; month++) {
				boolean found = false;

				// 현재 조회할 연도와 월에 해당하는 인덱스 찾기
				int index = 0;
				for (RequestCount requestCount : requestCountList) {
					// 이미 해당 연도와 월에 대한 데이터가 존재하는 경우
					if (requestCount.getYear() == year && requestCount.getMonth() == month) {
						found = true;
						break;
					}
					// 현재 연도보다 작은 경우 삽입 위치 찾기
					else if (requestCount.getYear() < year || (requestCount.getYear() == year && requestCount.getMonth() < month)) {
						// 삽입 위치 계산
						index++;
					}
				}

				// 해당 연도와 월에 대한 데이터가 존재하지 않는 경우, 0으로 데이터 추가
				if (!found) {
					// 데이터를 삽입한 후에는 인덱스를 증가시킴
					requestCountList.add(index, new RequestCountImpl(year, month, 0L));
				}
			}
		}
		return ApiResponse.ok("문의수 목록을 성공적으로 조회했습니다.", requestCountList);
	}

	public ApiResponse<List<Map<String, Object>>> retrieveCategoryRequestCountByPeriod(Integer startYear, Integer startMonth, Integer endYear, Integer endMonth) {
		// 월 형식 검사
		if(!checkMonth(startMonth) || !checkMonth(endMonth)) return ApiResponse.withError(ErrorCode.INVALID_REQUEST_MONTH);
		// 종료점이 시작점보다 앞에 있을 경우 제한 걸기
		if(startYear > endYear || (startYear.equals(endYear) && startMonth > endMonth)) {
			return ApiResponse.withError(ErrorCode.INVALID_PERIOD_FORMAT);
		}
		// 2~12달로 제한 걸기
		Integer months = (endYear - startYear) * 12 + (endMonth - startMonth) + 1;
		if(months < 2 || months > 12) {
			return ApiResponse.withError(ErrorCode.INVALID_REQUEST_PERIOD);
		}

		List<RequestCategoryCount> requestCountList = requestRepository.findCategoryReqNumByYearAndMonthBetween(startYear, startMonth, endYear, endMonth);
		List<Map<String, Object>> responseList = new ArrayList<>();
		for (int year = startYear; year <= endYear; year++) {
			int monthStart = (year == startYear) ? startMonth : 1;
			int monthEnd = (year == endYear) ? endMonth : 12;

			for (int month = monthStart; month <= monthEnd; month++) {
				// 해당 연도와 월에 대한 categoryRequestCount 초기화.
				Map<String, Long> categoryRequestCount = new HashMap<>();

				for (RequestCategoryCount requestCount : requestCountList) {
					if (requestCount.getYear() == year && requestCount.getMonth() == month) {
						categoryRequestCount.put(requestCount.getCategory(), requestCount.getRequestCount());
					}
				}

				Map<String, Object> responseItem = new HashMap<>();
				responseItem.put("year", year);
				responseItem.put("month", month);
				responseItem.put("categoryRequestCount", categoryRequestCount);
				responseList.add(responseItem);
			}
		}


		return ApiResponse.ok("문의수 목록을 성공적으로 조회했습니다.", responseList);
	}

	public ApiResponse<List<Map<String, Object>>> retrieveStateRequestCountByPeriod(Integer startYear, Integer startMonth, Integer endYear, Integer endMonth) {
		// 월 형식 검사
		if(!checkMonth(startMonth) || !checkMonth(endMonth)) return ApiResponse.withError(ErrorCode.INVALID_REQUEST_MONTH);
		// 종료점이 시작점보다 앞에 있을 경우 제한 걸기
		if(startYear > endYear || (startYear.equals(endYear) && startMonth > endMonth)) {
			return ApiResponse.withError(ErrorCode.INVALID_PERIOD_FORMAT);
		}
		// 2~12달로 제한 걸기
		Integer months = (endYear - startYear) * 12 + (endMonth - startMonth) + 1;
		if(months < 2 || months > 12) {
			return ApiResponse.withError(ErrorCode.INVALID_REQUEST_PERIOD);
		}

		List<RequestStateCount> requestCountList =
				requestRepository.findStateReqNumByYearAndMonthBetween(startYear, startMonth, endYear, endMonth);

		if (requestCountList.isEmpty()) {
			System.out.println("No data found for the given period.");
		} else {
			requestCountList.forEach(count -> {
				System.out.printf("Year: %d, Month: %d, State: %s, Count: %d%n",
						count.getYear(), count.getMonth(), count.getState(), count.getRequestCount());
			});
		} //logging

		List<Map<String, Object>> responseList = new ArrayList<>();
		for (int year = startYear; year <= endYear; year++) {
			int monthStart = (year == startYear) ? startMonth : 1;
			int monthEnd = (year == endYear) ? endMonth : 12;

			for (int month = monthStart; month <= monthEnd; month++) {
				// 해당 연도와 월에 대한 stateRequestCount 초기화.
				Map<String, Long> stateRequestCount = new HashMap<>();

				for (RequestStateCount requestCount : requestCountList) {
					if (requestCount.getYear() == year && requestCount.getMonth() == month) {
						stateRequestCount.put(requestCount.getState(), requestCount.getRequestCount());
					}
				}

				Map<String, Object> responseItem = new HashMap<>();
				responseItem.put("year", year);
				responseItem.put("month", month);
				responseItem.put("stateRequestCount", stateRequestCount);
				responseList.add(responseItem);
			}
		}


		return ApiResponse.ok("문의수 목록을 성공적으로 조회했습니다.", responseList);
	}
	public ApiResponse<Long> retrieveWaitingRequestCount() {
		Long requestCount = requestRepository.countByState(State.WAITING);
		return ApiResponse.ok("접수 대기 중인 문의 수를 성공적으로 조회했습니다.", requestCount);
	}

	public ApiResponse<List<Request>> retrieveWaitingRequest() {
		List<Request> requestList = requestRepository.findByState(State.WAITING);

		if (requestList.isEmpty()){
			return ApiResponse.ok("접수 대기 중인 문의가 존재하지 않습니다.");
		}
		return ApiResponse.ok("접수 대기 중인 문의 목록을 성공적으로 조회했습니다.", requestList);
	}

	public Page<Request> retrieveRequestPage(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return requestRepository.findAll(pageable);
	}

	public ApiResponse<String> updateRequestState(Long requestId, UpdateRequestStateServiceDto dto) {
		Optional<Request> optionalRequest = requestRepository.findById(requestId);
		if(optionalRequest.isEmpty()){
			return ApiResponse.withError(ErrorCode.INVALID_REQUEST_ID);
		}
		Request request = optionalRequest.get();
		request.updateState(dto.state());
		requestRepository.save(request);
//		Request updatedRequest = requestRepository.save(request);

//		if(dto.state() != null) {
//			String subject = "[studio-eye] [" + convertState(dto.state()) + "]" + updatedRequest.getClientName() + "님의 문의에 의뢰 상태가 " + convertState(dto.state()) + "으로 변경되었습니다."; // 이메일 제목
//			String text = "카테고리: " + updatedRequest.getCategory() + "\n"
//					+ "프로젝트명: " + updatedRequest.getProjectName() + "\n"
//					+ "의뢰인 이름: " + updatedRequest.getClientName() + "\n"
//					+ "기관 혹은 기업: " + updatedRequest.getOrganization() + "\n"
//					+ "연락처: " + updatedRequest.getContact() + "\n"
//					+ "이메일 주소: " + updatedRequest.getEmail() + "\n"
//					+ "직책: " + updatedRequest.getPosition() + "\n"
//					+ "의뢰 내용: " + updatedRequest.getDescription() + "\n\n"
//					+ "답변: " + updatedRequest.getAnswer() + "\n"
//					+ "의뢰 상태: " + convertState(dto.state());
//
//			emailService.sendEmail(updatedRequest.getEmail(), subject, text);
//		}

		return ApiResponse.ok("상태를 성공적으로 수정했습니다.");
	}

	public ApiResponse<String> updateRequestComment(Long requestId, UpdateRequestCommentServiceDto dto) {
		String answer = dto.answer().trim();
		State state = dto.state();

		Optional<Request> optionalRequest = requestRepository.findById(requestId);
		if(optionalRequest.isEmpty()){
			return ApiResponse.withError(ErrorCode.INVALID_REQUEST_ID);
		}
		Request request = optionalRequest.get();
		Answer updatedAnswer = Answer.builder()
				.text(answer)
				.state(state)
				.createdAt(LocalDateTime.now())
				.request(request)
				.build();
		request.getAnswers().add(updatedAnswer);
		request.updateState(state);
		Request updatedRequest = requestRepository.save(request);
		answerRepository.save(updatedAnswer);
//		request.updateAnswer(answer);
//		if(state != null) {
//			request.updateState(state);
//		}
//		Request updatedRequest = requestRepository.save(request);
//
		if(!answer.isEmpty() && state != null) {
			String subject = "[STUDIO EYE] " + updatedRequest.getClientName() + "님의 문의에 답변이 작성되었습니다."; // 이메일 제목
			String text = "[문의 내역]\n\n"

					+ "의뢰인 성명: " + updatedRequest.getClientName() + "\n"
					+ "기관 혹은 기업: " + updatedRequest.getOrganization() + "\n"
					+ "직책: " + updatedRequest.getPosition() + "\n"
					+ "연락처: " + updatedRequest.getContact() + "\n\n"

					+ "카테고리: " + updatedRequest.getCategory() + "\n"
					+ "프로젝트명: " + updatedRequest.getProjectName() + "\n"
					+ "문의 내용: " + updatedRequest.getDescription() + "\n\n\n"


					+ "[답변 내용]" + "\n"
					+ answer + "\n";
			emailService.sendEmail(updatedRequest.getEmail(), subject, text);
		}

		return ApiResponse.ok("답변을 성공적으로 작성했습니다.");
	}

	public ApiResponse<String> deleteRequest(Long requestId) {
		Optional<Request> optionalRequest = requestRepository.findById(requestId);
		if(optionalRequest.isEmpty()){
			return ApiResponse.withError(ErrorCode.INVALID_REQUEST_ID);
		}

		Request request = optionalRequest.get();
		requestRepository.delete(request);

		return ApiResponse.ok("문의를 성공적으로 삭제했습니다.");
	}

	private boolean checkMonth(int month) {
		return (month>=1 && month<=12);
	}

	public static boolean isValidEmail(String email) {
		Pattern pattern = Pattern.compile(EMAIL_REGEX);
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}
}
