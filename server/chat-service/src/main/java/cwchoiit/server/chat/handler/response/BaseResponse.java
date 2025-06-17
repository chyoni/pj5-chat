package cwchoiit.server.chat.handler.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class BaseResponse {
    private final String type;
}
