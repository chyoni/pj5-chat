package cwchoiit.server.chat.handler.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import static cwchoiit.server.chat.constants.MessageType.FETCH_USER_INVITE_CODE_RESPONSE;

@Getter
public class FetchUserInviteCodeResponse extends BaseResponse {

    private final String inviteCode;

    @JsonCreator
    public FetchUserInviteCodeResponse(String inviteCode) {
        super(FETCH_USER_INVITE_CODE_RESPONSE);
        this.inviteCode = inviteCode;
    }
}
