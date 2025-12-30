package modelly.modelly_be.domain.chat.event;

import modelly.modelly_be.domain.chat.dto.response.SendMessageResponse;

public record ChatBroadcastEvent(
        Long roomId,
        SendMessageResponse response
) {}
