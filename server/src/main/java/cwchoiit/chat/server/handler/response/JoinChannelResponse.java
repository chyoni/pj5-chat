package cwchoiit.chat.server.handler.response;

import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.server.constants.MessageType.ENTER_CHANNEL_RESPONSE;
import static cwchoiit.chat.server.constants.MessageType.JOIN_CHANNEL_RESPONSE;

@Getter
@ToString
public class JoinChannelResponse extends BaseResponse {
    private final Long channelId;
    private final String title;

    public JoinChannelResponse(Long channelId, String title) {
        super(JOIN_CHANNEL_RESPONSE);
        this.channelId = channelId;
        this.title = title;
    }
}
