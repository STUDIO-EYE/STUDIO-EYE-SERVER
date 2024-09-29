package studio.studioeye.domain.client.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import studio.studioeye.domain.client.application.ClientService;
import studio.studioeye.domain.client.domain.Client;
import studio.studioeye.domain.client.dto.request.CreateClientRequestDto;
import studio.studioeye.domain.client.dto.request.UpdateClientRequestDto;
import studio.studioeye.global.common.response.ApiResponse;

import java.util.List;
import java.util.Map;

@Tag(name = "클라이언트 API", description = "클라이언트 등록 / 수정 / 삭제 / 조회")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @Operation(summary = "클라이언트 등록 API")
    @PostMapping("/client")
    public ApiResponse<Client> createClient(@Valid @RequestPart("clientInfo") CreateClientRequestDto dto,
                                            @RequestPart(value = "logoImg", required = false) MultipartFile logoImg){
        return clientService.createClient(dto.toServiceRequest(), logoImg);
    }

    @Operation(summary = "클라이언트 전체 조회 API")
    @GetMapping("/client")
    public ApiResponse<List<Map<String, Object>>> retrieveAllClient(){
        return clientService.retrieveAllClient();
    }

    @Operation(summary = "클라이언트 상세 조회 API")
    @GetMapping("/client/{clientId}")
    public ApiResponse<Map<String, Object>> retrieveClient(@PathVariable Long clientId){
        return clientService.retrieveClient(clientId);
    }

    @Operation(summary = "클라이언트 로고 이미지 리스트 조회 API")
    @GetMapping("/client/logoImgList")
    public ApiResponse<List<String>> retrieveAllClientLogoImgList(){
        return clientService.retrieveAllClientLogoImgList();
    }

    @Operation(summary = "클라이언트 페이지네이션 조회 API")
    @GetMapping("/client/page")
    public Page<Client> retrieveClientPage(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size){
        return clientService.retrieveClientPage(page, size);
    }

    @Operation(summary = "클라이언트 수정 API")
    @PutMapping("/client")
    public ApiResponse<Client> updateClient(@Valid @RequestPart("clientInfo") UpdateClientRequestDto dto,
                                    @RequestPart(value = "logoImg", required = false) MultipartFile logoImg){
        return clientService.updateClient(dto.toServiceRequest(), logoImg);
    }
    @Operation(summary = "클라이언트 텍스트(이미지 제외) 수정 API")
    @PutMapping("/client/modify")
    public ApiResponse<Client> updateClientText(@Valid @RequestPart("clientInfo") UpdateClientRequestDto dto){
        return clientService.updateClientText(dto.toServiceRequest());
    }

    @Operation(summary = "클라이언트 로고 이미지 수정 API")
    @PutMapping("/client/{clientId}/logoImg")
    public ApiResponse<Client> updateClientLogoImg(@PathVariable Long clientId,
                                    @RequestPart(value = "logoImg", required = false) MultipartFile logoImg){
        return clientService.updateClientLogoImg(clientId, logoImg);
    }

    @Operation(summary = "클라이언트 삭제 API")
    @DeleteMapping("/client/{clientId}")
    public ApiResponse<String> deleteClient(@PathVariable Long clientId){
        return clientService.deleteClient(clientId);
    }
}
