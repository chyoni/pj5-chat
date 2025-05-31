package cwchoiit.chat.server.handler.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import static cwchoiit.chat.server.constants.MessageType.FETCH_CHANNEL_INVITE_CODE_REQUEST;

@Getter
public class FetchChannelInviteCodeRequest extends BaseRequest {

    private final Long channelId;

    @JsonCreator
    public FetchChannelInviteCodeRequest(@JsonProperty("channelId") Long channelId) {
        super(FETCH_CHANNEL_INVITE_CODE_REQUEST);
        this.channelId = channelId;
    }
}
