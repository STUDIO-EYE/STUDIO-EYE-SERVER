package studio.studioeye.domain.request.dto.request;

import studio.studioeye.domain.request.domain.State;

public record UpdateRequestCommentServiceDto(
        String answer,
        State state
) {
}
