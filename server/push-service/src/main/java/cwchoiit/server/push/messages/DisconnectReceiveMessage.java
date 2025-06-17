package cwchoiit.server.push.messages;

import cwchoiit.server.push.constants.UserConnectionStatus;

import static cwchoiit.server.push.constants.MessageType.DISCONNECT_RESPONSE;


public record DisconnectReceiveMessage(Long userId,
                                       String username,
                                       UserConnectionStatus status) implements BaseRecord {
    @Override
    public String type() {
        return DISCONNECT_RESPONSE;
    }
}
