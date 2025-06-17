package cwchoiit.server.chat.service.request.kafka.push.outbound;


import static cwchoiit.server.chat.constants.MessageType.CHANNEL_CREATE_RESPONSE;

public record CreateChannelOutboundMessage(Long userId, Long channelId, String title) implements BaseRecord {
    @Override
    public String type() {
        return CHANNEL_CREATE_RESPONSE;
    }
}
