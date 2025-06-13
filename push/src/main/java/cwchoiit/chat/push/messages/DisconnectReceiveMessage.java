package cwchoiit.chat.push.messages;

import cwchoiit.chat.push.constants.UserConnectionStatus;

import static cwchoiit.chat.push.constants.MessageType.DISCONNECT_RESPONSE;


public record DisconnectReceiveMessage(Long userId,
                                       String username,
                                       UserConnectionStatus status) implements BaseRecord {
    @Override
    public String type() {
        return DISCONNECT_RESPONSE;
    }
}
