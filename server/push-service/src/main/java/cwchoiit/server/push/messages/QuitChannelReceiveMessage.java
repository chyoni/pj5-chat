package cwchoiit.server.push.messages;

import static cwchoiit.server.push.constants.MessageType.QUIT_CHANNEL_RESPONSE;

public record QuitChannelReceiveMessage(Long userId, Long channelId) implements BaseRecord {
    @Override
    public String type() {
        return QUIT_CHANNEL_RESPONSE;
    }
}
