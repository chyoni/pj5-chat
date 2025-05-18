package cwchoiit.chat.server.handler.response;

import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.server.constants.MessageType.CHANNEL_CREATE_RESPONSE;

@Getter
@ToString
public class CreateChannelResponse extends BaseResponse {
    private final Long channelId;
    private final String title;

    public CreateChannelResponse(Long channelId, String title) {
        super(CHANNEL_CREATE_RESPONSE);
        this.channelId = channelId;
        this.title = title;
    }
}
