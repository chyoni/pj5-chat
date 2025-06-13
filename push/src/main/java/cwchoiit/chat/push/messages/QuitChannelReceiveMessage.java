package cwchoiit.chat.push.messages;

import static cwchoiit.chat.push.constants.MessageType.QUIT_CHANNEL_RESPONSE;

public record QuitChannelReceiveMessage(Long userId, Long channelId) implements BaseRecord {
    @Override
    public String type() {
        return QUIT_CHANNEL_RESPONSE;
    }
}
