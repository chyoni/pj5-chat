package cwchoiit.chat.server.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IdKey {
    HTTP_SESSION_ID("HTTP_SESSION_ID"),
    USER_ID("USER_ID");

    private final String value;
}
