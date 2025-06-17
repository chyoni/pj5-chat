package cwchoiit.server.chat.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChannelResponse {
    SUCCESS("Successfully."),
    FAILED("Failed."),
    NOT_FOUND("Not Found."),
    ALREADY_JOINED("Already Joined."),
    OVER_LIMIT("Over Limit."),
    NOT_JOINED("Not Joined."),
    NOT_ALLOWED("Not Allowed."),
    INVALID_ARGS("Invalid Arguments.");

    private final String message;
}
