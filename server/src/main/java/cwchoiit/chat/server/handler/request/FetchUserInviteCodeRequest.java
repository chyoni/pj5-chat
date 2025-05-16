package cwchoiit.chat.server.handler.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import static cwchoiit.chat.server.constants.MessageType.FETCH_USER_INVITE_CODE_REQUEST;
import static cwchoiit.chat.server.constants.MessageType.INVITE_REQUEST;

@Getter
public class FetchUserInviteCodeRequest extends BaseRequest {

    @JsonCreator
    public FetchUserInviteCodeRequest() {
        super(FETCH_USER_INVITE_CODE_REQUEST);
    }
}
