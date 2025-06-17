package cwchoiit.server.chat.handler.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import static cwchoiit.server.chat.constants.MessageType.FETCH_USER_INVITE_CODE_REQUEST;

@Getter
public class FetchUserInviteCodeRequest extends BaseRequest {

    @JsonCreator
    public FetchUserInviteCodeRequest() {
        super(FETCH_USER_INVITE_CODE_REQUEST);
    }
}
