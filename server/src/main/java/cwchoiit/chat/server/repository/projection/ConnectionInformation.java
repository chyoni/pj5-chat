package cwchoiit.chat.server.repository.projection;

public interface ConnectionInformation {
    Long getUserId();

    String getUsername();

    Long getInviterUserId();
}
