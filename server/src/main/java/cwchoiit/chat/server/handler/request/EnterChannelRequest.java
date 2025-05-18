package cwchoiit.chat.server.handler.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import static cwchoiit.chat.server.constants.MessageType.ENTER_CHANNEL_REQUEST;

@Getter
public class EnterChannelRequest extends BaseRequest {

    private final Long channelId;

    @JsonCreator
    public EnterChannelRequest(@JsonProperty("channelId") Long channelId) {
        super(ENTER_CHANNEL_REQUEST);
        this.channelId = channelId;
    }
}
