package cwchoiit.chat.server.service.request.kafka.push.outbound;


import static cwchoiit.chat.server.constants.MessageType.QUIT_CHANNEL_RESPONSE;

public record QuitChannelOutboundMessage(Long userId, Long channelId) implements BaseRecord {
    @Override
    public String type() {
        return QUIT_CHANNEL_RESPONSE;
    }
}
