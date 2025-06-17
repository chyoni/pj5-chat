package cwchoiit.server.chat.handler.response;

import lombok.Getter;
import lombok.ToString;

import static cwchoiit.server.chat.constants.MessageType.QUIT_CHANNEL_RESPONSE;

@Getter
@ToString
public class QuitChannelResponse extends BaseResponse {
    private final Long channelId;

    public QuitChannelResponse(Long channelId) {
        super(QUIT_CHANNEL_RESPONSE);
        this.channelId = channelId;
    }
}
