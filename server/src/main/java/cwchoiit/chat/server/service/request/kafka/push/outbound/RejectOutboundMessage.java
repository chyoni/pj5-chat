package cwchoiit.chat.server.service.request.kafka.push.outbound;


import cwchoiit.chat.server.constants.UserConnectionStatus;

import static cwchoiit.chat.server.constants.MessageType.REJECT_RESPONSE;

public record RejectOutboundMessage(Long userId, String username, UserConnectionStatus status) implements BaseRecord {
    @Override
    public String type() {
        return REJECT_RESPONSE;
    }
}
