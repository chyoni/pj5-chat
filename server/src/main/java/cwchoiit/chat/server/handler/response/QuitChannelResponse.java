package cwchoiit.chat.server.handler.response;

import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.server.constants.MessageType.QUIT_CHANNEL_RESPONSE;

@Getter
@ToString
public class QuitChannelResponse extends BaseResponse {
    private final Long channelId;

    public QuitChannelResponse(Long channelId) {
        super(QUIT_CHANNEL_RESPONSE);
        this.channelId = channelId;
    }
}
