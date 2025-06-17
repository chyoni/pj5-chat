package cwchoiit.server.chat.service.request.kafka.push.outbound;

import static cwchoiit.server.chat.constants.MessageType.MESSAGE;


public record ChatMessageOutboundMessage(Long userId, String username, String content) implements BaseRecord {
    @Override
    public String type() {
        return MESSAGE;
    }
}
