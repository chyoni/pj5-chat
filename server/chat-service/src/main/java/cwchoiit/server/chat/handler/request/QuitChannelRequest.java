package cwchoiit.server.chat.handler.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import static cwchoiit.server.chat.constants.MessageType.QUIT_CHANNEL_REQUEST;

@Getter
public class QuitChannelRequest extends BaseRequest {

    private final Long channelId;

    @JsonCreator
    public QuitChannelRequest(@JsonProperty("channelId") Long channelId) {
        super(QUIT_CHANNEL_REQUEST);
        this.channelId = channelId;
    }
}
