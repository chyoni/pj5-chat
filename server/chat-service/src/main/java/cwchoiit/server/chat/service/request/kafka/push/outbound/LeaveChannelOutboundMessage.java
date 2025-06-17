package cwchoiit.server.chat.service.request.kafka.push.outbound;


import static cwchoiit.server.chat.constants.MessageType.LEAVE_CHANNEL_RESPONSE;

public record LeaveChannelOutboundMessage(Long userId) implements BaseRecord {
    @Override
    public String type() {
        return LEAVE_CHANNEL_RESPONSE;
    }
}
