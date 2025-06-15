package cwchoiit.chat.server.service.request.kafka.push.outbound;


import static cwchoiit.chat.server.constants.MessageType.NOTIFY_CHANNEL_JOIN;

public record ChannelJoinNotificationOutboundMessage(Long userId, Long channelId, String title) implements BaseRecord {
    @Override
    public String type() {
        return NOTIFY_CHANNEL_JOIN;
    }
}
