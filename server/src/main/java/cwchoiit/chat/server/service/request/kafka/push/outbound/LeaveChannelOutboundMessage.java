package cwchoiit.chat.server.service.request.kafka.push.outbound;


import static cwchoiit.chat.server.constants.MessageType.LEAVE_CHANNEL_RESPONSE;

public record LeaveChannelOutboundMessage(Long userId) implements BaseRecord {
    @Override
    public String type() {
        return LEAVE_CHANNEL_RESPONSE;
    }
}
