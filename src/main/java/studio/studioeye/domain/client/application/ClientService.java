package studio.studioeye.domain.client.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import studio.studioeye.domain.client.dao.ClientRepository;
import studio.studioeye.domain.client.domain.Client;
import studio.studioeye.domain.client.dto.request.CreateClientServiceRequestDto;
import studio.studioeye.domain.client.dto.request.UpdateClientServiceRequestDto;
import studio.studioeye.infrastructure.s3.S3Adapter;
import studio.studioeye.global.common.response.ApiResponse;
import studio.studioeye.global.exception.error.ErrorCode;

import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final S3Adapter s3Adapter;

    public ApiResponse<Client> createClient(CreateClientServiceRequestDto dto, MultipartFile logoImg) {
        String logoImgStr = getImgUrl(logoImg);
        if (logoImgStr.isEmpty()) return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);

        Client client = dto.toEntity(logoImgStr);

        Client savedClient = clientRepository.save(client);
        return ApiResponse.ok("클라이언트를 성공적으로 등록하였습니다.", savedClient);
    }

    public ApiResponse<List<Map<String, Object>>> retrieveAllClient() {
        List<Client> clientList = clientRepository.findAll();
        if (clientList.isEmpty()){
            return ApiResponse.ok("클라이언트가 존재하지 않습니다.");
        }

        List<Map<String, Object>> responseList = new ArrayList<>();
        for (Client client : clientList) {
            Map<String, Object> responseBody = getResponseBody(client);
            responseList.add(responseBody);
        }

        return ApiResponse.ok("클라이언트 목록을 성공적으로 조회했습니다.", responseList);
    }

    public ApiResponse<Map<String, Object>> retrieveClient(Long clientId) {
        Optional<Client> optionalClient = clientRepository.findById(clientId);
        if(optionalClient.isEmpty()){
            return ApiResponse.withError(ErrorCode.INVALID_CLIENT_ID);
        }

        Client client = optionalClient.get();
        Map<String, Object> responseBody = getResponseBody(client);

        return ApiResponse.ok("클라이언트를 성공적으로 조회했습니다.", responseBody);
    }

    public ApiResponse<List<String>> retrieveAllClientLogoImgList() {
        List<Client> clientList = clientRepository.findAll();
        if (clientList.isEmpty()){
            return ApiResponse.ok("클라이언트가 존재하지 않습니다.");
        }

        List<String> logoImgList = new ArrayList<>();
        for (Client client : clientList) {
            logoImgList.add(client.getLogoImg());
        }

        return ApiResponse.ok("클라이언트 로고 이미지 리스트를 성공적으로 조회했습니다.", logoImgList);
    }

    public Page<Client> retrieveClientPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return clientRepository.findAll(pageable);
    }

    private static Map<String, Object> getResponseBody(Client client) {
        // post와 get의 구조 통일
        LinkedHashMap<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("clientInfo", Map.of(
                "id", client.getId(),
                "name", client.getName(),
                "visibility",client.getVisibility()
        ));
        responseBody.put("logoImg", client.getLogoImg());
        return responseBody;
    }

    public ApiResponse<Client> updateClient(UpdateClientServiceRequestDto dto, MultipartFile logoImg) {
        Optional<Client> optionalClient = clientRepository.findById(dto.clientId());
        if(optionalClient.isEmpty()){
            return ApiResponse.withError(ErrorCode.INVALID_CLIENT_ID);
        }

        Client client = optionalClient.get();

        if(!logoImg.isEmpty()) {
            // 새로운 로고이미지 저장
            String logoImgStr = getImgUrl(logoImg);
            if (logoImgStr.isEmpty()) return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);
            client.setLogoImg(logoImgStr);
        }

        Client updatedClient = clientRepository.save(client);
        updatedClient.update(dto);
        return ApiResponse.ok("클라이언트를 성공적으로 수정했습니다.", updatedClient);
    }

    public ApiResponse<Client> updateClientText(UpdateClientServiceRequestDto dto) {
        Optional<Client> optionalClient = clientRepository.findById(dto.clientId());
        if(optionalClient.isEmpty()){
            return ApiResponse.withError(ErrorCode.INVALID_CLIENT_ID);
        }

        Client client = optionalClient.get();
        client.update(dto);
        Client updatedClient = clientRepository.save(client);
        return ApiResponse.ok("클라이언트를 성공적으로 수정했습니다.", updatedClient);
    }

    public ApiResponse<Client> updateClientLogoImg(Long clientId, MultipartFile logoImg) {
        Optional<Client> optionalClient = clientRepository.findById(clientId);
        if (optionalClient.isEmpty()) {
            return ApiResponse.withError(ErrorCode.INVALID_CLIENT_ID);
        }

        Client client = optionalClient.get();

        // 새로운 로고이미지 저장
        String logoImgStr = getImgUrl(logoImg);
        if (logoImgStr.isEmpty()) return ApiResponse.withError(ErrorCode.ERROR_S3_UPDATE_OBJECT);
        client.setLogoImg(logoImgStr);

        Client updatedClient = clientRepository.save(client);
        return ApiResponse.ok("클라이언트 로고 이미지를 성공적으로 수정했습니다.", updatedClient);
    }

    private String getImgUrl(MultipartFile file) {
        ApiResponse<String> updateFileResponse = s3Adapter.uploadImage(file);

        if(updateFileResponse.getStatus().is5xxServerError()){

            return "";
        }
        return updateFileResponse.getData();
    }

    public ApiResponse<String> deleteClient(Long clientId) {
        Optional<Client> optionalClient = clientRepository.findById(clientId);
        if(optionalClient.isEmpty()){
            return ApiResponse.withError(ErrorCode.INVALID_CLIENT_ID);
        }

        Client client = optionalClient.get();
        clientRepository.delete(client);

        return ApiResponse.ok("클라이언트를 성공적으로 삭제했습니다.");
    }
}
