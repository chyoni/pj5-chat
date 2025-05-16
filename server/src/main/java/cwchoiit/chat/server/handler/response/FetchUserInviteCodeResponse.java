package cwchoiit.chat.server.handler.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import cwchoiit.chat.server.handler.request.BaseRequest;
import lombok.Getter;

import static cwchoiit.chat.server.constants.MessageType.FETCH_USER_INVITE_CODE_REQUEST;
import static cwchoiit.chat.server.constants.MessageType.FETCH_USER_INVITE_CODE_RESPONSE;

@Getter
public class FetchUserInviteCodeResponse extends BaseResponse {

    private final String inviteCode;

    @JsonCreator
    public FetchUserInviteCodeResponse(String inviteCode) {
        super(FETCH_USER_INVITE_CODE_RESPONSE);
        this.inviteCode = inviteCode;
    }
}
