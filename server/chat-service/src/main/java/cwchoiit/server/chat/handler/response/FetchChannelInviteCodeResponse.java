package cwchoiit.server.chat.handler.response;

import lombok.Getter;
import lombok.ToString;

import static cwchoiit.server.chat.constants.MessageType.FETCH_CHANNEL_INVITE_CODE_RESPONSE;

@Getter
@ToString
public class FetchChannelInviteCodeResponse extends BaseResponse {
    private final Long channelId;
    private final String inviteCode;

    public FetchChannelInviteCodeResponse(Long channelId, String inviteCode) {
        super(FETCH_CHANNEL_INVITE_CODE_RESPONSE);
        this.channelId = channelId;
        this.inviteCode = inviteCode;
    }
}
