package cwchoiit.server.push.messages;

import static cwchoiit.server.push.constants.MessageType.NOTIFY_CHANNEL_JOIN;

public record ChannelJoinNotificationReceiveMessage(Long userId, Long channelId, String title) implements BaseRecord {
    @Override
    public String type() {
        return NOTIFY_CHANNEL_JOIN;
    }
}
