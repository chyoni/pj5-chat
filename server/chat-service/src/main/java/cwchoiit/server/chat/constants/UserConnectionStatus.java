package cwchoiit.server.chat.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserConnectionStatus {
    NONE,
    PENDING,
    ACCEPTED,
    REJECTED,
    DISCONNECTED
}
