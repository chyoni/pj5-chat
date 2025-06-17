package cwchoiit.server.chat.service.request.kafka.push.outbound;


import cwchoiit.server.chat.constants.UserConnectionStatus;

import static cwchoiit.server.chat.constants.MessageType.DISCONNECT_RESPONSE;

public record DisconnectOutboundMessage(Long userId,
                                        String username,
                                        UserConnectionStatus status) implements BaseRecord {
    @Override
    public String type() {
        return DISCONNECT_RESPONSE;
    }
}
