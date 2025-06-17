package cwchoiit.server.push.messages;

import static cwchoiit.server.push.constants.MessageType.LEAVE_CHANNEL_RESPONSE;


public record LeaveChannelReceiveMessage(Long userId) implements BaseRecord {
    @Override
    public String type() {
        return LEAVE_CHANNEL_RESPONSE;
    }
}
