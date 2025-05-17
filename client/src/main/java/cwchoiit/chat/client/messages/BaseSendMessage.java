package cwchoiit.chat.client.messages;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class BaseSendMessage {
    private final String type;
}
