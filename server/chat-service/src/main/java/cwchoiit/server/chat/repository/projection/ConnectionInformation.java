package cwchoiit.server.chat.repository.projection;

public interface ConnectionInformation {
    Long getUserId();

    String getUsername();

    Long getInviterUserId();
}
