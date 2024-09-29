package studio.studioeye.domain.client.dto.request;

import studio.studioeye.domain.client.domain.Client;

public record CreateClientServiceRequestDto(
    String name,
    Boolean visibility
) {
        public Client toEntity(String logoImg) {
            return Client.builder()
                    .name(name)
                    .visibility(visibility)
                    .logoImg(logoImg)
                    .build();
        }
    }
