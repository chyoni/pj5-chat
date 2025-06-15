package cwchoiit.chat.server.service.request.kafka.push.outbound;

import static cwchoiit.chat.server.constants.MessageType.MESSAGE;


public record ChatMessageOutboundMessage(Long userId, String username, String content) implements BaseRecord {
    @Override
    public String type() {
        return MESSAGE;
    }
}
