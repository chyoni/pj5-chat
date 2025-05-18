package cwchoiit.chat.server.handler.response;

import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.server.constants.MessageType.ENTER_CHANNEL_RESPONSE;

@Getter
@ToString
public class EnterChannelResponse extends BaseResponse {
    private final Long channelId;
    private final String title;

    public EnterChannelResponse(Long channelId, String title) {
        super(ENTER_CHANNEL_RESPONSE);
        this.channelId = channelId;
        this.title = title;
    }
}
