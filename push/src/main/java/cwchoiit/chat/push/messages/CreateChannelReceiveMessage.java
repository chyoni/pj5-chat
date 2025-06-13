package cwchoiit.chat.push.messages;

import static cwchoiit.chat.push.constants.MessageType.CHANNEL_CREATE_RESPONSE;


public record CreateChannelReceiveMessage(Long userId, Long channelId, String title) implements BaseRecord {
    @Override
    public String type() {
        return CHANNEL_CREATE_RESPONSE;
    }
}
