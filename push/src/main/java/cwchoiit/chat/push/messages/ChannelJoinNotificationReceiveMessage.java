package cwchoiit.chat.push.messages;

import static cwchoiit.chat.push.constants.MessageType.NOTIFY_CHANNEL_JOIN;

public record ChannelJoinNotificationReceiveMessage(Long userId, Long channelId, String title) implements BaseRecord {
    @Override
    public String type() {
        return NOTIFY_CHANNEL_JOIN;
    }
}
