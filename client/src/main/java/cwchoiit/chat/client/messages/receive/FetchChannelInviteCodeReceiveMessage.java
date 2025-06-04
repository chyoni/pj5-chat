package cwchoiit.chat.client.messages.receive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cwchoiit.chat.client.messages.BaseReceiveMessage;
import lombok.Getter;
import lombok.ToString;

import static cwchoiit.chat.client.constants.MessageType.FETCH_CHANNEL_INVITE_CODE_RESPONSE;

@Getter
@ToString
public class FetchChannelInviteCodeReceiveMessage extends BaseReceiveMessage {
    private final Long channelId;
    private final String inviteCode;

    @JsonCreator
    public FetchChannelInviteCodeReceiveMessage(@JsonProperty("channelId") Long channelId,
                                                @JsonProperty("inviteCode") String inviteCode) {
        super(FETCH_CHANNEL_INVITE_CODE_RESPONSE);
        this.channelId = channelId;
        this.inviteCode = inviteCode;
    }
}
