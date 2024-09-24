package studio.studioeye.domain.client.dto.request;

public record UpdateClientServiceRequestDto(
    Long clientId,
    String name,
    Boolean visibility
) {
}
