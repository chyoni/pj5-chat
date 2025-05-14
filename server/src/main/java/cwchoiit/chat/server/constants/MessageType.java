package cwchoiit.chat.server.constants;

public interface MessageType {
    // Client <-> Server
    String INVITE_REQUEST = "INVITE_REQUEST";
    String INVITE_RESPONSE = "INVITE_RESPONSE";
    String MESSAGE = "MESSAGE";

    // Notification
    String KEEP_ALIVE = "KEEP_ALIVE";
    String NOTIFY_MESSAGE = "NOTIFY_MESSAGE";
    String ASK_INVITE = "ASK_INVITE";
    String ERROR = "ERROR";
}
